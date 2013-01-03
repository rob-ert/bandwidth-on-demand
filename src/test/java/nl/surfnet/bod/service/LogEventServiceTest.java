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
package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.event.LogEvent;
import nl.surfnet.bod.repo.LogEventRepo;
import nl.surfnet.bod.support.*;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class LogEventServiceTest {

  private static final String GROUP_ID = "urn:groupie";

  @Mock
  private LogEventRepo logEventRepoMock;

  @Mock
  private Logger logMock;

  @Mock
  private RichUserDetails userMock;

  @Mock
  private Environment environmentMock;

  @Mock
  private VirtualResourceGroupService virtualResourceGroupServiceMock;

  @InjectMocks
  private LogEventService subject;

  private final RichUserDetails user = new RichUserDetailsFactory().addUserGroup(GROUP_ID).create();
  private final VirtualResourceGroup vrg = new VirtualResourceGroupFactory().create();

  @Test
  public void shouldCreateLogEventForVirtualResourceGroup() {
    try {
      DateTime now = DateTime.now();
      DateTimeUtils.setCurrentMillisFixed(now.toDate().getTime());

      Collection<LogEvent> logEvents = subject.logCreateEvent(user, vrg);

      assertThat(logEvents, hasSize(1));
      LogEvent logEvent = Iterables.getOnlyElement(logEvents);

      assertThat(logEvent.getUserId(), is(user.getUsername()));
      assertThat(logEvent.getAdminGroups(), hasItem(vrg.getAdminGroup()));
      assertThat(logEvent.getEventTypeWithCorrelationId(), is("Create"));

      assertThat(logEvent.getDomainObjectId(), is(vrg.getId()));
      assertThat(logEvent.getDomainObjectClass(), is(vrg.getClass().getSimpleName()));
      assertThat(logEvent.getDescription(), is(vrg.getLabel()));
      assertThat(logEvent.getDetails(), is(""));

      assertThat(logEvent.getSerializedObject(), is(vrg.toString()));
      assertThat(logEvent.getCreated(), is(now));

      assertThat(logEvent.getOldReservationStatus(), nullValue());
      assertThat(logEvent.getNewReservationStatus(), nullValue());
    }
    finally {
      DateTimeUtils.setCurrentMillisSystem();
    }
  }

  @Test
  public void shouldCreateUpdateLogEventWithStatesForReservation() {
    try {
      DateTime now = DateTime.now();
      DateTimeUtils.setCurrentMillisFixed(now.toDate().getTime());

      Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.AUTO_START).create();

      LogEvent logEvent = subject.logReservationStatusChangeEvent(user, reservation, ReservationStatus.RESERVED);

      assertThat(logEvent.getUserId(), is(user.getUsername()));
      assertThat(logEvent.getAdminGroups(), is(reservation.getAdminGroups()));
      assertThat(logEvent.getEventTypeWithCorrelationId(), is("Update"));

      assertThat(logEvent.getDomainObjectId(), is(reservation.getId()));
      assertThat(logEvent.getDomainObjectClass(), is(reservation.getClass().getSimpleName()));
      assertThat(logEvent.getDescription(), is(reservation.getLabel()));
      assertThat(logEvent.getDetails(), containsString("[RESERVED] to [AUTO_START]"));

      assertThat(logEvent.getSerializedObject(), is(reservation.toString()));
      assertThat(logEvent.getCreated(), is(now));

      assertThat(logEvent.getOldReservationStatus(), is(ReservationStatus.RESERVED));
      assertThat(logEvent.getNewReservationStatus(), is(reservation.getStatus()));
    }
    finally {
      DateTimeUtils.setCurrentMillisSystem();
    }
  }

  @Test
  public void shouldOnlyLogEvent() {
    LogEvent logEvent = new LogEventFactory().setDomainObject(vrg).create();

    subject.handleEvent(logMock, logEvent);

    verify(logMock).info(anyString(), eq(logEvent));
    verifyZeroInteractions(logEventRepoMock);
  }

  @Test
  public void shouldPersistEventForReservation() {
    LogEvent logEvent = new LogEventFactory().setDomainObject(new ReservationFactory().create()).create();

    subject.handleEvent(logMock, logEvent);

    verify(logMock).info(anyString(), eq(logEvent));
    verify(logEventRepoMock).save(logEvent);
  }

  @Test
  public void shouldPersistEventForListOfReservation() {
    subject.logUpdateEvent(userMock, "details", new ReservationFactory().create(), new ReservationFactory().create());

    verify(logEventRepoMock, times(2)).save(any(LogEvent.class));
  }

  @Test
  public void shouldNotPersistNullDomainObject() {
    LogEvent logEvent = new LogEventFactory().setDomainObject(null).create();

    subject.handleEvent(logMock, logEvent);

    verify(logMock).info(anyString(), eq(logEvent));
    verifyZeroInteractions(logEventRepoMock);
  }

  @Test
  public void shouldRelatedItemsInList() {
    when(userMock.isSelectedNocRole()).thenReturn(true);

    List<LogEvent> logEvents = subject.logUpdateEvent(userMock, "show my the details", new InstituteFactory().create(),
        new InstituteFactory().create());

    assertThat(logEvents, hasSize(2));
    assertThat(logEvents.get(0).getEventTypeWithCorrelationId().toString(), is("Update 1/2"));
    assertThat(logEvents.get(1).getEventTypeWithCorrelationId().toString(), is("Update 2/2"));
  }

  @Test
  public void shouldFindLogEventsForAUser() {
        when(userMock.getUserGroupIds()).thenReturn(ImmutableList.of("urn:first", "urn:second", "urn:third"));
    when(virtualResourceGroupServiceMock.determineAdminGroupsForUser(userMock)).thenReturn(
        Lists.newArrayList("urn:first", "urn:second"));

    when(logEventRepoMock.findAll(any(Specification.class), any(Pageable.class))).thenReturn(
        new PageImpl<LogEvent>(Lists.newArrayList(new LogEventFactory().create())));

    List<LogEvent> logEvents = subject.findByUser(userMock, 1, 100, new Sort("userId"));

    assertThat(logEvents, hasSize(1));
  }

  @Test
  public void shouldFindLogEventsForAUserWithoutVrgs() {
    when(userMock.getUserGroupIds()).thenReturn(ImmutableList.of("urn:first"));
    when(virtualResourceGroupServiceMock.findByAdminGroup("urn:first")).thenReturn(null);

    List<LogEvent> logEvents = subject.findByUser(userMock, 1, 100, new Sort("userId"));

    assertThat(logEvents, hasSize(0));
    verifyZeroInteractions(logEventRepoMock);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailIfRoleIsNotManager() {
    BodRole userRole = BodRole.createUser();
    subject.findByAdministratorRole(userRole, 1, 100, new Sort("userId"));
  }

}