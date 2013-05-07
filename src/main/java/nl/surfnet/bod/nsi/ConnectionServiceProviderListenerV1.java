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
package nl.surfnet.bod.nsi;

import static com.google.common.base.Optional.fromNullable;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import nl.surfnet.bod.domain.ConnectionV1;
import nl.surfnet.bod.domain.NsiVersion;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.nsi.v1sc.ConnectionServiceRequesterVersionOneCallback;
import nl.surfnet.bod.service.ReservationEventPublisher;
import nl.surfnet.bod.service.ReservationListener;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.ReservationStatusChangeEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

@Component
public class ConnectionServiceProviderListenerV1 implements ReservationListener {

  private final Logger logger = LoggerFactory.getLogger(ConnectionServiceProviderListenerV1.class);

  @Resource private ReservationEventPublisher reservationEventPublisher;

  @Resource private ConnectionServiceRequesterVersionOneCallback requester;

  @Resource private ReservationService reservationService;

  @PostConstruct
  public void registerListener() {
    reservationEventPublisher.addListener(this);
  }

  @Override
  public void onStatusChange(ReservationStatusChangeEvent event) {
    try {
      logger.debug("Got a reservation status change event {}", event);

      Reservation reservation = reservationService.find(event.getReservation().getId());

      if (!reservation.getConnection().isPresent() || reservation.getConnection().get().getNsiVersion() != NsiVersion.ONE) {
        logger.debug("Reservation {} was not created using NSIv1, no work to perform", reservation.getLabel());
        return;
      }

      ConnectionV1 connection = (ConnectionV1) reservation.getConnection().get();

      switch (event.getNewStatus()) {
      case RESERVED:
        requester.reserveConfirmed(connection, event.getNsiRequestDetails().get());
        break;
      case AUTO_START:
        requester.provisionSucceeded(connection);
        break;
      case FAILED:
        handleReservationFailed(connection, event, requester);
        break;
      case NOT_ACCEPTED:
        Optional<String> failedReason = Optional.fromNullable(Strings.emptyToNull(event.getReservation().getFailedReason()));
        requester.reserveFailed(connection, event.getNsiRequestDetails().get(), failedReason);
        break;
      case TIMED_OUT:
        requester.terminateTimedOutReservation(connection);
        break;
      case RUNNING:
        requester.provisionConfirmed(connection, event.getNsiRequestDetails().get());
        break;
      case CANCELLED:
        requester.terminateConfirmed(connection, event.getNsiRequestDetails());
        break;
      case CANCEL_FAILED:
        requester.terminateFailed(connection, event.getNsiRequestDetails());
        break;
      case SUCCEEDED:
        requester.executionSucceeded(connection);
        break;
      case SCHEDULED:
        requester.scheduleSucceeded(connection);
        break;
      case REQUESTED:
        logger.error("Can not handle REQUESTED state. Could not happen because it is the initial state");
        break;
      }
    }
    catch (Exception e) {
      logger.error("Handeling status change failed " + event, e);
    }
  }

  private void handleReservationFailed(ConnectionV1 connection, ReservationStatusChangeEvent event, ConnectionServiceRequesterCallback<ConnectionV1> requester) {
    switch (connection.getCurrentState()) {
    case PROVISIONED:
    case RESERVED:
      requester.executionFailed(connection);
      break;
    case RESERVING:
      requester.reserveFailed(
        connection, event.getNsiRequestDetails().get(), fromNullable(connection.getReservation().getFailedReason()));
      break;
    case TERMINATING:
      requester.terminateFailed(connection, event.getNsiRequestDetails());
      break;
    case PROVISIONING:
    case AUTO_PROVISION:
    case SCHEDULED:
      // the connection is was ready to get started but the step to running/provisioned failed
      // so send a provisionFailed
      requester.provisionFailed(connection, event.getNsiRequestDetails().get());
      break;
    case UNKNOWN:
    case TERMINATED:
    case RELEASING:
    case INITIAL:
    case CLEANING:
      logger.error("Got a fail for {} but don't know how to handle.", connection.getCurrentState());
      break;
    }
  }

}
