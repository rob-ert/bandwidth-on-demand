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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequestMapping(ActivationEmailController.ACTIVATION_MANAGER_PATH)
@Controller
public class ActivationEmailController {

  public final static String ACTIVATION_MANAGER_PATH = "/manager/activate";
  private static final String MODEL_KEY = "link";

  @Autowired
  private PhysicalResourceGroupService physicalResourceGroupService;

  @RequestMapping(value = "/{uuid}", method = RequestMethod.GET)
  public String activateEmail(@PathVariable String uuid, Model uiModel) {
    ActivationEmailLink<PhysicalResourceGroup> link = physicalResourceGroupService.findActivationLink(uuid);
    uiModel.addAttribute(MODEL_KEY, link);

    if (link == null) {
      return "index";
    }
    uiModel.addAttribute("physicalResourceGroup", link.getSourceObject());

    if (link.isActivated()) {
      return "manager/linkActive";
    }
    else if (link.isValid()) {
      physicalResourceGroupService.activate(link);
      return "manager/emailConfirmed";
    }

    return "manager/linkNotValid";
  }

  @RequestMapping(method = RequestMethod.POST)
  public String create(PhysicalResourceGroup physicalResourceGroup, final BindingResult bindingResult,
      final Model model, final RedirectAttributes redirectAttributes) {

    PhysicalResourceGroup foundPhysicalResourceGroup = physicalResourceGroupService.find(physicalResourceGroup.getId());

    ActivationEmailLink<PhysicalResourceGroup> activationLink = physicalResourceGroupService
        .sendAndPersistActivationRequest(foundPhysicalResourceGroup);

    redirectAttributes.addFlashAttribute("infoMessage", "An activation email for Physcial Resource Group '"
        + activationLink.getSourceObject().getName() + "' was sent to '" + activationLink.getToEmail() + "'");

    return "manager/index";
  }

}
