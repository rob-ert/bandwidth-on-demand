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

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.event.EntityStatistics;
import nl.surfnet.bod.service.LogEventService;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.ReservationFilterViewFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.NocStatisticsView;
import nl.surfnet.bod.web.view.ReservationFilterView;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DashboardControllerTest {

  @InjectMocks
  private DashboardController subject;

  @Mock
  private PhysicalPortService physicalPortServiceMock;

  @Mock
  private ReservationService reservationServiceMock;

  @Mock
  private Environment environment;

  @Mock
  private LogEventService logEventServiceMock;

  @Test
  public void shouldAddNullPrgToModel() {
    RichUserDetails user = new RichUserDetailsFactory().addUserRole().create();

    Security.setUserDetails(user);

    RedirectAttributes model = new ModelStub();

    String page = subject.index(model);

    assertThat(WebUtils.getAttributeFromModel("stats", model), notNullValue());
    assertThat(page, is("noc/index"));
  }

  @Test
  public void shouldAddStatisticsToModel() {
    ReservationFilterView elapsedFilter = new ReservationFilterViewFactory()
        .create(ReservationFilterViewFactory.ELAPSED);
    ReservationFilterView activeFilter = new ReservationFilterViewFactory().create(ReservationFilterViewFactory.ACTIVE);
    ReservationFilterView comingFilter = new ReservationFilterViewFactory().create(ReservationFilterViewFactory.COMING);

    RichUserDetails noc = new RichUserDetailsFactory().addNocRole().create();
    Security.setUserDetails(noc);
    Security.switchToNocEngineer();

    when(physicalPortServiceMock.countAllocated()).thenReturn(2L);
    when(reservationServiceMock.countAllEntriesUsingFilter(elapsedFilter)).thenReturn(3L);
    when(reservationServiceMock.countAllEntriesUsingFilter(activeFilter)).thenReturn(4L);
    when(reservationServiceMock.countAllEntriesUsingFilter(comingFilter)).thenReturn(5L);
    when(physicalPortServiceMock.countUnalignedPhysicalPorts()).thenReturn(6L);

    DateTime end = DateTime.now();
    DateTime start = end.minus(WebUtils.DEFAULT_REPORTING_PERIOD);
    // Entity statistics
    EntityStatistics<PhysicalPort> physicalPortStats = new EntityStatistics<PhysicalPort>(PhysicalPort.class, start, 1,
        2, 3, end);
    EntityStatistics<VirtualPort> virtualPortStats = new EntityStatistics<VirtualPort>(VirtualPort.class, start, 4, 5,
        6, end);
    EntityStatistics<Reservation> reservationStats = new EntityStatistics<Reservation>(Reservation.class, start, 7, 8,
        9, end);

    when(
        logEventServiceMock.determineStatisticsForNocByEventTypeAndDomainObjectClassBetween(noc, PhysicalPort.class,
            start, end)).thenReturn(physicalPortStats);

    when(
        logEventServiceMock.determineStatisticsForNocByEventTypeAndDomainObjectClassBetween(noc, VirtualPort.class,
            start, end)).thenReturn(virtualPortStats);

    when(
        logEventServiceMock.determineStatisticsForNocByEventTypeAndDomainObjectClassBetween(noc, Reservation.class,
            start, end)).thenReturn(reservationStats);

    NocStatisticsView statistics = subject.determineStatistics();

    assertThat(statistics.getPhysicalPortsAmount(), is(2L));
    assertThat(statistics.getElapsedReservationsAmount(), is(3L));
    assertThat(statistics.getActiveReservationsAmount(), is(4L));
    assertThat(statistics.getComingReservationsAmount(), is(5L));
    assertThat(statistics.getUnalignedPhysicalPortsAmount(), is(6L));
  }
}
