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
package nl.surfnet.bod.nbi.opendrac;

import static java.util.stream.Collectors.toList;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;

import nl.surfnet.bod.domain.NbiPort;
import nl.surfnet.bod.domain.NbiPort.InterfaceType;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.domain.UpdatedReservationStatus;
import nl.surfnet.bod.nbi.NbiClient;
import nl.surfnet.bod.nbi.PortNotAvailableException;
import nl.surfnet.bod.repo.ReservationRepo;
import nl.surfnet.bod.service.ReservationService;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("opendrac-offline")
@Component
public class NbiOpenDracOfflineClient implements NbiClient {

  private static final Function<MockNbiPort, NbiPort> TRANSFORM_FUNCTION = new Function<MockNbiPort, NbiPort>() {
    @Override
    public NbiPort apply(MockNbiPort nbiPort) {
      NbiPort port = new NbiPort();
      port.setNmsPortId(nbiPort.getId());
      port.setSuggestedBodPortId("Mock_" + nbiPort.getName());
      port.setSuggestedNocLabel("Mock_" + nbiPort.getUserLabel().orElse(nbiPort.getName()));
      port.setVlanRequired(isVlanRequired(nbiPort.getName()));
      port.setInterfaceType(nbiPort.getInterfaceType());
      return port;
    }

    /**
     * @return true when a VlanId is required for this port. This is only the
     *         case when the name of the port contains NOT
     *         {@link NbiClient#VLAN_REQUIRED_SELECTOR}
     */
    private boolean isVlanRequired(String name) {
      return name == null ? false : !name.toLowerCase().contains(VLAN_REQUIRED_SELECTOR);
    }
  };

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Resource
  private ReservationRepo reservationRepo;
  @Resource
  private ReservationService reservationService;

  private final List<MockNbiPort> ports = new ArrayList<>();
  private final Map<String, OfflineReservation> scheduleIds = new HashMap<>();
  private long lastScheduleIdTimestamp = -1L;

  private boolean shouldSleep = true;

  public NbiOpenDracOfflineClient() {
    ports.add(new MockNbiPort("Ut002A_OME01_ETH-1-1-4", "00-1B-25-2D-DA-65_ETH-1-1-4", InterfaceType.UNI));
    ports.add(new MockNbiPort("Ut002A_OME01_ETH-1-2-4", "00-1B-25-2D-DA-65_ETH-1-2-4", InterfaceType.UNI));
    ports.add(new MockNbiPort("ETH10G-1-13-1", "00-21-E1-D6-D6-70_ETH10G-1-13-1", InterfaceType.E_NNI, "Poort 1de verdieping toren1a"));
    ports.add(new MockNbiPort("ETH10G-1-13-2", "00-21-E1-D6-D6-70_ETH10G-1-13-2", InterfaceType.UNI, "Poort 2de verdieping toren1b"));
    ports.add(new MockNbiPort("ETH-1-13-4", "00-21-E1-D6-D5-DC_ETH-1-13-4", InterfaceType.E_NNI, "Poort 3de verdieping toren1c"));
    ports.add(new MockNbiPort("ETH10G-1-13-2", "00-21-E1-D6-D5-DC_ETH10G-1-13-5", InterfaceType.UNI));
    ports.add(new MockNbiPort("ETH10G-1-5-1", "00-20-D8-DF-33-8B_ETH10G-1-5-1", InterfaceType.UNI));
    ports.add(new MockNbiPort("OME0039_OC12-1-12-1", "00-21-E1-D6-D6-70_OC12-1-12-1", InterfaceType.UNI, "Poort 4de verdieping toren1a"));
    ports.add(new MockNbiPort("WAN-1-4-102", "00-20-D8-DF-33-86_WAN-1-4-102", InterfaceType.UNI, "Poort 5de verdieping toren1a"));
    ports.add(new MockNbiPort("ETH-1-3-1", "00-21-E1-D6-D6-70_ETH-1-3-1", InterfaceType.E_NNI));
    ports.add(new MockNbiPort("ETH-1-1-1", "00-21-E1-D6-D5-DC_ETH-1-1-1", InterfaceType.UNI, "Poort 1de verdieping toren2"));
    ports.add(new MockNbiPort("ETH-1-2-3", "00-20-D8-DF-33-8B_ETH-1-2-3", InterfaceType.UNI, "Poort 2de verdieping toren2"));
    ports.add(new MockNbiPort("WAN-1-4-101", "00-20-D8-DF-33-86_WAN-1-4-101", InterfaceType.UNI));
    ports.add(new MockNbiPort("ETH-1-1-2", "00-21-E1-D6-D5-DC_ETH-1-1-2", InterfaceType.UNI));
    ports.add(new MockNbiPort("OME0039_OC12-1-12-2", "00-21-E1-D6-D6-70_OC12-1-12-2", InterfaceType.UNI, "Poort 3de verdieping toren2"));
    ports.add(new MockNbiPort("ETH-1-13-5", "00-21-E1-D6-D5-DC_ETH-1-13-5", InterfaceType.UNI, "Poort 4de verdieping toren3"));
    ports.add(new MockNbiPort("ETH10G-1-13-3", "00-21-E1-D6-D5-DC_ETH10G-1-13-3", InterfaceType.UNI, "Poort 4de verdieping toren3"));
    ports.add(new MockNbiPort("Asd001A_OME3T_ETH-1-1-1", "00-20-D8-DF-33-59_ETH-1-1-1", InterfaceType.UNI));
  }

  @PostConstruct
  public void init() {
    log.info("USING OFFLINE NBI CLIENT!");
    List<Reservation> reservations = reservationRepo.findAll();
    for (Reservation reservation : reservations) {
      this.scheduleIds.put(reservation.getReservationId(), new OfflineReservation(reservation));
    }
  }

