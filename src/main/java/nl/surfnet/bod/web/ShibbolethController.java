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

import java.util.Collection;

import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.service.GroupService;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.util.UserContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@SessionAttributes({ "userContext" })
@RequestMapping("/shibboleth")
public class ShibbolethController {

  @Autowired
  private GroupService groupService;

  @Autowired
  private Environment env;

  @RequestMapping(value = "/groups", method = RequestMethod.GET)
  public String list(@ModelAttribute("userContext") UserContext userContext, final Model uiModel) {
    Collection<UserGroup> groups = groupService.getGroups(userContext.getNameId());

    uiModel.addAttribute("groups", groups);

    return "shibboleth/groups";
  }

  @RequestMapping("/info")
  public String info() {
    return "shibboleth/info";
  }

  @RequestMapping("/login")
  public String login() {
    return env.getImitateShibboleth() ? "shibboleth/login" : "shibboleth/info";
  }
}
