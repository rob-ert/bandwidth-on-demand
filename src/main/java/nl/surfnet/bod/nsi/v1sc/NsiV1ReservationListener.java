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
package nl.surfnet.bod.nsi.v1sc;

import javax.annotation.Resource;

import java.util.Optional;
import com.google.common.base.Strings;

import nl.surfnet.bod.domain.ConnectionV1;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.service.ReservationListener;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.ReservationStatusChangeEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class NsiV1ReservationListener implements ReservationListener {

  private final Logger logger = LoggerFactory.getLogger(NsiV1ReservationListener.class);

  @Resource private ConnectionServiceRequesterV1 requester;
  @Resource private ReservationService reservationService;

  @Override
  public void onStatusChange(ReservationStatusChangeEvent event) {
    try {
      logger.debug("Got a reservation status change event {}", event);

      Reservation reservation = reservationService.find(event.getReservation().getId());

      if (!reservation.getConnectionV1().isPresent()) {
        logger.debug("Reservation {} was not created using NSIv1, no work to perform", reservation.getLabel());
        return;
      }

      ConnectionV1 connection = reservation.getConnectionV1().get();

      switch (event.getNewStatus()) {
      case RESERVED:
        requester.reserveConfirmed(connection, connection.getReserveRequestDetails());
        break;
      case AUTO_START:
        requester.provisionSucceeded(connection);
        break;
      case FAILED:
        handleReservationFailed(connection, requester);
        break;
      case NOT_ACCEPTED:
        Optional<String> failedReason = Optional.ofNullable(Strings.emptyToNull(event.getReservation().getFailedReason()));
        requester.reserveFailed(connection, connection.getReserveRequestDetails(), failedReason);
        break;
      case PASSED_END_TIME:
        requester.terminateReservationPassedEndTime(connection);
        break;
      case RUNNING:
        requester.provisionConfirmed(connection, connection.getProvisionRequestDetails());
        break;
      case CANCELLING:
        break;
      case CANCELLED:
        requester.terminateConfirmed(connection, Optional.ofNullable(connection.getTerminateRequestDetails()));
        break;
      case CANCEL_FAILED:
        requester.terminateFailed(connection, Optional.ofNullable(connection.getTerminateRequestDetails()));
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

  private void handleReservationFailed(ConnectionV1 connection, ConnectionServiceRequesterV1 requester) {
    switch (connection.getCurrentState()) {
    case PROVISIONED:
    case RESERVED:
      requester.executionFailed(connection);
      break;
    case RESERVING:
      requester.reserveFailed(
        connection, connection.getReserveRequestDetails(), Optional.ofNullable(connection.getReservation().getFailedReason()));
      break;
    case TERMINATING:
      requester.terminateFailed(connection, Optional.ofNullable(connection.getTerminateRequestDetails()));
      break;
    case PROVISIONING:
    case AUTO_PROVISION:
    case SCHEDULED:
      // the connection is was ready to get started but the step to running/provisioned failed
      // so send a provisionFailed
      requester.provisionFailed(connection, connection.getProvisionRequestDetails());
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
