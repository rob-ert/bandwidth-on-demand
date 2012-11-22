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
package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import nl.surfnet.bod.domain.*;
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
import org.springframework.data.domain.Sort;

import com.google.common.collect.Iterables;

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
  private PhysicalResourceGroupService physicalResourceGroupService;

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
      assertThat(logEvent.getAdminGroup(), is(vrg.getAdminGroup()));
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
      assertThat(logEvent.getAdminGroup(), is(reservation.getAdminGroup()));
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
  public void shouldDetermineManagerAdminGroupWithoutReservation() {
    PhysicalResourceGroup prg = new PhysicalResourceGroupFactory().create();
    BodRole manager = BodRole.createManager(prg);

    when(userMock.isSelectedManagerRole()).thenReturn(true);
    when(userMock.getSelectedRole()).thenReturn(manager);

    when(physicalResourceGroupService.find(prg.getId())).thenReturn(prg);
    prg.getAdminGroup();
    String adminGroup = subject.determineAdminGroup(userMock, null);

    assertThat(adminGroup, is(prg.getAdminGroup()));
  }

  @Test
  public void shouldDetermineManagerAdminGroupWithReservation() {
    Reservation res = new ReservationFactory().create();
    PhysicalResourceGroup prg = new PhysicalResourceGroupFactory().create();
    BodRole manager = BodRole.createManager(prg);

    when(userMock.isSelectedManagerRole()).thenReturn(true);
    when(userMock.getSelectedRole()).thenReturn(manager);

    when(physicalResourceGroupService.find(prg.getId())).thenReturn(prg);
    prg.getAdminGroup();
    String adminGroup = subject.determineAdminGroup(userMock, res);

    assertThat(adminGroup, is(res.getVirtualResourceGroup().getSurfconextGroupId()));
  }

  @Test
  public void shouldDetermineNocGroupWithoutReservation() {
    when(environmentMock.getNocGroup()).thenReturn("nocje");
    when(userMock.isSelectedNocRole()).thenReturn(true);

    String adminGroup = subject.determineAdminGroup(userMock, null);

    assertThat(adminGroup, is("nocje"));
  }

  @Test
  public void shouldDetermineNocGroupWithReservation() {
    Reservation res = new ReservationFactory().create();
    when(userMock.isSelectedNocRole()).thenReturn(true);

    String adminGroup = subject.determineAdminGroup(userMock, res);

    assertThat(adminGroup, is(res.getVirtualResourceGroup().getSurfconextGroupId()));
  }

  @Test
  public void shouldDetermineReservationGroupForUser() {
    Reservation reservation = new ReservationFactory().create();
    when(userMock.isSelectedUserRole()).thenReturn(true);

    String adminGroup = subject.determineAdminGroup(userMock, reservation);

    assertThat(adminGroup, is(reservation.getVirtualResourceGroup().getSurfconextGroupId()));
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowForForUserWithoutReservation() {
    when(userMock.isSelectedUserRole()).thenReturn(true);

    subject.determineAdminGroup(userMock, null);
  }

  @Test
  public void shouldReturnNocGroupForNullUserAndNullAdminGroupFromDomainObject() {
    String nocGroup = "NOCje";
    when(environmentMock.getNocGroup()).thenReturn(nocGroup);

    String adminGroup = subject.determineAdminGroup(null, new InstituteFactory().create());

    assertThat(adminGroup, is(nocGroup));
  }

  @Test
  public void shouldRelatedItemsInList() {
    when(userMock.isSelectedNocRole()).thenReturn(true);

    List<LogEvent> logEvents = subject.logUpdateEvent(
        userMock, "show my the details",
        new InstituteFactory().create(), new InstituteFactory().create());

    assertThat(logEvents, hasSize(2));
    assertThat(logEvents.get(0).getEventTypeWithCorrelationId().toString(), is("Update 1/2"));
    assertThat(logEvents.get(1).getEventTypeWithCorrelationId().toString(), is("Update 2/2"));
  }

  @Test
  public void findByAdminGroupWithNoGroupsShoulBeEmpty() {
    List<LogEvent> groups = subject.findByAdminGroups(Collections.<String> emptyList(), 1, 100, new Sort("userId"));

    assertThat(groups.isEmpty(), is(true));
    verifyZeroInteractions(logEventRepoMock);
  }

  @Test
  public void countByAdminGroupsWithNoGroupsShouldBeZero() {
    long groupCount = subject.countByAdminGroups(Collections.<String> emptyList());

    assertThat(groupCount, is(0L));
    verifyZeroInteractions(logEventRepoMock);
  }

}