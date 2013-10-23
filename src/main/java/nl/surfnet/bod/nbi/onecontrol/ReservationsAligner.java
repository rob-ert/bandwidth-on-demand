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
package nl.surfnet.bod.nbi.onecontrol;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.annotation.Resource;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.domain.UpdatedReservationStatus;
import nl.surfnet.bod.nbi.NbiClient;
import nl.surfnet.bod.service.ReservationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Continuously polls for reservations that need to be aligned
 */
@Component
@Profile({ "onecontrol", "onecontrol-offline" })
public class ReservationsAligner implements SmartLifecycle {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReservationsAligner.class);
  private volatile boolean started = false;

  public static String POISON_PILL = "TOXIC";

  @Resource
  private NbiClient nbiClient;
  @Resource
  private ReservationService reservationService;

  private BlockingQueue<String> reservationQueue = new ArrayBlockingQueue<>(1000);

  public void align() throws InterruptedException {
    while (alignNextReservation()) {
    }
  }

  @VisibleForTesting
  boolean alignNextReservation() throws InterruptedException {
    try {
      String reservationId = reservationQueue.take();
      if (POISON_PILL.equals(reservationId)) {
        return false;
      }

      alignReservation(reservationId);
    } catch (InterruptedException e) {
      throw e;
    } catch (Exception e) {
      LOGGER.error("Exception occurred while updating a reservation", e);
    }
    return true;
  }

  @VisibleForTesting
  void alignReservation(String reservationId) {
    LOGGER.info("Aligning reservation {}", reservationId);

    Optional<ReservationStatus> reservationStatus = nbiClient.getReservationStatus(reservationId);
    if (!reservationStatus.isPresent()) {
      return;
    }

    try {
      Reservation reservation = reservationService.updateStatus(reservationId, UpdatedReservationStatus.forNewStatus(reservationStatus.get()));
      if (!reservation.isNSICreated()
          && (reservation.getStatus() == ReservationStatus.RESERVED || reservation.getStatus() == ReservationStatus.SCHEDULED)) {
        // Auto-provision if the reservation was created in the UI (not
        // through NSI).
        reservationService.provision(reservation);
      }
    } catch (EmptyResultDataAccessException e) {
      // apparently the reservation did not exist
      LOGGER.debug("Ignoring unknown reservation with id {}", reservationId);
    }
  }

  @Override
  public void start() {
    started = true;
    LOGGER.info("Starting OneControl Reservations Aligner...");

    Thread alignerThread = new Thread() {
      @Override
      public void run() {
        try {
          align();
        } catch (InterruptedException e) {
          LOGGER.debug("OneControl Reservations Aligner exiting");
          Thread.currentThread().interrupt();
        } finally {
          started = false;
        }
      }
    };
    alignerThread.setName("Onecontrol Reservation Aligner");
    alignerThread.setDaemon(true);
    alignerThread.start();
  }

  @Scheduled(fixedRate = 60000l)
  public void refreshReservationsToAlign() {
    Collection<Reservation> reservationsToPoll = reservationService.findTransitionableReservations();
    for (Reservation reservation : reservationsToPoll) {
      if (reservation.getReservationId() != null) {
        LOGGER.debug("Adding reservation {} to the alignment queue", reservation.getReservationId());
        add(reservation.getReservationId());
      }
    }
  }

  @Override
  public void stop() {
    add(POISON_PILL);
  }

  @Override
  public boolean isRunning() {
    return started;
  }

  @Override
  public boolean isAutoStartup() {
    return true;
  }

  @Override
  public void stop(Runnable callback) {
    LOGGER.debug("Shutting down OneControl ReservationsAligner");
    stop();
    callback.run();
  }

  @Override
  public int getPhase() { // start last, destroy first
    return Integer.MAX_VALUE;
  }

  /**
   * @throws IllegalStateException
   *           when the backing queue is full
   */
  public void add(String reservationId) {
    checkNotNull(reservationId);
    reservationQueue.add(reservationId);
  }

}
