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

import java.util.concurrent.TimeUnit;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.support.ReservationFactory;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReservationPollerTest {

  @InjectMocks
  private ReservationPoller subject;

  @Mock
  private ReservationEventPublisher reservationEventPublisherMock;

  @Mock
  private ReservationService reservationServiceMock;

  @Before
  public void makeSureWeReallyPoll() {
    subject.setMaxPollingTries(1);
  }

  @Test
  public void pollerShouldRaiseOneChangingReservation() throws InterruptedException {
    Reservation reservation = new ReservationFactory().setId(1L).setStatus(ReservationStatus.REQUESTED).create();

    when(reservationServiceMock.findReservationsToPoll(any(DateTime.class))).thenReturn(
        Lists.newArrayList(reservation));
    when(reservationServiceMock.getStatus(reservation)).thenReturn(ReservationStatus.SCHEDULED);
    when(reservationServiceMock.find(1L)).thenReturn(reservation);

    subject.pollReservationsThatAreAboutToChangeStatusOrShouldHaveChanged();

    awaitPollerReady();

    ArgumentCaptor<ReservationStatusChangeEvent> eventCaptor = ArgumentCaptor
        .forClass(ReservationStatusChangeEvent.class);

    verify(reservationEventPublisherMock).notifyListeners(eventCaptor.capture());

    assertThat(eventCaptor.getAllValues(), hasSize(1));
    assertThat(eventCaptor.getValue().getOldStatus(), is(ReservationStatus.REQUESTED));
    assertThat(eventCaptor.getValue().getReservation().getStatus(), is(ReservationStatus.SCHEDULED));
  }

  @Test
  public void pollerShouldPollMaxTimes() throws InterruptedException {
    Reservation reservation = new ReservationFactory().setId(1L).setStatus(ReservationStatus.SCHEDULED).create();
    int maxTries = 3;

    when(reservationServiceMock.findReservationsToPoll(any(DateTime.class))).thenReturn(
        Lists.newArrayList(reservation));
    when(reservationServiceMock.getStatus(reservation)).thenReturn(ReservationStatus.SCHEDULED);
    when(reservationServiceMock.find(1L)).thenReturn(reservation);

    subject.setMaxPollingTries(3);
    subject.setPollingInterval(1, TimeUnit.MILLISECONDS);
    subject.pollReservationsThatAreAboutToChangeStatusOrShouldHaveChanged();

    awaitPollerReady();

    verify(reservationServiceMock, times(maxTries)).getStatus(reservation);
  }

  @Test
  public void findReservationShouldBeForWholeMinutes() throws InterruptedException {
    ArgumentCaptor<DateTime> argument = ArgumentCaptor.forClass(DateTime.class);

    subject.pollReservationsThatAreAboutToChangeStatusOrShouldHaveChanged();

    verify(reservationServiceMock).findReservationsToPoll(argument.capture());

    assertThat(argument.getValue().getSecondOfMinute(), is(0));
    assertThat(argument.getValue().getMillisOfSecond(), is(0));
  }

  private void awaitPollerReady() throws InterruptedException {
    subject.getExecutorService().shutdown();
    boolean terminated = subject.getExecutorService().awaitTermination(1000, TimeUnit.MILLISECONDS);
    assertThat(terminated, is(true));
  }

}
