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
package nl.surfnet.bod.nsi.v2;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.ogf.schemas.nsi._2013._04.connection.types.LifecycleStateEnumType;
import org.springframework.stereotype.Component;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.ConnectionV2;
import nl.surfnet.bod.domain.NsiVersion;
import nl.surfnet.bod.service.ReservationEventPublisher;
import nl.surfnet.bod.service.ReservationListener;
import nl.surfnet.bod.service.ReservationStatusChangeEvent;

import com.google.common.base.Optional;

@Component
public class ConnectionServiceProviderListenerV2 implements ReservationListener {

  @Resource private ReservationEventPublisher reservationEventPublisher;
  @Resource private ConnectionServiceRequesterV2 requester;

  @PostConstruct
  public void registerListener() {
    reservationEventPublisher.addListener(this);
  }

  @Override
  public void onStatusChange(ReservationStatusChangeEvent event) {

    Optional<Connection> optConnection = event.getReservation().getConnection();

    if (!optConnection.isPresent() || optConnection.get().getNsiVersion() != NsiVersion.TWO) {
      return;
    }

    ConnectionV2 connection = (ConnectionV2) optConnection.get();

    switch (event.getNewStatus()) {
    case RESERVED:
      requester.reserveConfirmed(connection.getId(), event.getNsiRequestDetails().get());
      break;
    case CANCELLED:
      // What if cancel was initiated through GUI..
      if (connection.getLifecycleState() == LifecycleStateEnumType.TERMINATING) {
        requester.terminateConfirmed(connection.getId(), event.getNsiRequestDetails().get());
      } else {
        requester.abortConfirmed(connection.getId(), event.getNsiRequestDetails().get());
      }
      break;
    case AUTO_START:
      requester.provisionConfirmed(connection.getId(), event.getNsiRequestDetails().get());
      break;
    case SCHEDULED:
      // start time passed but no provision.. nothing to do..
      break;
    case TIMED_OUT:
      // TODO what todo
      break;
    case RUNNING:
      requester.dataPlaneActivated(connection.getId(), connection.getProvisionRequestDetails());
      break;
    default:
      throw new RuntimeException("ARGG not implemented..");
    }
  }

}
