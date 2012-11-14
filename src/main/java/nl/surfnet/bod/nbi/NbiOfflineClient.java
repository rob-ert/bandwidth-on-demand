/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
package nl.surfnet.bod.nbi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.repo.ReservationRepo;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import static nl.surfnet.bod.domain.ReservationStatus.CANCELLED;
import static nl.surfnet.bod.domain.ReservationStatus.FAILED;
import static nl.surfnet.bod.domain.ReservationStatus.NOT_ACCEPTED;
import static nl.surfnet.bod.domain.ReservationStatus.RESERVED;
import static nl.surfnet.bod.domain.ReservationStatus.AUTO_START;
import static nl.surfnet.bod.domain.ReservationStatus.TIMED_OUT;;

class NbiOfflineClient implements NbiClient {

  private static final Function<NbiPort, PhysicalPort> TRANSFORM_FUNCTION = new Function<NbiPort, PhysicalPort>() {
    @Override
    public PhysicalPort apply(NbiPort nbiPort) {
      PhysicalPort physicalPort = new PhysicalPort(isVlanRequired(nbiPort.getName()));
      physicalPort.setNmsPortId(nbiPort.getId());
      physicalPort.setBodPortId("Mock_" + nbiPort.getName());
      physicalPort.setNocLabel("Mock_" + nbiPort.getUserLabel().or(nbiPort.getName()));

      return physicalPort;
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
  private final Random random = new Random();

  @Resource
  private ReservationRepo reservationRepo;

  private final List<NbiPort> ports = Lists.newArrayList();
  private final Map<String, ReservationStatus> scheduleIds = new HashMap<String, ReservationStatus>();

  public NbiOfflineClient() {
    ports.add(new NbiPort("Ut002A_OME01_ETH-1-1-4", "00-1B-25-2D-DA-65_ETH-1-1-4"));
    ports.add(new NbiPort("Ut002A_OME01_ETH-1-2-4", "00-1B-25-2D-DA-65_ETH-1-2-4"));
    ports.add(new NbiPort("ETH10G-1-13-1", "00-21-E1-D6-D6-70_ETH10G-1-13-1", "Poort 1de verdieping toren1a"));
    ports.add(new NbiPort("ETH10G-1-13-2", "00-21-E1-D6-D6-70_ETH10G-1-13-2", "Poort 2de verdieping toren1b"));
    ports.add(new NbiPort("ETH-1-13-4", "00-21-E1-D6-D5-DC_ETH-1-13-4", "Poort 3de verdieping toren1c"));
    ports.add(new NbiPort("ETH10G-1-13-2", "00-21-E1-D6-D5-DC_ETH10G-1-13-5"));
    ports.add(new NbiPort("ETH10G-1-5-1", "00-20-D8-DF-33-8B_ETH10G-1-5-1"));
    ports.add(new NbiPort("OME0039_OC12-1-12-1", "00-21-E1-D6-D6-70_OC12-1-12-1", "Poort 4de verdieping toren1a"));
    ports.add(new NbiPort("WAN-1-4-102", "00-20-D8-DF-33-86_WAN-1-4-102", "Poort 5de verdieping toren1a"));
    ports.add(new NbiPort("ETH-1-3-1", "00-21-E1-D6-D6-70_ETH-1-3-1"));
    ports.add(new NbiPort("ETH-1-1-1", "00-21-E1-D6-D5-DC_ETH-1-1-1", "Poort 1de verdieping toren2"));
    ports.add(new NbiPort("ETH-1-2-3", "00-20-D8-DF-33-8B_ETH-1-2-3", "Poort 2de verdieping toren2"));
    ports.add(new NbiPort("WAN-1-4-101", "00-20-D8-DF-33-86_WAN-1-4-101"));
    ports.add(new NbiPort("ETH-1-1-2", "00-21-E1-D6-D5-DC_ETH-1-1-2"));
    ports.add(new NbiPort("OME0039_OC12-1-12-2", "00-21-E1-D6-D6-70_OC12-1-12-2", "Poort 3de verdieping toren2"));
    ports.add(new NbiPort("ETH-1-13-5", "00-21-E1-D6-D5-DC_ETH-1-13-5", "Poort 4de verdieping toren3"));
    ports.add(new NbiPort("ETH10G-1-13-3", "00-21-E1-D6-D5-DC_ETH10G-1-13-3", "Poort 4de verdieping toren3"));
    ports.add(new NbiPort("Asd001A_OME3T_ETH-1-1-1", "00-20-D8-DF-33-59_ETH-1-1-1"));
  }

  @SuppressWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "Called by IoC container")
  @PostConstruct
  private void init() {
    log.info("USING OFFLINE NBI CLIENT!");
    List<Reservation> reservations = reservationRepo.findAll();
    for (Reservation reservation : reservations) {
      this.scheduleIds.put(reservation.getReservationId(), reservation.getStatus());
    }
  }

  @Override
  public List<PhysicalPort> findAllPhysicalPorts() {
    return Lists.newArrayList(Lists.transform(ports, TRANSFORM_FUNCTION));
  }

  @Override
  public long getPhysicalPortsCount() {
    return ports.size();
  }

  @Override
  public Reservation createReservation(Reservation reservation, boolean autoProvision) {
    final String scheduleId = "SCHEDULE-" + System.currentTimeMillis();

    if (reservation.getStartDateTime() == null) {
      reservation.setStartDateTime(DateTime.now());
      log.info("No startTime specified, using now: {}", reservation.getStartDateTime());
    }

    if (StringUtils.containsIgnoreCase(reservation.getLabel(), NOT_ACCEPTED.name())) {
      reservation.setStatus(NOT_ACCEPTED);
    }
    else if (StringUtils.containsIgnoreCase(reservation.getLabel(), FAILED.name())) {
      reservation.setStatus(FAILED);
    }
    else if (StringUtils.containsIgnoreCase(reservation.getLabel(), TIMED_OUT.name())) {
      reservation.setStatus(TIMED_OUT);
    }
    else if (autoProvision) {
      reservation.setStatus(AUTO_START);
    }
    else {
      reservation.setStatus(RESERVED);
    }

    reservation.setReservationId(scheduleId);

    scheduleIds.put(scheduleId, reservation.getStatus());

    log.warn("Created reservation using MOCK with id: {}", reservation.getReservationId());

    // Imitate the online client..
    Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);

    return reservation;
  }

