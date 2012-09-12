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
package nl.surfnet.bod.web;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.web.manager.ActivationEmailController;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.base.Optional;

@Controller
@RequestMapping("/switchrole")
public class SwitchRoleController {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Resource
  private PhysicalResourceGroupService physicalResourceGroupService;

  @Resource
  private Environment environment;

  @Resource
  private MessageSource messageSource;

  @RequestMapping(method = RequestMethod.POST)
  public String switchRole(final String roleId, final Model uiModel, final RedirectAttributes redirectAttribs) {
    RichUserDetails userDetails = Security.getUserDetails();

    if (StringUtils.hasText(roleId)) {
      userDetails.switchToRoleById(Long.valueOf(roleId));
    }

    return determineViewNameAndAddAttributes(userDetails.getSelectedRole(), redirectAttribs);
  }

  @RequestMapping(value = "logout", method = RequestMethod.GET)
  public String logout(HttpServletRequest request) {
    logger.info("Logging out user: {}", Security.getUserDetails().getUsername());
    request.getSession().invalidate();

    return "redirect:" + environment.getShibbolethLogoutUrl();
  }

  private String determineViewNameAndAddAttributes(BodRole selectedRole, RedirectAttributes redirectAttribs) {
    Optional<Long> groupId = WebUtils.getSelectedPhysicalResourceGroupId();

    if (groupId.isPresent()) {
      PhysicalResourceGroup group = physicalResourceGroupService.find(groupId.get());

      if (!group.isActive()) {
        String successMessage = WebUtils.getMessageWithBoldArguments(messageSource, "info_activation_request_send",
            group.getName(), group.getManagerEmail());
        String newLinkButton = createNewActivationLinkForm(environment.getExternalBodUrl()
            + ActivationEmailController.ACTIVATION_MANAGER_PATH, group.getId().toString(), successMessage);

        WebUtils.addInfoFlashMessage(newLinkButton, redirectAttribs, messageSource,
            "info_physicalresourcegroup_not_activated");

        return "redirect:manager/physicalresourcegroups/edit?id=" + group.getId();
      }
    }
    return selectedRole.getRole().getViewName();
  }

  String createNewActivationLinkForm(Object... args) {
    return String.format(
        "<a href=\"%s?id=%s\" class=\"btn btn-primary\" data-form=\"true\" data-success=\"%s\">Resend email</a>", args);
  }
}
