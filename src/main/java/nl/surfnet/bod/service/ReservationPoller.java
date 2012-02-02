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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.repo.ReservationRepo;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.util.concurrent.Uninterruptibles;

/**
 * This class is responsible for monitoring changes of a
 * {@link Reservation#getStatus()}. A scheduler is started upon the call to
 * {@link #monitorStatus(Reservation)}, whenever a state change is detected the
 * new state will be updated in the specific {@link Reservation} object and will
 * be persisted. The scheduler will be cancelled afterwards.
 * 
 * @author Franky
 * 
 */
@Component
@Transactional
public class ReservationPoller {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private ReservationService reservationService;

  @Autowired
  private ReservationRepo reservationRepo;

  @Autowired
  @Qualifier("nbiService")
  private NbiService nbiService;

  @Autowired
  private ReservationEventPublisher reservationEventPublisher;

  private ExecutorService executorService = Executors.newFixedThreadPool(10);

  @Value("${reservation.poll.max.tries}")
  private Integer maxPollingTries;

  /**
   * The polling of reservations is scheduled every minute. This is because the
   * precision of reservations is in minutes.
   */
  @Scheduled(cron = "0 * * * * *")
  public void pollReservations() {
    LocalDateTime dateTime = LocalDateTime.now().withSecondOfMinute(0).withMillisOfSecond(0);
    List<Reservation> reservations = reservationService.findReservationsToPoll(dateTime);

    for (Reservation reservation : reservations) {
      executorService.submit(new ReservationStatusChecker(reservation));
    }
  }

  
  private class ReservationStatusChecker implements Runnable {
    private final Reservation reservation;
    private final ReservationStatus startStatus;
    private int numberOfTries;

    public ReservationStatusChecker(Reservation reservation) {
      this.reservation = reservation;
      this.startStatus = reservation.getStatus();
    }

    @Override
    public void run() {
      ReservationStatus currentStatus = null;

      try {
        while (numberOfTries < maxPollingTries) {
          log.info("Checking status update for: '{}'", reservation.getReservationId());

          currentStatus = reservationService.getStatus(reservation);
          numberOfTries++;

          if (startStatus != currentStatus) {
            reservation.setStatus(currentStatus);
            reservationService.update(reservation);

            return;
          }
          Uninterruptibles.sleepUninterruptibly(10, TimeUnit.SECONDS);
        }
      }
      finally {
        ReservationStatusChangeEvent changeEvent = new ReservationStatusChangeEvent(currentStatus, reservation);
        reservationEventPublisher.notifyListeners(changeEvent);
      }
    }
  }
}
