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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.google.common.collect.Lists;

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
    final Reservation reservation = new ReservationFactory().setId(1L).setStatus(ReservationStatus.REQUESTED).create();

    when(reservationServiceMock.findReservationsToPoll(any(DateTime.class))).thenReturn(
        Lists.newArrayList(reservation));
    when(reservationServiceMock.getStatus(reservation)).thenReturn(ReservationStatus.AUTO_START);
    when(reservationServiceMock.find(1L)).thenReturn(reservation);
    when(reservationServiceMock.updateStatus(reservation, ReservationStatus.AUTO_START)).thenAnswer(new Answer<Reservation>() {
      @Override
      public Reservation answer(InvocationOnMock invocation) {
        reservation.setStatus(ReservationStatus.AUTO_START);
        return reservation;
      }
    });

    subject.pollReservationsThatAreAboutToChangeStatusOrShouldHaveChanged();

    awaitPollerReady();

    ArgumentCaptor<ReservationStatusChangeEvent> eventCaptor = ArgumentCaptor
        .forClass(ReservationStatusChangeEvent.class);

    verify(reservationEventPublisherMock).notifyListeners(eventCaptor.capture());

    assertThat(eventCaptor.getAllValues(), hasSize(1));
    assertThat(eventCaptor.getValue().getOldStatus(), is(ReservationStatus.REQUESTED));
    assertThat(eventCaptor.getValue().getReservation().getStatus(), is(ReservationStatus.AUTO_START));
  }

  @Test
  public void pollerShouldPollMaxTimes() throws InterruptedException {
    Reservation reservation = new ReservationFactory().setId(1L).setStatus(ReservationStatus.AUTO_START).create();
    int maxTries = 3;

    when(reservationServiceMock.findReservationsToPoll(any(DateTime.class))).thenReturn(
        Lists.newArrayList(reservation));
    when(reservationServiceMock.getStatus(reservation)).thenReturn(ReservationStatus.AUTO_START);
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
