package nl.surfnet.bod.web.view;

import nl.surfnet.bod.web.manager.PhysicalPortController;
import nl.surfnet.bod.web.manager.ReservationController;
import nl.surfnet.bod.web.manager.VirtualPortController;

public class ManagerStatisticsView {

  private int virtualPortsAmount;
  private int elapsedReservationsAmount;
  private int activeReservationsAmount;
  private int comingReservationsAmount;
  private int physicalPortsAmount;

  public ManagerStatisticsView(int virtualPorts, int elapsedReservations, int activeReservations,
      int comingReservations, int physicalPorts) {
    this.elapsedReservationsAmount = elapsedReservations;
    this.virtualPortsAmount = virtualPorts;
    this.activeReservationsAmount = activeReservations;
    this.comingReservationsAmount = comingReservations;
    this.physicalPortsAmount = physicalPorts;
  }

  public int getVirtualPortsAmount() {
    return virtualPortsAmount;
  }

  public int getElapsedReservationsAmount() {
    return elapsedReservationsAmount;
  }

  public int getActiveReservationsAmount() {
    return activeReservationsAmount;
  }

  public int getComingReservationsAmount() {
    return comingReservationsAmount;
  }

  public int getPhysicalPortsAmount() {
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
