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

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.support.ReservationFilterViewFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.ReservationFilterView;
import nl.surfnet.bod.web.view.ReservationView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;

import com.google.common.collect.Lists;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReservationControllerTest {

  private final ReservationService reservationService = new ReservationService();

  @Mock
  private ReservationService reservationServiceMock;

  @Mock
  private ReservationFilterViewFactory reservationFilterViewFactoryMock;

  @InjectMocks
  private ReservationController subject;

  private final ReservationFilterView filter2012 = new ReservationFilterViewFactory().create("2012");

  private final List<Reservation> reservationsFor2012 = Lists.newArrayList();
  private final List<ReservationView> reservationViewsFor2012 = Lists.newArrayList();

  private final Integer page = 0;
  private final Model model = new ModelStub();
  private Integer size;

  @Before
  public void setUp() {
    Security.setUserDetails(new RichUserDetailsFactory().create());

    List<Reservation> reservations = Lists.newArrayList();
    for (int i = 0; i <= WebUtils.MAX_ITEMS_PER_PAGE; i++) {
      Reservation reservation = new ReservationFactory().create();
      reservations.add(reservation);
    }

    reservationsFor2012.addAll(reservations);
    size = new Integer(reservationsFor2012.size());

    reservationViewsFor2012.addAll(reservationService.transformToView(reservationsFor2012, Security.getUserDetails()));

    when(reservationFilterViewFactoryMock.create(filter2012.getId())).thenReturn(filter2012);

    when(reservationServiceMock.transformToView(reservations, Security.getUserDetails())).thenReturn(
        reservationViewsFor2012);

    when(reservationServiceMock.countAllEntriesUsingFilter(filter2012)).thenReturn((long) reservationsFor2012.size());

    when(
        reservationServiceMock.findAllEntriesUsingFilter(any(ReservationFilterView.class), anyInt(), anyInt(),
            any(Sort.class))).thenReturn(reservationsFor2012);
  }

  @Test
  public void shouldHaveMaxPageOnModel() {
    subject.filter(page, "id", "asc", "", filter2012.getId(), model);

    assertThat((Integer) model.asMap().get(WebUtils.MAX_PAGES_KEY),
        is(Integer.valueOf(WebUtils.calculateMaxPages(size.longValue()))));
  }

  @Test
  public void defaultListViewShouldRedirectToDefaultFilterView() {
    String viewName = subject.list(page, "id", "asc", model);

    assertThat(viewName, is(subject.listUrl()));
  }

  @Test
  public void filteredViewShouldRedirectToListView() {
    String viewName = subject.filter(page, "id", "asc", null, filter2012.getId(), model);

    assertThat(viewName, is(subject.listUrl()));
  }
}
