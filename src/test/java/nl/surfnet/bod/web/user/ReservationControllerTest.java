/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.web.user;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.support.*;
import nl.surfnet.bod.util.MessageRetriever;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.base.MessageView;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.ReservationFilterView;

import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;
import org.joda.time.Period;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.ui.Model;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class ReservationControllerTest {

  @InjectMocks
  private ReservationController subject;

  @Mock
  private VirtualResourceGroupService virtualResourceGroupServiceMock;

  @Mock
  private ReservationService reservationServiceMock;

  @Mock
  private MessageRetriever messageRetriever;

  @Mock
  private final ReservationFilterViewFactory reservationFilterViewFactoryMock = when(
      mock(ReservationFilterViewFactory.class).create(anyString())).thenCallRealMethod().getMock();

  private final RichUserDetails user = new RichUserDetailsFactory().create();
  private final Model model = new ModelStub();

  @Before
  public void onSetup() {
        Security.setUserDetails(user);
  }

  @Test
  public void newReservationShouldHaveDefaults() {
    VirtualResourceGroup group = new VirtualResourceGroupFactory().create();
    VirtualPort sourcePort = new VirtualPortFactory().setMaxBandwidth(8000).setVirtualResourceGroup(group).create();
    VirtualPort destPort = new VirtualPortFactory().setMaxBandwidth(4000).setVirtualResourceGroup(group).create();

    // Make sure source and destination have some
    group.setVirtualPorts(Lists.newArrayList(sourcePort, destPort));

    when(virtualResourceGroupServiceMock.findAllForUser(user)).thenReturn(Lists.newArrayList(group));

    subject.createForm(null, model);

    assertThat(model.asMap(), hasKey("reservation"));
    assertThat(model.asMap(), hasKey("virtualPorts"));
    assertThat(model.asMap(), hasKey("virtualResourceGroups"));

    Reservation reservation = (Reservation) model.asMap().get("reservation");
    assertThat(reservation.getStartDateTime(), not(nullValue()));
    assertThat(reservation.getEndDateTime(), not(nullValue()));
    assertThat(reservation.getSourcePort(), is(sourcePort));
    assertThat(reservation.getDestinationPort(), is(destPort));
    assertThat(reservation.getBandwidth(), is(2000));
  }

  @Test
  public void createFormWithoutAnyGroupsShouldGiveSpecialPage() {
    when(virtualResourceGroupServiceMock.findAllForUser(user)).thenReturn(
        Collections.<VirtualResourceGroup> emptyList());

    String page = subject.createForm(null, model);

    assertThat(page, is("message"));
    assertThat(model.asMap(), hasKey(MessageView.MODEL_KEY));
  }

  @Test
  public void reservationShouldHaveDefaultDuration() {
    VirtualResourceGroup group = new VirtualResourceGroupFactory().create();
    VirtualPort sourcePort = new VirtualPortFactory().setVirtualResourceGroup(group).create();
    VirtualPort destPort = new VirtualPortFactory().setVirtualResourceGroup(group).create();

    // Make sure source and destination have some
    group.setVirtualPorts(Lists.newArrayList(sourcePort, destPort));
    when(virtualResourceGroupServiceMock.findAllForUser(user)).thenReturn(Lists.newArrayList(group));

    subject.createForm(null, model);

    Reservation reservation = (Reservation) model.asMap().get(ReservationController.MODEL_KEY);

    Period period = new Period(reservation.getStartDateTime().toDate().getTime(), reservation.getEndDateTime().toDate()
        .getTime());

    assertThat(period.get(DurationFieldType.minutes()), is(WebUtils.DEFAULT_RESERVATON_DURATION.get(DurationFieldType
        .minutes())));
  }

  @Test
  public void listShouldSetListOnModel() {
    Reservation reservation = new ReservationFactory().setStartDateTime(DateTime.now().plusDays(1)).create();

    when(
        reservationServiceMock.findEntriesForUserUsingFilter(any(RichUserDetails.class),
            any(ReservationFilterView.class), eq(0), eq(Integer.MAX_VALUE), any(Sort.class))).thenReturn(
        Lists.newArrayList(reservation));

    subject.list(0, "name", "asc", model);

    assertThat(model.asMap(), hasKey("list"));
    assertThat(model.asMap(), hasKey("sortProperty"));
    assertThat(model.asMap(), hasKey("sortDirection"));
  }

  @Test
  public void listWithNonExistingSortProperty() {
    Reservation reservation = new ReservationFactory().create();
    List<Reservation> reservations = Lists.newArrayList(reservation);

    when(
        reservationServiceMock.findEntriesForUserUsingFilter(any(RichUserDetails.class),
            any(ReservationFilterView.class), anyInt(), anyInt(), any(Sort.class))).thenReturn(reservations);

    when(reservationServiceMock.pageList(anyInt(), anyInt(), eq(reservations))).thenCallRealMethod();

    subject.filter(1, "nonExistingProperty", "nonExistingDirection", "2012", model);

    assertThat(model.asMap(), hasKey("list"));
    assertThat(model.asMap(), hasKey("sortProperty"));
    assertThat(model.asMap(), hasKey("sortDirection"));

    assertThat(model.asMap().get("sortDirection"), is(Object.class.cast(Direction.ASC)));
    assertThat(model.asMap().get("sortProperty"), is(Object.class.cast("startDateTime")));
    assertThat(((List<?>) model.asMap().get("list")), hasSize(1));
  }

  @Test
  public void listWithNonExistingFilter() {
    when(reservationFilterViewFactoryMock.create(anyString())).thenCallRealMethod();

    String page = subject.filter(1, "name", "asc", "nonExistingFilter", model);

    assertThat(page, is("redirect:../"));
  }

  @Test
  public void lessThenTwoVirtualPortsShouldShowInfoMessage() {
    VirtualResourceGroup group = new VirtualResourceGroupFactory().addVirtualPorts(new VirtualPortFactory().create())
        .create();

    when(virtualResourceGroupServiceMock.findAllForUser(user)).thenReturn(Lists.newArrayList(group));

    String view = subject.createForm(null, model);

    assertThat(view, is(MessageView.PAGE_URL));
    verify(messageRetriever).getMessage(eq("info_reservation_need_two_virtual_ports_title"));
  }
}
