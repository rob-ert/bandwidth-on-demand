package nl.surfnet.bod.web.view;

import nl.surfnet.bod.web.manager.PhysicalPortController;
import nl.surfnet.bod.web.manager.ReservationController;
import nl.surfnet.bod.web.manager.VirtualPortController;

public class ManagerStatisticsView {

  private long virtualPortsAmount;
  private long elapsedReservationsAmount;
  private long activeReservationsAmount;
  private long comingReservationsAmount;
  private long physicalPortsAmount;

  public ManagerStatisticsView(long countPhysicalPorts, long countVirtualPorts, long countElapsedReservations,
      long countActiveReservations, long countComingReservations) {

    this.physicalPortsAmount = countPhysicalPorts;
    this.virtualPortsAmount = countVirtualPorts;
    this.elapsedReservationsAmount = countElapsedReservations;
    this.activeReservationsAmount = countActiveReservations;
    this.comingReservationsAmount = countComingReservations;
  }

  public long getVirtualPortsAmount() {
    return virtualPortsAmount;
  }

  public long getElapsedReservationsAmount() {
    return elapsedReservationsAmount;
  }

  public long getActiveReservationsAmount() {
    return activeReservationsAmount;
  }

  public long getComingReservationsAmount() {
    return comingReservationsAmount;
  }

  public long getPhysicalPortsAmount() {
    return physicalPortsAmount;
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
