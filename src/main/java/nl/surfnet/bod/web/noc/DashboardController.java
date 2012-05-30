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
package nl.surfnet.bod.web.noc;

import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.support.ReservationFilterViewFactory;
import nl.surfnet.bod.web.view.NocStatisticsView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller("nocDashboardController")
@RequestMapping("/noc")
public class DashboardController {

  @Autowired
  private PhysicalPortService physicalPortService;

  @Autowired
  private ReservationService reservationService;

  @RequestMapping(method = RequestMethod.GET)
  public String index(Model model) {
    
    model.addAttribute("stats", determineStatistics());
    model.addAttribute("defaultDuration", ReservationFilterViewFactory.DEFAULT_FILTER_INTERVAL_STRING);

    return "noc/index";
  }

  NocStatisticsView determineStatistics() {
    ReservationFilterViewFactory reservationFilterViewFactory = new ReservationFilterViewFactory();

    long countPhysicalPorts = physicalPortService.countAllocated();

    long countElapsedReservations = reservationService.countAllEntriesUsingFilter(reservationFilterViewFactory
        .create(ReservationFilterViewFactory.ELAPSED));

    long countActiveReservations = reservationService.countAllEntriesUsingFilter(reservationFilterViewFactory
        .create(ReservationFilterViewFactory.ACTIVE));

    long countComingReservations = reservationService.countAllEntriesUsingFilter(reservationFilterViewFactory
        .create(ReservationFilterViewFactory.COMING));

    return new NocStatisticsView(countPhysicalPorts, countElapsedReservations, countActiveReservations,
        countComingReservations);
  }
}
