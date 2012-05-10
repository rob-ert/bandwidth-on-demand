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
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.support.ReservationFilterViewFactory;
import nl.surfnet.bod.util.Orderings;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
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

  @Autowired
  private PhysicalPortService physicalPortService;

  @Autowired
  private ReservationService reservationService;

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

    model.addAttribute("stats", determineStatistics(Security.getUserDetails()));

    return "manager/index";
  }

   ManagerStatisticsView determineStatistics(RichUserDetails manager) {
    ReservationFilterViewFactory reservationFilterViewFactory = new ReservationFilterViewFactory();
    PhysicalResourceGroup managerPrg = physicalResourceGroupService.find(manager.getSelectedRole()
        .getPhysicalResourceGroupId());

    long countPhysicalPorts = physicalPortService.countAllocatedForPhysicalResourceGroup(managerPrg);

    long countVirtualPorts = virtualPortService.countForManager(manager);

    long countElapsedReservations = reservationService.countForFilterAndManager(manager,
        reservationFilterViewFactory.create(ReservationFilterViewFactory.ELAPSED));

    long countActiveReservations = reservationService.countForFilterAndManager(manager,
        reservationFilterViewFactory.create(ReservationFilterViewFactory.ACTIVE));

    long countComingReservations = reservationService.countForFilterAndManager(manager,
        reservationFilterViewFactory.create(ReservationFilterViewFactory.COMING));

    return new ManagerStatisticsView(countPhysicalPorts, countVirtualPorts, countElapsedReservations,
        countActiveReservations, countComingReservations);
  }
}
