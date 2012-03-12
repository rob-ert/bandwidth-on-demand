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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.support.*;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.ReservationFilterView;

import org.joda.time.Chronology;
import org.joda.time.DateMidnight;
import org.joda.time.DurationFieldType;
import org.joda.time.LocalDateTime;
import org.joda.time.Months;
import org.joda.time.Period;
import org.joda.time.ReadablePeriod;
import org.joda.time.Years;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.ui.Model;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class ReservationControllerTest {

  private static final String INFO_AT_LEAST_TWO_PORTS = "at least two ports";

  private final static LocalDateTime START = LocalDateTime.now().withDate(2012, 01, 01).withTime(01, 0, 0, 0);

  private final static ReadablePeriod PERIOD = Months.THREE;

  @InjectMocks
  private ReservationController subject;

  @Mock
  private VirtualResourceGroupService virtualResourceGroupServiceMock;

  @Mock
  private ReservationService reservationServiceMock;

  @Mock
  private MessageSource messageSource;

  private RichUserDetails user = new RichUserDetailsFactory().create();

  private Model model = new ModelStub();

  private Reservation resStart = new ReservationFactory().setStartAndDuration(START, PERIOD).create();

  private Reservation resFirst = new ReservationFactory().setStartAndDuration(START.minusYears(2), PERIOD).create();

  private Reservation resLast = new ReservationFactory().setStartAndDuration(START.plusYears(2), PERIOD).create();

  private Reservation resCommingPeriod = new ReservationFactory().setStartAndDuration(
      START.plus(ReservationController.DEFAULT_FILTER_INTERVAL), PERIOD).create();

  private Reservation resElapsedPeriod = new ReservationFactory().setStartAndDuration(
      START.minus(ReservationController.DEFAULT_FILTER_INTERVAL), PERIOD).create();

  private List<Reservation> reservationsToFilter = Lists.newArrayList(resFirst, resElapsedPeriod, resStart, 
      resCommingPeriod, resLast);

  @Before
  public void onSetup() {
    Security.setUserDetails(user);
  }

  @Test
  public void newReservationShouldHaveDefaults() {

    VirtualPort sourcePort = new VirtualPortFactory().setMaxBandwidth(8000).create();
    VirtualPort destPort = new VirtualPortFactory().setMaxBandwidth(4000).create();
    VirtualResourceGroup group = new VirtualResourceGroupFactory().addVirtualPorts(sourcePort, destPort).create();

    when(virtualResourceGroupServiceMock.findAllForUser(user)).thenReturn(Lists.newArrayList(group));

    subject.populateVirtualPorts(model);
    subject.createForm(model);

    assertThat(model.asMap(), hasKey("reservation"));
    assertThat(model.asMap(), hasKey("virtualPorts"));
    assertThat(model.asMap(), hasKey("virtualResourceGroups"));

    Reservation reservation = (Reservation) model.asMap().get("reservation");
    assertThat(reservation.getStartDate(), not(nullValue()));
    assertThat(reservation.getEndDate(), not(nullValue()));
    assertThat(reservation.getSourcePort(), is(sourcePort));
    assertThat(reservation.getDestinationPort(), is(destPort));
    assertThat(reservation.getBandwidth(), is(2000));
  }

  @Test
  public void reservationEmptyPorts() {
    when(virtualResourceGroupServiceMock.findAllForUser(user)).thenReturn(
        Collections.<VirtualResourceGroup> emptyList());

    subject.populateVirtualPorts(model);
    subject.createForm(model);

    assertThat(model.asMap(), hasKey(ReservationController.MODEL_KEY));
    assertThat(model.asMap(), hasKey("virtualPorts"));
    assertThat(model.asMap(), hasKey("virtualResourceGroups"));

    Reservation reservation = (Reservation) model.asMap().get("reservation");
    assertThat(reservation.getStartDate(), not(nullValue()));
    assertThat(reservation.getEndDate(), not(nullValue()));
    assertThat(reservation.getSourcePort(), nullValue());
    assertThat(reservation.getDestinationPort(), nullValue());
    assertThat(reservation.getBandwidth(), nullValue());
  }

  @Test
  public void reservationShouldHaveDefaultDuration() {
    subject.populateVirtualPorts(model);
    Reservation reservation = (Reservation) model.asMap().get(ReservationController.MODEL_KEY);

    Period period = new Period(reservation.getStartDateTime().toDate().getTime(), reservation.getEndDateTime().toDate()
        .getTime());

    assertThat(period.get(DurationFieldType.minutes()),
        is(ReservationController.DEFAULT_RESERVATON_DURATION.get(DurationFieldType.minutes())));
  }

  @Test
  public void listShouldSetListOnModel() {
    Model model = new ModelStub();
    Reservation reservation = new ReservationFactory().create();

    when(reservationServiceMock.findEntries(eq(0), eq(WebUtils.MAX_ITEMS_PER_PAGE), any(Sort.class))).thenReturn(
        Lists.newArrayList(reservation));

    subject.list(1, null, null, model);

    assertThat(model.asMap(), hasKey("list"));
    assertThat(model.asMap(), hasKey("sortProperty"));
    assertThat(model.asMap(), hasKey("sortDirection"));

    assertThat(((List<?>) model.asMap().get("list")), hasSize(1));
  }

  @Test
  public void listWithNonExistingSortProperty() {
    Model model = new ModelStub();
    Reservation reservation = new ReservationFactory().create();

    when(reservationServiceMock.findEntries(eq(0), eq(WebUtils.MAX_ITEMS_PER_PAGE), any(Sort.class))).thenReturn(
        Lists.newArrayList(reservation));

    subject.list(1, "nonExistingProperty", "nonExistingDirection", model);

    assertThat(model.asMap(), hasKey("list"));
    assertThat(model.asMap(), hasKey("sortProperty"));
    assertThat(model.asMap(), hasKey("sortDirection"));

    assertThat(model.asMap().get("sortDirection"), is(Object.class.cast(Direction.ASC)));
    assertThat(model.asMap().get("sortProperty"), is(Object.class.cast("startDateTime")));
    assertThat(((List<?>) model.asMap().get("list")), hasSize(1));
  }

  @Test
  public void lessThenTwoVirtualPortsShouldShowInfoMessage() {
    when(messageSource.getMessage("info_reservation_need_two_virtual_ports", null, LocaleContextHolder.getLocale()))
        .thenReturn(INFO_AT_LEAST_TWO_PORTS);

    String view = subject.createForm(model);

    assertThat(model.asMap().containsKey(MessageView.MODEL_KEY), is(true));
    assertThat(((MessageView) model.asMap().get(MessageView.MODEL_KEY)).getParagraph(),
        containsString(INFO_AT_LEAST_TWO_PORTS));
    assertThat(view, is(MessageView.PAGE_URL));
  }

  @Test
  public void twoVirtualPortsOrMoreShouldNotShowInfoMessage() {
    VirtualPort sourcePort = new VirtualPortFactory().setMaxBandwidth(8000).create();
    VirtualPort destPort = new VirtualPortFactory().setMaxBandwidth(4000).create();
    model.addAttribute("virtualPorts", Lists.newArrayList(sourcePort, destPort));

    when(messageSource.getMessage("info_reservation_need_two_virtual_ports", null, LocaleContextHolder.getLocale()))
        .thenReturn(INFO_AT_LEAST_TWO_PORTS);

    String view = subject.createForm(model);

    assertThat(model.asMap().containsKey(MessageView.MODEL_KEY), is(false));
    assertThat(view, is(ReservationController.PAGE_URL + WebUtils.CREATE));
  }

  @Test
  public void testFirstReservation() {
    Reservation first = subject.getFirstReservation(reservationsToFilter);

    assertThat(first, is(resFirst));
  }

  @Test
  public void testLastReservation() {
    Reservation last = subject.getLastReservation(reservationsToFilter);

    assertThat(last, is(resLast));
  }

  @Test
  public void testCommingPeriod() {
    List<Reservation> reservations = subject.getReservationsBetween(START,
        START.plus(ReservationController.DEFAULT_FILTER_INTERVAL), reservationsToFilter);

    assertThat(reservations, hasSize(1));
    assertThat(reservations, contains(resStart));
    assertThat(reservations, contains(resCommingPeriod));
  }

  @Test
  public void testElapsedPeriod() {
    List<Reservation> reservations = subject.getReservationsBetween(START,
        START.minus(ReservationController.DEFAULT_FILTER_INTERVAL), reservationsToFilter);

    assertThat(reservations, hasSize(1));
    assertThat(reservations, contains(resElapsedPeriod));
  }

  @Test
  public void testDistinctReservationYears() {
    List<Years> reservationYears = subject.getDistinctReservationYears(reservationsToFilter);

    assertThat(reservationYears, hasSize(4));
    assertThat(
        reservationYears,
        contains(Years.years(resFirst.getStartDateTime().getYear()), Years.years(resLast.getStartDateTime().getYear()),
            Years.years(resElapsedPeriod.getStartDateTime().getYear()), Years.years(START.getYear())));
  }
}
