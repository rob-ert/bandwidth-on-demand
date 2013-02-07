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
package nl.surfnet.bod.nbi;

import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.nbi.mtosi.InventoryRetrievalClient;
import nl.surfnet.bod.nbi.mtosi.ServiceComponentActivationClient;
import nl.surfnet.bod.repo.ReservationRepo;

import com.google.common.base.Optional;

public class NbiMtosiClient implements NbiClient {

  @Resource
  private InventoryRetrievalClient inventoryRetrievalClient;

  @Resource
  private ServiceComponentActivationClient serviceComponentActivationClient;

  @Resource
  private ReservationRepo reservationRepo;

  @Override
  public boolean activateReservation(String reservationId) {
    throw new UnsupportedOperationException("Not implemented yet..");
  }

  @Override
  public ReservationStatus cancelReservation(String scheduleId) {
    throw new UnsupportedOperationException("Not implemented yet..");
  }

  @Override
  public long getPhysicalPortsCount() {
    return inventoryRetrievalClient.getUnallocatedMtosiPortCount();
  }

  @Override
  public Reservation createReservation(final Reservation reservation, boolean autoProvision) {
    // Generate id
    reservation.setReservationId(UUID.randomUUID().toString());
    reservationRepo.saveAndFlush(reservation);

    serviceComponentActivationClient.reserve(reservation, autoProvision);

    return reservation;
  }

  @Override
  public List<PhysicalPort> findAllPhysicalPorts() {
    return inventoryRetrievalClient.getUnallocatedPorts();
  }

  @Override
  public Optional<ReservationStatus> getReservationStatus(String scheduleId) {
    throw new UnsupportedOperationException("Not implemented yet..");
  }

  @Override
  public PhysicalPort findPhysicalPortByNmsPortId(String nmsPortId) throws PortNotAvailableException {
    throw new PortNotAvailableException(nmsPortId);
  }

}
