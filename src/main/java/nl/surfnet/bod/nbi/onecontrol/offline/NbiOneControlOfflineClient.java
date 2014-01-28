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

import static nl.surfnet.bod.domain.ReservationStatus.AUTO_START;
import static nl.surfnet.bod.domain.ReservationStatus.CANCELLED;
import static nl.surfnet.bod.domain.ReservationStatus.FAILED;
import static nl.surfnet.bod.domain.ReservationStatus.NOT_ACCEPTED;
import static nl.surfnet.bod.domain.ReservationStatus.PASSED_END_TIME;
import static nl.surfnet.bod.domain.ReservationStatus.REQUESTED;
import static nl.surfnet.bod.domain.ReservationStatus.RESERVED;
import static nl.surfnet.bod.domain.ReservationStatus.RUNNING;
import static nl.surfnet.bod.domain.ReservationStatus.SCHEDULED;
import static nl.surfnet.bod.domain.ReservationStatus.SUCCEEDED;

import com.google.common.base.Preconditions;

import com.google.common.base.Optional;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import nl.surfnet.bod.domain.NbiPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.domain.UpdatedReservationStatus;
import nl.surfnet.bod.nbi.NbiClient;
import nl.surfnet.bod.nbi.PortNotAvailableException;
import nl.surfnet.bod.nbi.onecontrol.InventoryRetrievalClient;
import nl.surfnet.bod.repo.ReservationRepo;
import nl.surfnet.bod.service.ReservationService;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("onecontrol-offline")
public class NbiOneControlOfflineClient implements NbiClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(NbiOneControlOfflineClient.class);

  private final InventoryRetrievalClient inventoryRetrievalClient;
  private final ReservationRepo reservationRepo;
  private final ReservationService reservationService;

  private final Map<String, OfflineReservation> offlineReservations = new ConcurrentHashMap<>();

  @Autowired
  public NbiOneControlOfflineClient(InventoryRetrievalClient inventoryRetrievalClient, ReservationRepo reservationRepo, ReservationService reservationService) {
    this.inventoryRetrievalClient = inventoryRetrievalClient;
    this.reservationRepo = reservationRepo;
    this.reservationService = reservationService;
  }

  @Override
  public void activateReservation(final String reservationId) {
    OfflineReservation reservation = offlineReservations.get(reservationId);
    if (reservation == null) {
      return;
    }

    if (reservation.getStatus() == RESERVED) {
      offlineReservations.put(reservationId, reservation.withStatus(AUTO_START));
      reservationService.updateStatus(reservationId, UpdatedReservationStatus.forNewStatus(AUTO_START));
    } else if (reservation.getStatus() == SCHEDULED) {
      offlineReservations.put(reservationId, reservation.withStatus(RUNNING));
      reservationService.updateStatus(reservationId, UpdatedReservationStatus.forNewStatus(RUNNING));
    }
  }

  @Override
  public void cancelReservation(final String reservationId) {
    Preconditions.checkNotNull(reservationId, "reservationId is required");
    if (!offlineReservations.containsKey(reservationId)) {
      reservationService.updateStatus(reservationId, UpdatedReservationStatus.cancelFailed("unknown reservationId " + reservationId));
    } else {
      offlineReservations.put(reservationId, offlineReservations.get(reservationId).withStatus(CANCELLED));
      reservationService.updateStatus(reservationId, UpdatedReservationStatus.forNewStatus(ReservationStatus.CANCELLED));
    }
  }

  @Override
  public long getPhysicalPortsCount() {
    return Long.valueOf(inventoryRetrievalClient.getPhysicalPortCount());
  }

  @Override
  public void createReservation(Reservation reservation, boolean autoProvision) {
    String reservationId = UUID.randomUUID().toString();
    reservation.setReservationId(reservationId);

    if (reservation.getStartDateTime() == null) {
      reservation.setStartDateTime(DateTime.now());
      LOGGER.info("No startTime specified, using now: {}", reservation.getStartDateTime());
    }
    reservation = reservationRepo.saveAndFlush(reservation);

    UpdatedReservationStatus status;
    if (StringUtils.containsIgnoreCase(reservation.getLabel(), NOT_ACCEPTED.name())) {
      status = UpdatedReservationStatus.notAccepted("label contains NOT_ACCEPTED");
    } else if (StringUtils.containsIgnoreCase(reservation.getLabel(), FAILED.name())) {
      status = UpdatedReservationStatus.failed("label contains FAILED");
    } else if (StringUtils.containsIgnoreCase(reservation.getLabel(), PASSED_END_TIME.name())) {
      status = UpdatedReservationStatus.forNewStatus(ReservationStatus.PASSED_END_TIME);
    } else if (autoProvision) {
      status = UpdatedReservationStatus.forNewStatus(ReservationStatus.AUTO_START);
    } else {
      status = UpdatedReservationStatus.forNewStatus(ReservationStatus.RESERVED);
    }

    offlineReservations.put(reservation.getReservationId(), new OfflineReservation(status.getNewStatus(), reservation.getStartDateTime(), reservation.getEndDateTime()));

    LOGGER.warn("NBI MOCK created reservation {} with label {} and start time {}", reservationId, reservation.getLabel(), reservation.getStartDateTime());

    reservationService.updateStatus(reservationId, status);
  }

  @PostConstruct
  public void init() {
    LOGGER.info("USING OFFLINE NBI CLIENT!");
    List<Reservation> reservations = reservationRepo.findAll();
    for (Reservation reservation : reservations) {
      this.offlineReservations.put(reservation.getReservationId(), new OfflineReservation(reservation));
    }
  }

  @Override
  public List<NbiPort> findAllPorts() {
    return inventoryRetrievalClient.getPhysicalPorts();
  }

  @Override
  public Optional<ReservationStatus> getReservationStatus(final String reservationId) {
    OfflineReservation reservation = offlineReservations.get(reservationId);

    ReservationStatus newStatus = reservation.getStatus();

    if (reservation.getStatus().isTransitionState()) {
      if (reservation.getStatus() == RESERVED && reservation.getStartDateTime().isBeforeNow()) {
        newStatus = ReservationStatus.SCHEDULED;
      } else if (reservation.getStatus() == AUTO_START && reservation.getStartDateTime().isBeforeNow()) {
        newStatus = RUNNING;
      } else if (reservation.getStatus() == REQUESTED) {
        newStatus = AUTO_START; // could be NOT_ACCEPTED as well..
      } else if (reservation.getStatus() == RUNNING && reservation.getEndDateTime().isPresent()
          && reservation.getEndDateTime().get().isBeforeNow()) {
        newStatus = SUCCEEDED;
      } else if (reservation.getEndDateTime().isPresent() && reservation.getEndDateTime().get().isBeforeNow()) {
        newStatus = PASSED_END_TIME;
      }
      offlineReservations.put(reservationId, reservation.withStatus(newStatus));
    }
    return Optional.of(newStatus);
  }

  @Override
  public NbiPort findPhysicalPortByNmsPortId(String nmsPortId) throws PortNotAvailableException {
    List<NbiPort> nbiPorts = this.inventoryRetrievalClient.getPhysicalPorts();
    for (NbiPort nbiPort: nbiPorts) {
      if (nbiPort.getNmsPortId().equals(nmsPortId)) {
        return nbiPort;
      }
    }
    throw new PortNotAvailableException(nmsPortId);
  }

  private static class OfflineReservation {
    private final ReservationStatus status;
    private final DateTime startDateTime;
    private final Optional<DateTime> endDateTime;

    public OfflineReservation(Reservation reservation) {
      this(reservation.getStatus(), reservation.getStartDateTime(), reservation.getEndDateTime());
    }

    private OfflineReservation(ReservationStatus status, DateTime startDateTime, DateTime endDateTime) {
      this.status = status;
      this.startDateTime = startDateTime;
      this.endDateTime = Optional.fromNullable(endDateTime);
    }

    public ReservationStatus getStatus() {
      return status;
    }

    public DateTime getStartDateTime() {
      return startDateTime;
    }

    public Optional<DateTime> getEndDateTime() {
      return endDateTime;
    }

    public OfflineReservation withStatus(ReservationStatus newStatus) {
      return new OfflineReservation(newStatus, startDateTime, endDateTime.orNull());
    }
  }
}
