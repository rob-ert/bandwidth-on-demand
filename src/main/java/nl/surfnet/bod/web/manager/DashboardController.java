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
import nl.surfnet.bod.domain.VirtualPortRequestLink;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.util.Orderings;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.view.ManagerStatisticsView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller("managerDashboardController")
@RequestMapping("/manager")
public class DashboardController {

  @Autowired
  private PhysicalResourceGroupService physicalResourceGroupService;

  @Autowired
  private VirtualPortService virtualPortService;

  @RequestMapping(method = RequestMethod.GET)
  public String index(Model model) {
    Long groupId = WebUtils.getSelectedPhysicalResourceGroupId();

    if (groupId == null) {
      return "redirect:/";
    }

    PhysicalResourceGroup group = physicalResourceGroupService.find(groupId);
    Collection<VirtualPortRequestLink> requests = virtualPortService.findPendingRequests(group);

    model.addAttribute("prg", group);
    model.addAttribute("requests", Orderings.vpRequestLinkOrdring().sortedCopy(requests));

    model.addAttribute("stats", determineStatistics());

    return "manager/index";
  }

  private ManagerStatisticsView determineStatistics() {
    return new ManagerStatisticsView(3, 4, 6, 103, 2);
  }
}
