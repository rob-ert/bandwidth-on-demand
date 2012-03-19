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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
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
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.ReservationFilterView;
import nl.surfnet.bod.web.view.ReservationView;

import org.joda.time.DateTimeUtils;
import org.joda.time.DurationFieldType;
import org.joda.time.LocalDateTime;
import org.joda.time.Months;
import org.joda.time.Period;
import org.joda.time.ReadablePeriod;
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

  private ReservationView resStart = new ReservationView(new ReservationFactory().setStartAndDuration(START, PERIOD)
      .create());

  private ReservationView resFirst = new ReservationView(new ReservationFactory().setStartAndDuration(
      START.minusYears(2), PERIOD).create());

  private ReservationView resLast = new ReservationView(new ReservationFactory().setStartAndDuration(
      START.plusYears(2), PERIOD).create());

  private ReservationView resCommingPeriod = new ReservationView(new ReservationFactory().setStartAndDuration(
      START.plus(ReservationController.DEFAULT_FILTER_INTERVAL), PERIOD).create());

  private ReservationView resElapsedPeriod = new ReservationView(new ReservationFactory().setStartAndDuration(
      START.minus(ReservationController.DEFAULT_FILTER_INTERVAL), PERIOD).create());

  private List<ReservationView> reservationsToFilter = Lists.newArrayList(resFirst, resElapsedPeriod, resStart,
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
  public void testCommingPeriodEndDate() {
    List<ReservationView> reservations = subject.getReservationsBetweenBasedOnEndDateOnly(START,
        START.plus(ReservationController.DEFAULT_FILTER_INTERVAL), reservationsToFilter);

    assertThat(reservations, hasSize(1));
    assertThat(reservations, contains(resStart));
  }

  @Test
  public void testElapedPeriodEndDate() {
    List<ReservationView> reservations = subject.getReservationsBetweenBasedOnEndDateOnly(START,
        START.plus(ReservationController.DEFAULT_FILTER_INTERVAL), reservationsToFilter);

    assertThat(reservations, hasSize(1));
    assertThat(reservations, containsInAnyOrder(resStart));
  }

  @Test
  public void testCommingPeriodStartAndEndDate() {
    List<ReservationView> reservations = subject.getReservationsBetweenBasedOnStartDateOrEndDate(START,
        START.plus(ReservationController.DEFAULT_FILTER_INTERVAL), reservationsToFilter);

    assertThat(reservations, hasSize(2));
    assertThat(reservations, containsInAnyOrder(resStart, resCommingPeriod));
  }

  @Test
  public void testElapsedPeriodStartAndEndDate() {
    List<ReservationView> reservations = subject.getReservationsBetweenBasedOnStartDateOrEndDate(
        START.minus(ReservationController.DEFAULT_FILTER_INTERVAL), START, reservationsToFilter);

    assertThat(reservations, hasSize(2));
    assertThat(reservations, containsInAnyOrder(resStart, resElapsedPeriod));
  }

  @Test
  public void testDistinctReservationYears() {
    List<Integer> reservationYears = subject.getDistinctReservationYears(reservationsToFilter);

    assertThat(reservationYears, hasSize(4));
    assertThat(
        reservationYears,
        containsInAnyOrder(Integer.valueOf(resFirst.getStartDateTime().getYear()),
            Integer.valueOf(resLast.getStartDateTime().getYear()),
            Integer.valueOf(resElapsedPeriod.getStartDateTime().getYear()), Integer.valueOf(START.getYear())));
  }

  @Test
  public void testShouldHaveSixSpecificFilters() {
    model.addAttribute(WebUtils.DATA_LIST, reservationsToFilter);

    DateTimeUtils.setCurrentMillisFixed(START.toDate().getTime());

    try {
      subject.populateFilter(reservationsToFilter, model);
      List<ReservationFilterView> reservationFilters = (List<ReservationFilterView>) model.asMap().get(
          WebUtils.FILTER_LIST);
      assertThat(reservationFilters, hasSize(6));

      assertThat(
          new ReservationFilterView(ReservationController.FILTER_COMMING_PERIOD, null, START,
              START.plus(ReservationController.DEFAULT_FILTER_INTERVAL)), is(reservationFilters.get(0)));

      assertThat(
          new ReservationFilterView(ReservationController.FILTER_ELAPSED_PERIOD, null,
              START.minus(ReservationController.DEFAULT_FILTER_INTERVAL), START), is(reservationFilters.get(1)));

      assertThat(new ReservationFilterView(2010), is(reservationFilters.get(2)));
      assertThat(new ReservationFilterView(2011), is(reservationFilters.get(3)));
      assertThat(new ReservationFilterView(2012), is(reservationFilters.get(4)));
      assertThat(new ReservationFilterView(2014), is(reservationFilters.get(5)));
    }
    finally {
      DateTimeUtils.setCurrentMillisSystem();
    }
  }

//  @Test
//  public void testFindFilter() {
//    int duration = ReservationController.DEFAULT_FILTER_INTERVAL.get(DurationFieldType.months());
//    when(
//        messageSource.getMessage("label_reservation_filter_comming_period", new Object[] { duration },
//            LocaleContextHolder.getLocale())).thenReturn("CommingPeriod");
//    when(
//        messageSource.getMessage("label_reservation_filter_elapsed_period", new Object[] { duration },
//            LocaleContextHolder.getLocale())).thenReturn("ElapsedPeriod");
//
//    model.addAttribute(WebUtils.DATA_LIST, reservationsToFilter);
//
//    subject.populateFilter(reservationsToFilter, model);
//
//    // Default selection is first filter...
//    ReservationFilterView expectedFilter = WebUtils.getAttributeFromModel(WebUtils.FILTER_SELECT, model);
//    ReservationFilterView filterView = subject.findFilter(expectedFilter.getId(), model);
//
//    assertThat(filterView, is(expectedFilter));
//  }
//
//  @Test
//  public void testCountWithFilter() {
//
//    model.addAttribute(WebUtils.DATA_LIST, reservationsToFilter);
//
//    subject.populateFilter(reservationsToFilter, model);
//    ReservationFilterView filter = WebUtils.getAttributeFromModel(WebUtils.FILTER_SELECT, model);
//
//    long count = subject.count(filter.getId(), model);
//
//    assertThat(count, is(1l));
//  }
//
//  @Test
//  public void testCountWithoutFilterShoulfFindAllReservations() {
//    model.addAttribute(WebUtils.DATA_LIST, reservationsToFilter);
//
//    subject.populateFilter(reservationsToFilter, model);
//
//    long count = subject.count(null, model);
//
//    assertThat(count, is(Long.valueOf(reservationsToFilter.size())));
//  }
//
//  @Test
//  public void testCountWithNonExistingFilterShoulfFindAllReservations() {
//    model.addAttribute(WebUtils.DATA_LIST, reservationsToFilter);
//
//    subject.populateFilter(reservationsToFilter, model);
//
//    long count = subject.count("9999", model);
//
//    assertThat(count, is(Long.valueOf(reservationsToFilter.size())));
//  }
}
