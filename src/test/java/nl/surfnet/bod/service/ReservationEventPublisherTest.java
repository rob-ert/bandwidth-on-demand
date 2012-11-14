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
