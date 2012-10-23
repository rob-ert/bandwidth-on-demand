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
package nl.surfnet.bod.web.view;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.event.EntityStatistics;
import nl.surfnet.bod.web.manager.PhysicalPortController;
import nl.surfnet.bod.web.manager.ReservationController;
import nl.surfnet.bod.web.manager.VirtualPortController;

public class ManagerStatisticsView {

  private final long countPhysicalPorts;
  private final long countVirtualPorts;
  private final long elapsedReservationsAmount;
  private final long activeReservationsAmount;
  private final long comingReservationsAmount;
  private final EntityStatistics<PhysicalPort> physicalPortStatistics;
  private final EntityStatistics<VirtualPort> virtualPortStatistics;
  private final EntityStatistics<Reservation> reservationStatistics;

  public ManagerStatisticsView(long countPhysicalPorts, long countVirtualPorts, long countElapsedReservations,
      long countActiveReservations, long countComingReservations, EntityStatistics<PhysicalPort> physicalPortStats,
      EntityStatistics<VirtualPort> virtualPortStats, EntityStatistics<Reservation> reservationStats) {

    this.countPhysicalPorts = countPhysicalPorts;
    this.countVirtualPorts = countVirtualPorts;

    this.physicalPortStatistics = physicalPortStats;
    this.virtualPortStatistics = virtualPortStats;
    this.reservationStatistics = reservationStats;

    this.elapsedReservationsAmount = countElapsedReservations;
    this.activeReservationsAmount = countActiveReservations;
    this.comingReservationsAmount = countComingReservations;
  }

  public long getVirtualPortsAmount() {
    return countVirtualPorts;
  }

  public long getElapsedReservationsAmount() {
    return elapsedReservationsAmount;
  }

  public EntityStatistics<PhysicalPort> getPhysicalPortStatistics() {
    return physicalPortStatistics;
  }

  public EntityStatistics<VirtualPort> getVirtualPortStatistics() {
    return virtualPortStatistics;
  }

  public EntityStatistics<Reservation> getReservationStatistics() {
    return reservationStatistics;
  }

  public long getActiveReservationsAmount() {
    return activeReservationsAmount;
  }

  public long getComingReservationsAmount() {
    return comingReservationsAmount;
  }

  public long getPhysicalPortsAmount() {
    return countPhysicalPorts;
  }

  public String getVpsUrl() {
    return VirtualPortController.PAGE_URL;
  }

  public String getElapsedReservationsUrl() {
    return ReservationController.ELAPSED_URL;
  }

  public String getActiveReservationsUrl() {
    return ReservationController.ACTIVE_URL;
  }

  public String getComingReservationsUrl() {
    return ReservationController.COMING_URL;
  }

  public String getPpsUrl() {
    return PhysicalPortController.PAGE_URL;
  }
}
