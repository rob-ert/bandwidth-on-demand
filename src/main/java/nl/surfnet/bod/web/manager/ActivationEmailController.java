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

import nl.surfnet.bod.domain.ActivationEmailLink;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.security.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping(ActivationEmailController.ACTIVATION_MANAGER_PATH)
@Controller
public class ActivationEmailController {

  public static final String ACTIVATION_MANAGER_PATH = "/manager/activate";
  private static final String MODEL_KEY = "link";

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private PhysicalResourceGroupService physicalResourceGroupService;

  @Autowired
  private MessageSource messageSource;

  @RequestMapping(value = "/{uuid}", method = RequestMethod.GET)
  public String activateEmail(@PathVariable String uuid, Model uiModel) {
    ActivationEmailLink<PhysicalResourceGroup> link = physicalResourceGroupService.findActivationLink(uuid);

    if (link == null) {
      return "index";
    }

    uiModel.addAttribute(MODEL_KEY, link);
    PhysicalResourceGroup physicalResourceGroup = link.getSourceObject();
    if (physicalResourceGroup == null) {
      return "index";
    }

    uiModel.addAttribute("physicalResourceGroup", physicalResourceGroup);
    if (!Security.isManagerMemberOf(physicalResourceGroup)) {
      WebUtils.addInfoMessage(uiModel, "User %s is not allowed to activate physical resource group %s", Security
          .getUserDetails().getDisplayName(), physicalResourceGroup.getName());
      log.debug("Manager [{}] has no right to access physical resourcegroup: {}", Security.getUserDetails()
          .getUsername(), physicalResourceGroup.getName());
      return "index";
    }

    if (link.isActivated()) {
      log.debug("Link [{}] already activated on: {}", link.getUuid(), link.getActivationDateTime());
      return "manager/linkActive";
    }
    else if (!link.getSourceObject().getManagerEmail().equals(link.getToEmail())) {
      log.debug("Email address [{}] of physical resource group [{}] differs from the link [{}]", new Object[] {
          physicalResourceGroup.getManagerEmail(), link.getSourceObject().getName(), link.getToEmail() });
      return "manager/linkChanged";
    }
    else if (link.isValid()) {
      physicalResourceGroupService.activate(link);

      // TODO Roles shoud be reload here, in case the user just has become a
      // manager by activation of the link
      Security.getUserDetails().switchToManagerRoleByPhysicalResourceGroup(physicalResourceGroup);

      return "manager/emailConfirmed";
    }

    log.debug("Link [{}} for physical resource group [{}] was not valid", link.getUuid(),
        physicalResourceGroup.getName());

    return "manager/linkNotValid";
  }

  @RequestMapping(method = RequestMethod.POST)
  public String create(PhysicalResourceGroup physicalResourceGroup, final Model model) {
    PhysicalResourceGroup foundPhysicalResourceGroup = physicalResourceGroupService.find(physicalResourceGroup.getId());

    ActivationEmailLink<PhysicalResourceGroup> activationLink = physicalResourceGroupService
        .sendActivationRequest(foundPhysicalResourceGroup);

    WebUtils.addInfoMessage(model, messageSource, "info_activation_request_send", activationLink.getSourceObject()
        .getName(), activationLink.getToEmail());

    return "manager/index";
  }
}
