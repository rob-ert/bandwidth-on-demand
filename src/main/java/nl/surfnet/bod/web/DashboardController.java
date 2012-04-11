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

import nl.surfnet.bod.web.security.Security;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping("/")
@Controller
public class DashboardController {

  @RequestMapping(method = RequestMethod.GET)
  public String index(Model model) {

    model.addAttribute("userGroups", Security.getUserDetails().getUserGroups());

    if (Security.isSelectedNocRole()) {
      return "redirect:noc";
    }

    if (Security.isSelectedManagerRole()) {
      return "redirect:manager";
    }

    if (Security.isSelectedUserRole()) {
      return "index";
    }

    return "noUserRole";
  }
}
