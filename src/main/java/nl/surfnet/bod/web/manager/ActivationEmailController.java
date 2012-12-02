/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
package nl.surfnet.bod.web.manager;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.ActivationEmailLink;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.security.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.ImmutableList;

@RequestMapping(ActivationEmailController.ACTIVATION_MANAGER_PATH)
@Controller
public class ActivationEmailController {

  public static final String ACTIVATION_MANAGER_PATH = "/manager/activate";

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Resource
  private PhysicalResourceGroupService physicalResourceGroupService;

  @Resource
  private MessageSource messageSource;

  @RequestMapping(value = "/{uuid}", method = RequestMethod.GET)
  public String activateEmail(@PathVariable String uuid, Model model, RedirectAttributes redirectAttrs) {
    ActivationEmailLink link = physicalResourceGroupService.findActivationLink(uuid);

    if (link == null || link.getSourceObject() == null) {
      redirectAttrs.addFlashAttribute(WebUtils.INFO_MESSAGES_KEY, ImmutableList.of("Activation link is not valid"));
      return "redirect:/";
    }

    PhysicalResourceGroup physicalResourceGroup = link.getSourceObject();

    if (!Security.isManagerMemberOf(physicalResourceGroup)) {
      WebUtils.addInfoFlashMessage(redirectAttrs, messageSource, "info_activation_request_notallowed", Security
          .getUserDetails().getDisplayName(), physicalResourceGroup.getName());

      log.debug("Manager [{}] has no right to access physical resourcegroup: {}", Security.getUserDetails()
          .getUsername(), physicalResourceGroup.getName());

      return "redirect:/";
    }

    model.addAttribute("link", link);
    model.addAttribute("physicalResourceGroup", physicalResourceGroup);

    Security.switchToManager(physicalResourceGroup);

    if (link.isActivated()) {
      return "manager/linkActive";
    }
    else if (emailHasChanged(link)) {
      return "manager/linkChanged";
    }
    else if (link.isValid()) {
      physicalResourceGroupService.activate(link);

      return "manager/emailConfirmed";
    }

    return "manager/linkNotValid";
  }

  private boolean emailHasChanged(ActivationEmailLink link) {
    return !link.getSourceObject().getManagerEmail().equals(link.getToEmail());
  }

  @RequestMapping(method = RequestMethod.POST)
  public String create(PhysicalResourceGroup physicalResourceGroup) {
    PhysicalResourceGroup foundPhysicalResourceGroup = physicalResourceGroupService.find(physicalResourceGroup.getId());

    physicalResourceGroupService.sendActivationRequest(foundPhysicalResourceGroup);

    return "manager/index";
  }
}
