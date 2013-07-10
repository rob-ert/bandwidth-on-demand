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

import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.ConnectionV2;
import nl.surfnet.bod.repo.ConnectionV2Repo;
import nl.surfnet.bod.service.ReservationService;

import org.joda.time.DateTime;
import org.ogf.schemas.nsi._2013._04.connection.types.ReservationStateEnumType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ConnectionV2ReserveTimeoutPoller {

  private final Logger logger = LoggerFactory.getLogger(ConnectionV2ReserveTimeoutPoller.class);

  @Resource
  private ConnectionV2Repo connectionRepo;

  @Resource
  private ConnectionServiceRequesterV2 connectionServiceRequesterV2;

  @Resource
  private ReservationService reservationService;


  @Scheduled(fixedDelay = 60 * 1000)
  public void timeOutUncommittedReservations() {
    DateTime now = DateTime.now();
    List<ConnectionV2> timedOut = connectionRepo.findByReservationStateAndReserveHeldTimeoutBefore(ReservationStateEnumType.RESERVE_HELD, now);
    logger.debug("Found {} timed out NSIv2 reservations", timedOut.size());

    for (ConnectionV2 connection : timedOut) {
      logger.info("Cancelling NSIv2 connection {} due to RESERVE_HELD timeout {}", connection.getConnectionId(), connection.getReserveHeldTimeout().orNull());
      reservationService.cancelDueToReserveTimeout(connection.getReservation());
      connectionServiceRequesterV2.reserveTimeout(connection.getId(), now);
    }
  }
}
