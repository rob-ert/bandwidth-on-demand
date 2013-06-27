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

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.Uninterruptibles;

/**
 * This class is responsible for monitoring changes of a
 * {@link Reservation#getStatus()}. A scheduler is started upon the call to
 * {@link #monitorStatus(Reservation)}, whenever a state change is detected the
 * new state will be updated in the specific {@link Reservation} object and will
 * be persisted. The scheduler will be cancelled afterwards.
 */
@Component
@Transactional
public class ReservationPoller {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final ExecutorService executorService = Executors.newFixedThreadPool(10);

  @Resource private ReservationService reservationService;
  @Resource private ReservationEventPublisher reservationEventPublisher;
  @Resource private EmailSender emailSender;

  @Value("${reservation.poll.max.tries}")
  private int maxPollingTries;

  @Value("${reservation.poll.interval.milliseconds}")
  private long pollingIntervalInMillis;

  /**
   * The polling of reservations is scheduled every minute. This is because the
   * precision of reservations is in minutes.
   */
  @Scheduled(cron = "0 * * * * *")
  public void pollReservationsThatAreAboutToChangeStatusOrShouldHaveChanged() {
    DateTime dateTime = DateTime.now().withSecondOfMinute(0).withMillisOfSecond(0);
    Collection<Reservation> reservations = reservationService.findReservationsToPoll(dateTime);

    logger.info("Found {} reservations to poll", reservations.size());
    logger.debug("The reservations {}", reservations);

    for (Reservation reservation : reservations) {
      executorService.execute(new ReservationStatusChecker(reservation, maxPollingTries));
    }
  }

  public void pollReservation(Reservation reservation) {
    executorService.execute(new ReservationStatusChecker(reservation, maxPollingTries));
  }

  protected void setMaxPollingTries(int maxPollingTries) {
    this.maxPollingTries = maxPollingTries;
  }

  protected void setPollingInterval(long sleepFor, TimeUnit timeUnit) {
    this.pollingIntervalInMillis = timeUnit.toMillis(sleepFor);
  }

  ExecutorService getExecutorService() {
    return executorService;
  }

  private class ReservationStatusChecker implements Runnable {
    private final Reservation reservation;
    private final ReservationStatus startStatus;
    private final int maxPollingTries;
    private int numberOfTries;

    public ReservationStatusChecker(Reservation reservation, int maxPollingTries) {
      this.reservation = reservation;
      this.maxPollingTries = maxPollingTries;
      this.startStatus = reservation.getStatus();
    }

    @Override
    public void run() {
      try {
        ReservationStatus currentStatus = null;

        // No need to retrieve status when there is no reservationId
        while (numberOfTries < maxPollingTries && reservation.getReservationId() != null) {

          // Get the latest version of the reservation..
          Reservation reservationFresh = reservationService.find(reservation.getId());

          logger.debug("Checking status update for: '{}' (try {})", reservation.getReservationId(), numberOfTries);

          currentStatus = reservationService.getStatus(reservationFresh);
          logger.debug("Got back status {}", currentStatus);

          if (!currentStatus.equals(startStatus)) {
            logger.info("Status change detected {} -> {} for reservation {}", new Object[] { startStatus,
                currentStatus, reservationFresh.getReservationId() });

            reservationFresh = reservationService.updateStatus(reservationFresh, currentStatus);

            Optional<NsiRequestDetails> requestDetails;
            if (reservationFresh.getConnection().isPresent()) {
              requestDetails = Optional.fromNullable(reservationFresh.getConnection().get().getProvisionRequestDetails());
            } else {
              requestDetails = Optional.absent();
            }

            reservationEventPublisher.notifyListeners(new ReservationStatusChangeEvent(startStatus, reservationFresh,
                requestDetails));

            return;
          }

          numberOfTries++;
          Uninterruptibles.sleepUninterruptibly(pollingIntervalInMillis, TimeUnit.MILLISECONDS);
        }
      } catch (Exception e) {
        logger.error("The poller failed for reservation " + reservation.getId() + "/" + reservation.getReservationId(), e);
        emailSender.sendErrorMail(e);
      }
    }
  }
}
