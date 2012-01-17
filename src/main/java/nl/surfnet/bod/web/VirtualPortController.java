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

import static nl.surfnet.bod.web.WebUtils.*;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.web.security.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/virtualports")
public class VirtualPortController {

  @Autowired
  private VirtualPortService virtualPortService;

  @RequestMapping(method = RequestMethod.GET)
  public String list(@RequestParam(value = PAGE_KEY, required = false) final Integer page, final Model uiModel) {

    uiModel.addAttribute(
        "virtualPorts",
        virtualPortService.findAllEntriesForUser(Security.getUserDetails(), calculateFirstPage(page), MAX_ITEMS_PER_PAGE));
    uiModel.addAttribute(MAX_PAGES_KEY, calculateMaxPages(virtualPortService.count()));

    return "virtualports/list";
  }

}
