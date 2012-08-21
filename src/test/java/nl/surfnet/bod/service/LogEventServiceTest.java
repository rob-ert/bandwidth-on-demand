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

import java.util.List;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.event.LogEvent;
import nl.surfnet.bod.event.LogEventType;
import nl.surfnet.bod.repo.LogEventRepo;
import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class LogEventServiceTest {

  private static final String GROUP_ID = "urn:groupie";
  private static final String LOG_DETAILS = "The reason why";

  @Mock
  private LogEventRepo repoMock;

  @Mock
  private Logger logMock;

  @InjectMocks
  private LogEventService subject;

  private RichUserDetails user = new RichUserDetailsFactory().addUserGroup(GROUP_ID).create();

  private VirtualResourceGroup vrg = new VirtualResourceGroupFactory().create();

  @Test
  public void shouldCreateLogEvent() {
    try {
      LocalDateTime now = LocalDateTime.now();
      DateTimeUtils.setCurrentMillisFixed(now.toDate().getTime());

      LogEvent logEvent = subject.createLogEvent(user, LogEventType.CREATE, vrg, LOG_DETAILS);

      assertThat(logEvent.getUserId(), is(user.getUsername()));
      assertThat(logEvent.getGroupIds(), is("[" + GROUP_ID + "]"));
      assertThat(logEvent.getEventType(), is(LogEventType.CREATE));

      assertThat(logEvent.getClassName(), is(vrg.getClass().getSimpleName()));
      assertThat(logEvent.getDetails(), is(LOG_DETAILS));

      assertThat(logEvent.getSerializedObject(), is(vrg.toString()));
      assertThat(logEvent.getCreated(), is(now));
    }
    finally {
      DateTimeUtils.setCurrentMillisSystem();
    }
  }

  @Test
  public void shouldOnlyLogEvent() {
    LogEvent logEvent = new LogEvent(user.getUsername(), GROUP_ID, LogEventType.UPDATE, vrg);

    subject.handleEvent(logMock, logEvent);

    verify(logMock).info(anyString(), eq(logEvent));
    verifyZeroInteractions(repoMock);
  }

  @Test
  public void shouldPersistEventForReservation() {
    LogEvent logEvent = new LogEvent(user.getUsername(), GROUP_ID, LogEventType.UPDATE,
        new ReservationFactory().create());

    subject.handleEvent(logMock, logEvent);

    verify(logMock).info(anyString(), eq(logEvent));
    verify(repoMock).save(logEvent);
  }

  @Test
  public void shouldPersistEventForListOfReservation() {
    List<Reservation> reservations = Lists.newArrayList(new ReservationFactory().create(),
        new ReservationFactory().create());

    LogEvent logEvent = new LogEvent(user.getUsername(), GROUP_ID, LogEventType.UPDATE, reservations);

    subject.handleEvent(logMock, logEvent);

    verify(logMock).info(anyString(), eq(logEvent));
    verify(repoMock).save(logEvent);
  }

  @Test
  public void shouldNotPersistNullDomainObject() {
    LogEvent logEvent = new LogEvent(user.getUsername(), GROUP_ID, LogEventType.UPDATE, null);

    subject.handleEvent(logMock, logEvent);

    verify(logMock).info(anyString(), eq(logEvent));
    verifyZeroInteractions(repoMock);
  }

  @Test
  public void shouldNotPersistEmptyList() {
    LogEvent logEvent = new LogEvent(user.getUsername(), GROUP_ID, LogEventType.UPDATE, Lists.newArrayList());

    subject.handleEvent(logMock, logEvent);

    verify(logMock).info(anyString(), eq(logEvent));
    verifyZeroInteractions(repoMock);
  }
}