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

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.web.WebUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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

  @Autowired
  private MessageSource messageSource;

  @RequestMapping(method = RequestMethod.GET)
  public String index(RedirectAttributes model) {

    Long groupId = WebUtils.getSelectedPhysicalResourceGroupId();

    if (groupId != null) {
      PhysicalResourceGroup group = physicalResourceGroupService.find(groupId);

      if (!group.isActive()) {
        String successMessage = WebUtils.getMessage(messageSource, "info_activation_request_send", group.getName(),
            group.getManagerEmail());
        WebUtils.addInfoMessage(model, createNewActivationLinkForm(new Object[] {
            environment.getExternalBodUrl() + ActivationEmailController.ACTIVATION_MANAGER_PATH,
            group.getId().toString(), successMessage }));

        return "redirect:manager/physicalresourcegroups/edit?id=" + group.getId();
      }
    }

    return "manager/index";
  }

  String createNewActivationLinkForm(Object... args) {
    return String.format("Your Physical Resource Group is not activated yet, please do so now. "
        + "<a href=\"%s?id=%s\" class=\"btn btn-primary\" data-form=\"true\" data-success=\"%s\">Resend email</a>",
        args);
  }
}
