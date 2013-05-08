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
  @Resource private ConnectionServiceRequesterV2Callback requester;

  @PostConstruct
  public void registerListener() {
    reservationEventPublisher.addListener(this);
  }

  @Override
  public void onStatusChange(ReservationStatusChangeEvent event) {

    Optional<Connection> connection = event.getReservation().getConnection();

    System.err.println("Change v2 detected.." + connection);

    if (!connection.isPresent() || connection.get().getNsiVersion() != NsiVersion.TWO) {
      return;
    }

    ConnectionV2 connectionV2 = (ConnectionV2) connection.get();

    switch (event.getNewStatus()) {
    case RESERVED:
      requester.reserveConfirmed(connectionV2, event.getNsiRequestDetails().get());
      break;
    case CANCELLED:
      requester.abortConfirmed(connectionV2, event.getNsiRequestDetails().get());
      break;
    default:
      throw new RuntimeException("ARGG not implemented..");
    }
  }

}