  @Override
  public Optional<ReservationStatus> getReservationStatus(String scheduleId) {
    ReservationStatus currentStatus = scheduleIds.get(scheduleId);

    if (currentStatus.isTransitionState() && random.nextInt(20) < 2) {
      ReservationStatus nextStatus = getNextStatus(currentStatus);
      scheduleIds.put(scheduleId, nextStatus);
      currentStatus = nextStatus;
    }

    return Optional.of(currentStatus);
  }

  private ReservationStatus getNextStatus(ReservationStatus status) {

    if (status.isEndState()) {
      return status;
    }
    else {
      switch (status) {
      case REQUESTED:
        return ReservationStatus.AUTO_START;
      case AUTO_START:
        return ReservationStatus.RUNNING;
      case RUNNING:
        return ReservationStatus.SUCCEEDED;
      default:
        return status;
      }
    }
  }

  @Override
  public boolean activateReservation(String reservationId) {
    return true;
  }

  @Override
  public ReservationStatus cancelReservation(String scheduleId) {
    scheduleIds.put(scheduleId, CANCELLED);
    return CANCELLED;
  }

  @Override
  public PhysicalPort findPhysicalPortByNmsPortId(final String nmsPortId) {
    Preconditions.checkNotNull(Strings.emptyToNull(nmsPortId));

    return Iterables.getOnlyElement(Iterables.transform(Iterables.filter(ports, new Predicate<NbiPort>() {
      @Override
      public boolean apply(NbiPort nbiPort) {
        return nbiPort.getId().equals(nmsPortId);
      }
    }), TRANSFORM_FUNCTION));
  }

  private static final class NbiPort {
    private final String name;
    private final Optional<String> userLabel;
    private final String id;

    public NbiPort(String name, String id) {
      this(name, id, null);
    }

    public NbiPort(String name, String id, String userLabel) {
      this.name = name;
      this.id = id;
      this.userLabel = Optional.fromNullable(userLabel);
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

}
