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
import nl.surfnet.bod.nbi.mtosi.MtosiUtils;
import nl.surfnet.bod.nbi.mtosi.ServiceComponentActivationClient;
import nl.surfnet.bod.repo.ReservationRepo;

import org.tmforum.mtop.msi.xsd.sir.v1.ServiceInventoryDataType.RfsList;
import org.tmforum.mtop.sb.xsd.svc.v1.ResourceFacingServiceType;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class NbiMtosiClient implements NbiClient {

  @Resource
  private InventoryRetrievalClient inventoryRetrievalClient;

  @Resource
  private ServiceComponentActivationClient serviceComponentActivationClient;

  @Resource
  private ReservationRepo reservationRepo;

  @Override
  public boolean activateReservation(String reservationId) {
    return serviceComponentActivationClient.activate(reservationRepo.findByReservationId(reservationId));
  }

  @Override
  public ReservationStatus cancelReservation(String reservationId) {
    serviceComponentActivationClient.terminate(reservationRepo.findByReservationId(reservationId));
    return ReservationStatus.CANCELLED;
  }

  @Override
  public long getPhysicalPortsCount() {
    return inventoryRetrievalClient.getPhysicalPortCount();
  }

  @Override
  public Reservation createReservation(final Reservation reservation, boolean autoProvision) {
    reservation.setReservationId(UUID.randomUUID().toString());
    Reservation savedReservation = reservationRepo.saveAndFlush(reservation);

    return serviceComponentActivationClient.reserve(savedReservation, autoProvision);
  }

  @Override
  public List<PhysicalPort> findAllPhysicalPorts() {
    return inventoryRetrievalClient.getPhysicalPorts();
  }

  @Override
  public Optional<ReservationStatus> getReservationStatus(String reservationId) {
    // The status should ideally be determined by receiving events from 1C

    RfsList rfsInventory = inventoryRetrievalClient.getCachedRfsInventory();
    for (ResourceFacingServiceType rfsType : rfsInventory.getRfs()) {

      if (MtosiUtils.findRdnValue("RFS", rfsType.getName().getValue()).get().equals(reservationId)) {
        ReservationStatus status = MtosiUtils.mapToReservationState(rfsType.getServiceState());
        return Optional.of(status);
      }
    }

    return Optional.<ReservationStatus> absent();
  }

  @Override
  public PhysicalPort findPhysicalPortByNmsPortId(final String nmsPortId) throws PortNotAvailableException {

    List<PhysicalPort> portList = inventoryRetrievalClient.getPhysicalPorts();

    Optional<PhysicalPort> port = Iterables.tryFind(portList, new Predicate<PhysicalPort>() {
      @Override
      public boolean apply(PhysicalPort physicalPort) {
        return nmsPortId.equals(physicalPort.getNmsPortId());
      }
    });

    if (port.isPresent()) {
      return port.get();
    }

    throw new PortNotAvailableException(nmsPortId);
  }
}