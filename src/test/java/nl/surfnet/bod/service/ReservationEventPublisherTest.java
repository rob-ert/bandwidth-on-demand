/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.support.ReservationFactory;

import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.Uninterruptibles;

import static nl.surfnet.bod.domain.ReservationStatus.REQUESTED;
import static nl.surfnet.bod.domain.ReservationStatus.AUTO_START;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ReservationEventPublisherTest {

  private final ReservationEventPublisher subject = new ReservationEventPublisher();

  @Test
  public void shouldNotGiveAConccurrenModifidationException() throws InterruptedException {
    final int numberOfEvents = 100;
    final Set<Long> set = new HashSet<>();

    final ReservationListener reservationListener = new ReservationListener() {
      @Override
      public void onStatusChange(ReservationStatusChangeEvent event) {
        set.add(event.getReservation().getId());
      }
    };

    Runnable adder = new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < 100; i++) {
          subject.addListener(reservationListener);
          Uninterruptibles.sleepUninterruptibly(5, TimeUnit.MILLISECONDS);
        }
      }
    };

    Runnable notifyer = new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < numberOfEvents; i++) {
          subject.notifyListeners(new ReservationStatusChangeEvent(REQUESTED, new ReservationFactory().setStatus(
              AUTO_START).create(), Optional.<NsiRequestDetails> absent()));
        }
      }
    };

    // make sure we have at least on listener
    subject.addListener(reservationListener);

    Thread adderThread = new Thread(adder);
    Thread notifyThread = new Thread(notifyer);
    notifyThread.start();
    adderThread.start();

    adderThread.join();
    notifyThread.join();

    assertThat(set.size(), is(numberOfEvents));
  }

}
