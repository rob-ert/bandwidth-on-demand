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
package nl.surfnet.bod.nsi.v2;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.google.common.base.Optional;

import nl.surfnet.bod.domain.ConnectionV2;
import nl.surfnet.bod.service.ReservationEventPublisher;
import nl.surfnet.bod.service.ReservationListener;
import nl.surfnet.bod.service.ReservationStatusChangeEvent;

import org.ogf.schemas.nsi._2013._07.connection.types.LifecycleStateEnumType;
import org.ogf.schemas.nsi._2013._07.connection.types.ReservationStateEnumType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class NsiV2ReservationListener implements ReservationListener {

  private final Logger logger = LoggerFactory.getLogger(NsiV2ReservationListener.class);

  @Resource private ReservationEventPublisher reservationEventPublisher;
  @Resource private ConnectionServiceRequesterV2 requester;

  @PostConstruct
  public void registerListener() {
    reservationEventPublisher.addListener(this);
  }

  @Override
  public void onStatusChange(ReservationStatusChangeEvent event) {
    Optional<ConnectionV2> optConnection = event.getReservation().getConnectionV2();

    if (!optConnection.isPresent()) {
      return;
    }

    ConnectionV2 connection = optConnection.get();

    switch (event.getNewStatus()) {
    case REQUESTED:
      throw new AssertionError("Should not have got a change event going to initial state REQUESTED");
    case RESERVED:
      requester.reserveConfirmed(connection.getId(), connection.getLastReservationRequestDetails());
      break;
    case AUTO_START:
      requester.provisionConfirmed(connection.getId(), connection.getLastProvisionRequestDetails());
      break;
    case SCHEDULED:
      // start time passed but no provision.. nothing to do..
      break;
    case RUNNING:
      requester.dataPlaneActivated(connection.getId(), connection.getInitialReserveRequestDetails());
      break;
    case SUCCEEDED:
      requester.dataPlaneDeactivated(connection.getId(), connection.getInitialReserveRequestDetails());
      break;
    case CANCELLED:
      if (connection.getLifecycleState() == LifecycleStateEnumType.TERMINATING) {
        // if the connections dataplane status is active, notify that dataPlane was deactivated first
        if (connection.getDataPlaneActive()) {
          requester.dataPlaneDeactivated(connection.getId(), connection.getInitialReserveRequestDetails());
        }
        requester.terminateConfirmed(connection.getId(), connection.getLastLifecycleRequestDetails());
      } else if (connection.getReservationState() == ReservationStateEnumType.RESERVE_ABORTING){
        requester.reserveAbortConfirmed(connection.getId(), connection.getLastReservationRequestDetails());
      } else if (connection.getReservationState() == ReservationStateEnumType.RESERVE_HELD) {
        // Notify handled by ConnectionV2ReserveTimeoutPoller.
      } else {
        logger.warn("State transition to CANCELLED unhandled {}", event);
      }
      break;
    case FAILED:
      if (connection.getReservationState() == ReservationStateEnumType.RESERVE_CHECKING) {
        requester.reserveFailed(connection.getId(), connection.getLastReservationRequestDetails());
      } else if (connection.getDataPlaneActive()) {
        requester.dataPlaneError(connection.getId(), connection.getInitialReserveRequestDetails());
      } else {
        logger.warn("State transition to FAILED unhandled {}", event);
      }
      break;
    case NOT_ACCEPTED:
      requester.reserveFailed(connection.getId(), connection.getLastReservationRequestDetails());
      break;
    case PASSED_END_TIME:
      requester.reservePassedEndTime(connection.getId());
      break;
    case CANCEL_FAILED:
      if (connection.getLifecycleState() == LifecycleStateEnumType.TERMINATING) {
        requester.deactivateFailed(connection.getId(), connection.getLastLifecycleRequestDetails());
      } else {
        logger.warn("State transition to CANCEL_FAILED unhandled {}", event);
      }
      break;
    }
  }

}
