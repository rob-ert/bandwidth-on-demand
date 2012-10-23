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

import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.event.EntityStatistics;
import nl.surfnet.bod.service.LogEventService;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.support.ReservationFilterViewFactory;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.NocStatisticsView;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.annotations.VisibleForTesting;

@Controller("nocDashboardController")
@RequestMapping(DashboardController.PAGE_URL)
public class DashboardController {

  public static final String PAGE_URL = "/noc";
  private static final String CHECK_PORTS = "/checkports";
  private static final String CHECK_PORTS_URL = PAGE_URL + CHECK_PORTS;

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Resource
  private PhysicalPortService physicalPortService;

  @Resource
  private ReservationService reservationService;

  @Resource
  private MessageSource messageSource;

  @Resource
  private Environment environment;

  @Resource
  private LogEventService logEventService;

  @RequestMapping(method = RequestMethod.GET)
  public String index(Model model) {

    DateTime end = DateTime.now();
    DateTime start = end.minus(WebUtils.DEFAULT_REPORTING_PERIOD);
    model.addAttribute("stats", determineStatistics(Security.getUserDetails(), start, end));
    model.addAttribute("defaultDuration", ReservationFilterViewFactory.DEFAULT_FILTER_INTERVAL_STRING);

    generateErrorMessagesForUnalignedPorts(model, physicalPortService.findUnalignedPhysicalPorts());

    return "noc/index";
  }

  @RequestMapping(value = CHECK_PORTS, method = RequestMethod.GET)
  public String forceCheckForPortInconsitencies(String callbackViewName) {
    log.info("Manually forcing check for port inconsitencies");
    physicalPortService.forceCheckForPortInconsitencies();
    return "redirect:/noc";
  }

  @VisibleForTesting
  NocStatisticsView determineStatistics(RichUserDetails user, DateTime start, DateTime end) {
    ReservationFilterViewFactory reservationFilterViewFactory = new ReservationFilterViewFactory();

    long countPhysicalPorts = physicalPortService.countAllocated();

    long countElapsedReservations = reservationService.countAllEntriesUsingFilter(reservationFilterViewFactory
        .create(ReservationFilterViewFactory.ELAPSED));

    long countActiveReservations = reservationService.countAllEntriesUsingFilter(reservationFilterViewFactory
        .create(ReservationFilterViewFactory.ACTIVE));

    long countComingReservations = reservationService.countAllEntriesUsingFilter(reservationFilterViewFactory
        .create(ReservationFilterViewFactory.COMING));

    long countMissingPhysicalPorts = physicalPortService.countUnalignedPhysicalPorts();

    EntityStatistics<PhysicalPort> physicalPortStats = logEventService
        .determineStatisticsForNocByEventTypeAndDomainObjectClassBetween(user, PhysicalPort.class, start, end);

    EntityStatistics<VirtualPort> virtualPortStats = logEventService
        .determineStatisticsForNocByEventTypeAndDomainObjectClassBetween(user, VirtualPort.class, start, end);

    EntityStatistics<Reservation> reservationStats = logEventService
        .determineStatisticsForNocByEventTypeAndDomainObjectClassBetween(user, Reservation.class, start, end);

    return new NocStatisticsView(countPhysicalPorts, countElapsedReservations, countActiveReservations,
        countComingReservations, countMissingPhysicalPorts, physicalPortStats, virtualPortStats, reservationStats);
  }

  private void generateErrorMessagesForUnalignedPorts(Model model, List<PhysicalPort> unaliagnedPhysicalPorts) {

    final String forcePortCheckButton = createForcePortCheckButton(environment.getExternalBodUrl() + CHECK_PORTS_URL);

    for (PhysicalPort port : unaliagnedPhysicalPorts) {
      WebUtils.addErrorMessage(forcePortCheckButton, model, messageSource, "info_physicalport_unaligned_with_nms", port
          .getNmsPortId(), port.getNocLabel());
    }
  }

  private String createForcePortCheckButton(String actionUrl) {
    return String.format(
        "<a href=\"%s\" rel=\"tooltip\" title=\"Force port check\"><i class=\"icon-refresh\"><!--  --></i></a>",
        actionUrl);
  }
}
