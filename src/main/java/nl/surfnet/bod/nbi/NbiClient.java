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
package nl.surfnet.bod.nbi;

import java.util.List;

import nl.surfnet.bod.domain.NbiPort;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;

import com.google.common.base.Optional;

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
  List<NbiPort> findAllPorts();

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
  NbiPort findPhysicalPortByNmsPortId(String nmsPortId) throws PortNotAvailableException;

}
