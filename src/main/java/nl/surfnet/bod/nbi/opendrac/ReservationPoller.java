/**
 * Copyright (c) 2012, 2013 SURFnet BV
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
package nl.surfnet.bod.nbi.opendrac;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import java.util.Optional;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.domain.UpdatedReservationStatus;
import nl.surfnet.bod.nbi.NbiClient;
import nl.surfnet.bod.service.ReservationService;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class is responsible for monitoring changes of a
 * {@link Reservation#getStatus()}. A scheduler is started upon the call to
 * {@link #monitorStatus(Reservation)}, whenever a state change is detected the
 * new state will be updated in the specific {@link Reservation} object and will
 * be persisted. The scheduler will be cancelled afterwards.
 */
@Profile({ "opendrac", "opendrac-offline" })
@Transactional
@Component
public class ReservationPoller {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Resource private ReservationService reservationService;
  @Resource private NbiClient nbiClient;

  /**
   * The polling of reservations is scheduled every minute. This is because the
   * precision of reservations is in minutes.
   */
  @Scheduled(fixedDelay = 10 * 1000)
  public void pollReservationsThatAreAboutToChangeStatusOrShouldHaveChanged() {
    DateTime dateTime = DateTime.now().withSecondOfMinute(0).withMillisOfSecond(0);
    Collection<Reservation> reservations = reservationService.findReservationsToPoll(dateTime);

    logger.info("Found {} reservations to poll", reservations.size());
    logger.debug("The reservations {}", reservations);

    ExecutorService executor = Executors.newFixedThreadPool(5); // we are IO-bound

    try {
      for (final Reservation reservation : reservations) {
        executor.submit(new Runnable() {
          @Override
          public void run() {
            logger.debug("Checking status update for: '{}' (try {})", reservation.getId());

            Optional<ReservationStatus> currentStatus = nbiClient.getReservationStatus(reservation.getReservationId());
            logger.debug("Got back status {}", currentStatus);

            if (currentStatus.isPresent() && currentStatus.get() != reservation.getStatus()) {
              logger.info("Status change detected {} -> {} for reservation {}", new Object[] { reservation.getStatus(), currentStatus.get(), reservation.getReservationId() });
              try {
                reservationService.updateStatus(reservation.getReservationId(), UpdatedReservationStatus.forNewStatus(currentStatus.get()));
              } catch (EmptyResultDataAccessException e) {
                // Reservation was already deleted, ignore.
              }
            }
          }
        });
      }
    } finally {
      executor.shutdown();
    }
  }

}