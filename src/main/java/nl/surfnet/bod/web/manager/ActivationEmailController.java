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
import nl.surfnet.bod.service.InstituteService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping(ActivationEmailController.ACTIVATION_MANAGER_PATH)
@Controller
public class ActivationEmailController {

  public final static String ACTIVATION_MANAGER_PATH = "/manager/activate";

  @Autowired
  private PhysicalResourceGroupService physicalResourceGroupService;
  @Autowired
  private InstituteService instituteService;

  @RequestMapping(value = "/{uuid}", method = RequestMethod.GET)
  public String activateEmail(@PathVariable String uuid, Model uiModel) {
    ActivationEmailLink<PhysicalResourceGroup> link = physicalResourceGroupService.findActivationLink(uuid);

    if (link == null) {
      return "index";
    }
    else if (!link.isValid()) {
      return "manager/linkNotValid";
    }

    PhysicalResourceGroup group = link.getSourceObject();

    physicalResourceGroupService.activate(link);
    instituteService.fillInstituteForPhysicalResourceGroup(group);

    uiModel.addAttribute("physicalResourceGroup", group);

    return "manager/emailConfirmed";
  }
}
