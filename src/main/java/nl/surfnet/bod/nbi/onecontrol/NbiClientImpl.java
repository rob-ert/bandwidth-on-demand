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
package nl.surfnet.bod.nbi.onecontrol;

import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.nbi.NbiClient;
import nl.surfnet.bod.nbi.PortNotAvailableException;
import nl.surfnet.bod.repo.ReservationRepo;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.tmforum.mtop.msi.xsd.sir.v1.ServiceInventoryDataType.RfsList;
import org.tmforum.mtop.sb.xsd.svc.v1.ResourceFacingServiceType;

@Profile("onecontrol")
@Component
public class NbiClientImpl implements NbiClient {

  @Resource private InventoryRetrievalClient inventoryRetrievalClient;
  @Resource private ServiceComponentActivationClient serviceComponentActivationClient;
  @Resource private ReservationRepo reservationRepo;

  @Override
  public long getPhysicalPortsCount() {
    return inventoryRetrievalClient.getPhysicalPortCount();
  }

  @Override
  public List<PhysicalPort> findAllPhysicalPorts() {
    return inventoryRetrievalClient.getPhysicalPorts();
  }

  /**
   * @param autoProvision is ignored, One Control does not support this
   */
  @Override
  @Transactional(propagation=Propagation.NEVER)
  public Reservation createReservation(Reservation reservation, boolean autoProvision) {
    reservation.setReservationId(UUID.randomUUID().toString());
    Reservation savedReservation = reservationRepo.save(reservation);

    return serviceComponentActivationClient.reserve(savedReservation);
  }

  @Override
  public Optional<ReservationStatus> getReservationStatus(final String reservationId) {
    Optional<RfsList> rfsInventory = inventoryRetrievalClient.getRfsInventory();

    if (rfsInventory.isPresent()) {
      Optional<ResourceFacingServiceType> rfs = Iterables.tryFind(rfsInventory.get().getRfs(),
          new Predicate<ResourceFacingServiceType>() {
            public boolean apply(ResourceFacingServiceType rfs) {
              return MtosiUtils.getRfsName(rfs).equals(reservationId);
            }
          });

      return rfs.transform(new Function<ResourceFacingServiceType, ReservationStatus>() {
        public ReservationStatus apply(ResourceFacingServiceType rfs) {
          return MtosiUtils.mapToReservationState(rfs.getServiceState());
        }
      });
    }

    return Optional.absent();
  }

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