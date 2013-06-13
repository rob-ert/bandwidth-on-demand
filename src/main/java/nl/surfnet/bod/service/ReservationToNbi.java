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
package nl.surfnet.bod.service;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.Future;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.nbi.NbiClient;
import nl.surfnet.bod.repo.ReservationRepo;
import nl.surfnet.bod.web.security.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;

@Service
public class ReservationToNbi {

  private final Logger logger = LoggerFactory.getLogger(ReservationToNbi.class);

  @Resource private NbiClient nbiClient;
  @Resource private ReservationRepo reservationRepo;
  @Resource private ReservationEventPublisher reservationEventPublisher;
  @Resource private LogEventService logEventService;

  @Async
  public Future<Long> asyncReserve(Long reservationId, boolean autoProvision, Optional<NsiRequestDetails> requestDetails) {
    checkNotNull(reservationId);

    Reservation reservation = reservationRepo.findOne(reservationId);

    logger.debug("Requesting a new reservation from the Nbi, {} ({})", reservation, reservationId);

    checkNotNull(reservation);

    ReservationStatus orgStatus = reservation.getStatus();

    reservation = nbiClient.createReservation(reservation, autoProvision);

    reservation = reservationRepo.save(reservation);

    publishStatusChanged(reservation, orgStatus, requestDetails);

    return new AsyncResult<Long>(reservation.getId());
  }

  @Async
  public Future<Long> asyncTerminate(Long reservationId, String cancelReason, Optional<NsiRequestDetails> requestDetails) {
    Reservation reservation = reservationRepo.findOne(reservationId);
    checkNotNull(reservation);

    logger.info("Terminating reservation {}, {}", reservation, cancelReason);

    ReservationStatus orgStatus = reservation.getStatus();

    ReservationStatus reservationState = nbiClient.cancelReservation(reservation.getReservationId());

    reservation.setStatus(reservationState);
    reservation.setCancelReason(cancelReason);
    reservation = reservationRepo.save(reservation);

    publishStatusChanged(reservation, orgStatus, requestDetails);

    return new AsyncResult<Long>(reservation.getId());
  }

  @Async
  public void asyncProvision(Long reservationId, Optional<NsiRequestDetails> requestDetails) {
    Reservation reservation = reservationRepo.findOne(reservationId);

    logger.debug("Activating a reservation {}", reservation);

    boolean activateReservation = nbiClient.activateReservation(reservation.getReservationId());

    if (activateReservation) {
      ReservationStatus orgStatus = reservation.getStatus();

      reservation.setStatus(ReservationStatus.AUTO_START);
      reservationRepo.save(reservation);

      publishStatusChanged(reservation, orgStatus, requestDetails);
    }
  }

  private void publishStatusChanged(Reservation reservation, ReservationStatus originalStatus, Optional<NsiRequestDetails> nsiRequestDetails) {

    if (originalStatus == reservation.getStatus()) {
      logger.debug("No status change detected from {} to {}", originalStatus, reservation.getStatus());
      return;
    }

    logEventService.logReservationStatusChangeEvent(Security.getUserDetails(), reservation, originalStatus);

    ReservationStatusChangeEvent event = new ReservationStatusChangeEvent(originalStatus, reservation, nsiRequestDetails);

    reservationEventPublisher.notifyListeners(event);
  }

}
