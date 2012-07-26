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

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.security.Security.RoleEnum;

@RequestMapping("/")
@Controller
public class DetermineRoleController {

  @RequestMapping(method = RequestMethod.GET)
  public String index(Model model, RedirectAttributes redirectAttributes) {

    preserveInfoMessages(model, redirectAttributes);

    if (Security.isSelectedNocRole()) {
      return RoleEnum.NOC_ENGINEER.getViewName();
    }

    if (Security.isSelectedManagerRole()) {
      return RoleEnum.ICT_MANAGER.getViewName();
    }

    return RoleEnum.USER.getViewName();
  }

  private void preserveInfoMessages(Model model, RedirectAttributes redirectAttributes) {
    Map<String, Object> modelMap = model.asMap();

    if (modelMap.containsKey(WebUtils.INFO_MESSAGES_KEY)) {
      Object messages = modelMap.remove(WebUtils.INFO_MESSAGES_KEY);
      redirectAttributes.addFlashAttribute(WebUtils.INFO_MESSAGES_KEY, messages);
    }
  }


}
