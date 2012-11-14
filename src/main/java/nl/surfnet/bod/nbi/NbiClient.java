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

import java.util.List;

import com.google.common.base.Optional;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;

public interface NbiClient {

  String VLAN_REQUIRED_SELECTOR = "ome";

  boolean activateReservation(final String reservationId);

  ReservationStatus cancelReservation(String scheduleId);

  /**
   *
   * @return the amount of all available {@link PhysicalPort}'s
   */
  long getPhysicalPortsCount();

  /**
   *
   * @param reservation
   * @param autoProvision, when true the reservation is automatically started
   * @return the reservation id returned from the underlying NMS
   */
  Reservation createReservation(Reservation reservation, boolean autoProvision);

  /**
   *
   * @return all available {@link PhysicalPort}'s
   */
  List<PhysicalPort> findAllPhysicalPorts();

  /**
   * Retrieve the status of a reservation.
   *
   * @param scheduleId
   *          the id of the schedule of interest
   * @return status of the reservation if available otherwise absent
   */
  Optional<ReservationStatus> getReservationStatus(String scheduleId);

  /**
   *
   * @param name
   * @return a {@link PhysicalPort} identified by a certain name, in OpenDRAC's
   *         case the network elements pk
   */
  PhysicalPort findPhysicalPortByNmsPortId(String nmsPortId);

}
