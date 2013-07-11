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

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.annotation.Resource;
import javax.persistence.NoResultException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.service.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Continuously polls for reservations that need to be aligned
 */
@Component
@Profile("onecontrol")
public class ReservationsAligner implements SmartLifecycle {

  private final Logger log = LoggerFactory.getLogger(ReservationsAligner.class);
  private volatile boolean started = false;

  public static String POISON_PILL = "TOXIC";

  @Resource
  private NbiOneControlClient nbiOneControlClient;

  @Resource
  private ReservationService reservationService;

  private BlockingQueue<String> reservationIds = new ArrayBlockingQueue<>(1000);

  public void align() throws InterruptedException {
    for (;;) {
      if (!doAlign())
        break;
    }
  }

  @VisibleForTesting
  boolean doAlign() {
    try {
      String reservationId = reservationIds.take();
      if (POISON_PILL.equals(reservationId)) {
        return false;
      }
      log.info("Picking up reservation {}", reservationId);
      Optional<ReservationStatus> reservationStatus = nbiOneControlClient.getReservationStatus(reservationId);
      try {
        reservationService.updateStatus(reservationId, reservationStatus.or(ReservationStatus.LOST));
      } catch (NoResultException e) {
        // apparently the reservation did not exist
        log.debug("Ignoring unknown reservation with id {}", reservationId);
      }
    } catch (Exception e) {
      log.error("Exception occurred while updating a reservation", e);
    }
    return true;
  }

  @Override
  public void start() {
    started = true;
    log.info("Starting OneControl Reservations Aligner...");

    Thread alignerThread = new Thread() {
      @Override
      public void run() {
        try {
          align();
        } catch (InterruptedException e) {
          log.debug("OneControl Reservations Aligner exiting");
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
    log.debug("Finding reservations to align");
    Collection<Reservation> reservationsToPoll = reservationService.findTransitionableReservations();
    for (Reservation reservation : reservationsToPoll) {
      log.debug("Adding reservation {} to the alignment queue", reservation.getReservationId());
      add(reservation.getReservationId());
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
    log.debug("Shutting down OneControl ReservationsAligner");
    stop();
    callback.run();
  }

  @Override
  public int getPhase() { // start last, destroy first
    return Integer.MAX_VALUE;
  }

  /**
   *
   * @throws IllegalStateException
   *           when the backing queue is full
   */
  public void add(String reservationId) {
    reservationIds.add(reservationId);
  }

}