  @Override
  public List<NbiPort> findAllPorts() {
    return ports.stream().map(TRANSFORM_FUNCTION).collect(toList());
  }

  @Override
  public long getPhysicalPortsCount() {
    return ports.size();
  }

  @Override
  public void createReservation(Reservation reservation, boolean autoProvision) {
    String scheduleId = generateScheduleId();

    reservation.setReservationId(scheduleId);

    if (reservation.getStartDateTime() == null) {
      reservation.setStartDateTime(DateTime.now());
      log.info("No startTime specified, using now: {}", reservation.getStartDateTime());
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

    scheduleIds.put(scheduleId, new OfflineReservation(status.getNewStatus(), reservation.getStartDateTime(), reservation.getEndDateTime()));

    log.warn("NBI MOCK created reservation {} with label {} and start time {}", scheduleId, reservation.getLabel(), reservation.getStartDateTime());

    // Imitate the online client..
    if (shouldSleep) {
      Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
    }

    reservationService.updateStatus(scheduleId, status);
  }

  private synchronized String generateScheduleId() {
    long timestamp;
    do {
      timestamp = System.currentTimeMillis();
    } while (lastScheduleIdTimestamp >= timestamp);
    lastScheduleIdTimestamp = timestamp;
    return "SCHEDULE-" + timestamp;
  }

  @Override
  public Optional<ReservationStatus> getReservationStatus(String scheduleId) {
    OfflineReservation reservation = scheduleIds.get(scheduleId);
    ReservationStatus status = reservation.getStatus();

    log.info("Get new status for {} with current {}", scheduleId, status);

    if (status.isTransitionState()) {
      if (status == RESERVED && reservation.getStartDateTime().isBeforeNow()) {
        status = ReservationStatus.SCHEDULED;
      }
      else if (status == AUTO_START && reservation.getStartDateTime().isBeforeNow()) {
        status = RUNNING;
      }
      else if (status == REQUESTED) {
        status = AUTO_START; // could be NOT_ACCEPTED as well..
      }
      else if (status == RUNNING && reservation.getEndDateTime().isPresent()
          && reservation.getEndDateTime().get().isBeforeNow()) {
        status = SUCCEEDED;
      }
      else if (reservation.getEndDateTime().isPresent() && reservation.getEndDateTime().get().isBeforeNow()) {
        status = PASSED_END_TIME;
      }

      scheduleIds.put(scheduleId, reservation.withStatus(status));
    }

    log.info("New status {}", status);

    return Optional.of(status);
  }

  @Override
  public void activateReservation(String scheduleId) {
    OfflineReservation reservation = scheduleIds.get(scheduleId);
    if (reservation == null) {
      return;
    }


    if (reservation.getStatus() == RESERVED) {
      scheduleIds.put(scheduleId, reservation.withStatus(AUTO_START));
      reservationService.updateStatus(scheduleId, UpdatedReservationStatus.forNewStatus(AUTO_START));
    } else if (reservation.getStatus() == SCHEDULED) {
      scheduleIds.put(scheduleId, reservation.withStatus(RUNNING));
      reservationService.updateStatus(scheduleId, UpdatedReservationStatus.forNewStatus(RUNNING));
    }
 }

  @Override
  public void cancelReservation(final String reservationId) {
    Preconditions.checkNotNull(reservationId, "reservationId is required");
    if (!scheduleIds.containsKey(reservationId)) {
      reservationService.updateStatus(reservationId, UpdatedReservationStatus.cancelFailed("unknown reservationId " + reservationId));
    } else {
      scheduleIds.put(reservationId, scheduleIds.get(reservationId).withStatus(CANCELLED));
      reservationService.updateStatus(reservationId, UpdatedReservationStatus.forNewStatus(ReservationStatus.CANCELLED));
    }
  }

  @Override
  public NbiPort findPhysicalPortByNmsPortId(final String nmsPortId) throws PortNotAvailableException {
    Preconditions.checkNotNull(Strings.emptyToNull(nmsPortId));

    try {
      return ports.stream().filter(port -> port.getId().equals(nmsPortId)).map(TRANSFORM_FUNCTION).findFirst().get();
    } catch (NoSuchElementException e) {
      throw new PortNotAvailableException(nmsPortId);
    }
  }

  public void setShouldSleep(boolean sleep) {
    this.shouldSleep = sleep;
  }

  private static final class MockNbiPort {
    private final String name;
    private final Optional<String> userLabel;
    private final String id;
    private final InterfaceType interfaceType;

    public MockNbiPort(String name, String id, InterfaceType interfaceType) {
      this(name, id, interfaceType, null);
    }

    public MockNbiPort(String name, String id, InterfaceType interfaceType, String userLabel) {
      this.name = name;
      this.id = id;
      this.interfaceType = interfaceType;
      this.userLabel = Optional.ofNullable(userLabel);
    }

    public InterfaceType getInterfaceType() {
      return interfaceType;
    }

    public String getName() {
      return name;
    }

    public String getId() {
      return id;
    }

    public Optional<String> getUserLabel() {
      return userLabel;
    }
  }

  private static class OfflineReservation {
    private final ReservationStatus status;
    private final DateTime startDateTime;
    private final Optional<DateTime> endDateTime;

    public OfflineReservation(Reservation reservation) {
      this(reservation.getStatus(), reservation.getStartDateTime(), reservation.getEndDateTime());
    }

    private OfflineReservation(ReservationStatus status, DateTime startDateTime, Optional<DateTime> endDateTime) {
      this.status = status;
      this.startDateTime = startDateTime;
      this.endDateTime = endDateTime;
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
      return new OfflineReservation(newStatus, startDateTime, endDateTime);
    }
  }
}
