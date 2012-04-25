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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.support.ReservationFilterViewFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.ReservationFilterView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Sort;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class ReservationControllerTest {

  @InjectMocks
  private ReservationController subject;

  @Mock
  private ReservationService reservationServiceMock;

  @Mock
  private ReservationFilterViewFactory reservationFilterViewFactoryMock;

  private final RichUserDetails manager = new RichUserDetailsFactory().create();

  private ReservationFilterView filter = new ReservationFilterViewFactory()
      .create(ReservationFilterViewFactory.COMMING);

  @Before
  public void login() {
    Security.setUserDetails(manager);
  }

  @Test
  public void listReservationsForManager() {
    ModelStub model = new ModelStub();

    Reservation reservation = new ReservationFactory().create();

    when(reservationFilterViewFactoryMock.create(filter.getId())).thenReturn(filter);

    when(
        reservationServiceMock.findEntriesForManagerUsingFilter(eq(manager), eq(filter), eq(0),
            eq(WebUtils.MAX_ITEMS_PER_PAGE), any(Sort.class))).thenReturn(Lists.newArrayList(reservation));

    subject.list(0, "id", "asc", filter.getId(), model);

    assertThat(model.asMap(), hasKey("list"));

    // TODO move to abstractReservationController
    // assertThat(((List<Reservation>) model.asMap().get("list")),
    // contains(reservation));
  }
}
