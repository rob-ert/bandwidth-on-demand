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

import java.util.Collection;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.security.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller("managerDashboardController")
@RequestMapping("/manager")
public class DashboardController {

  @Autowired
  private PhysicalResourceGroupService physicalResourceGroupService;

  @Autowired
  private Environment environment;

  @RequestMapping(method = RequestMethod.GET)
  public String index(RedirectAttributes redirectAttributes) {
    Collection<PhysicalResourceGroup> groups = physicalResourceGroupService
        .findAllForManager(Security.getUserDetails());

    for (PhysicalResourceGroup group : groups) {
      if (!group.isActive()) {

        WebUtils.addInfoMessageWithHtml(redirectAttributes, createNewActivationLinkForm(group),
            "Your Physical Resource group is not activated yet, please do so now. ");

        return "redirect:manager/physicalresourcegroups/edit?id=" + group.getId();
      }
    }

    return "index";
  }

  String createNewActivationLinkForm(PhysicalResourceGroup physicalResourceGroup) {
    return String.format("<form style='display: inline' id=\"activateFrom\" action=\"%s\" method=\"POST\" + "
        + "enctype=\"application/x-www-form-urlencoded\">"
        + "<input id=\"id\" name=\"id\" type=\"hidden\" value=\"%d\"><input class=\"btn primary\""
        + "value=\"Send email\" type=\"submit\"></div></form>", environment.getExternalBodUrl()
        + ActivationEmailController.ACTIVATION_MANAGER_PATH, physicalResourceGroup.getId());
  }

}
