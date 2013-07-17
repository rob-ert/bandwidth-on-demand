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
package nl.surfnet.bod.nbi.onecontrol.offline;

import java.util.List;

import com.google.common.base.Optional;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.nbi.NbiClient;
import nl.surfnet.bod.nbi.PortNotAvailableException;
import nl.surfnet.bod.nbi.onecontrol.InventoryRetrievalClient;
import nl.surfnet.bod.nbi.onecontrol.ReservationsAligner;
import nl.surfnet.bod.repo.ReservationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("onecontrol-offline")
public class NbiOneControlOfflineClient implements NbiClient {

  private InventoryRetrievalClient inventoryRetrievalClient;
  private ReservationRepo reservationRepo;
  private ReservationsAligner reservationsAligner;

  @Autowired
  public NbiOneControlOfflineClient(InventoryRetrievalClient inventoryRetrievalClient, ReservationRepo reservationRepo, ReservationsAligner reservationsAligner) {
    this.inventoryRetrievalClient = inventoryRetrievalClient;
    this.reservationRepo = reservationRepo;
    this.reservationsAligner = reservationsAligner;
  }

  @Override
  public boolean activateReservation(String reservationId) {
    return false;
  }

  @Override
  public ReservationStatus cancelReservation(String scheduleId) {
    return null;
  }

  @Override
  public long getPhysicalPortsCount() {
    return new Long(inventoryRetrievalClient.getPhysicalPortCount());
  }

  @Override
  public Reservation createReservation(Reservation reservation, boolean autoProvision) {
    return null;
  }

  @Override
  public List<PhysicalPort> findAllPhysicalPorts() {
    return inventoryRetrievalClient.getPhysicalPorts();
  }

  @Override
  public Optional<ReservationStatus> getReservationStatus(String scheduleId) {
    return null;
  }

  @Override
  public PhysicalPort findPhysicalPortByNmsPortId(String nmsPortId) throws PortNotAvailableException {
    return null;
  }
}
