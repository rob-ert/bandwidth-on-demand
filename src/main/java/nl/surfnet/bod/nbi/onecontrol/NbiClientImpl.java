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

import nl.surfnet.bod.service.ReservationService;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.NbiPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.domain.UpdatedReservationStatus;
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
  @Resource private ReservationService reservationService;

  private static final String RESERVATION_ID_FORMAT = "dlp-%1$tY%1$tm%1$td%1$tH%1$tM%1$tS-%2$s";

  @Override
  public long getPhysicalPortsCount() {
    return inventoryRetrievalClient.getPhysicalPortCount();
  }

  @Override
  public List<NbiPort> findAllPorts() {
    return inventoryRetrievalClient.getPhysicalPorts();
  }

  /**
   * @param autoProvision is ignored, One Control does not support this
   */
  @Override
  @Transactional(propagation=Propagation.NEVER)
  public void createReservation(Reservation reservation, boolean autoProvision) {
    reservation.setReservationId(createReservationId());
    Reservation savedReservation = reservationRepo.save(reservation);

    UpdatedReservationStatus status = serviceComponentActivationClient.reserve(savedReservation);
    reservationService.updateStatus(savedReservation.getReservationId(), status);
  }

  @VisibleForTesting
  String createReservationId() {
    return String.format(RESERVATION_ID_FORMAT, new Date(), UUID.randomUUID().toString());
  }

  @Override
  public Optional<ReservationStatus> getReservationStatus(final String reservationId) {
    Optional<RfsList> rfsInventory = inventoryRetrievalClient.getRfsInventory();

    if (rfsInventory.isPresent()) {
      Optional<ResourceFacingServiceType> rfs = rfsInventory.get().getRfs().stream().filter(r -> MtosiUtils.getRfsName(r).equals(reservationId)).findFirst();

      if (rfs.isPresent()) {
        return MtosiUtils.mapToReservationState(rfs.get());
      }
    }

    return Optional.empty();
  }

  @Override
  public void activateReservation(String reservationId) {
    serviceComponentActivationClient.activate(reservationRepo.findByReservationId(reservationId));
    // TODO do something with the error or just ignore like before?
  }

  @Override
  public void cancelReservation(String reservationId) {
    Optional<String> error = serviceComponentActivationClient.terminate(reservationRepo.findByReservationId(reservationId));
    if (error.isPresent()) {
      reservationService.updateStatus(reservationId, UpdatedReservationStatus.cancelFailed("NBI failed to cancel reservation"));
    }
    // Successful termination status update is handled by the notification listener.
  }

  @Override
  public NbiPort findPhysicalPortByNmsPortId(final String nmsPortId) throws PortNotAvailableException {
    List<NbiPort> portList = inventoryRetrievalClient.getPhysicalPorts();

    Optional<NbiPort> port = portList.stream().filter(p -> nmsPortId.equals(p.getNmsPortId())).findFirst();

    if (port.isPresent()) {
      return port.get();
    }

    throw new PortNotAvailableException(nmsPortId);
  }

}