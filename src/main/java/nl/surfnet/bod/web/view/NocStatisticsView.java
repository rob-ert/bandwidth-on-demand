package nl.surfnet.bod.web.view;

import nl.surfnet.bod.web.noc.PhysicalPortController;
import nl.surfnet.bod.web.noc.ReservationController;

public class NocStatisticsView {

  private long elapsedReservationsAmount;
  private long activeReservationsAmount;
  private long comingReservationsAmount;
  private long physicalPortsAmount;

  public NocStatisticsView(long countPhysicalPorts,  long countElapsedReservations,
      long countActiveReservations, long countComingReservations) {

    this.physicalPortsAmount = countPhysicalPorts;
    this.elapsedReservationsAmount = countElapsedReservations;
    this.activeReservationsAmount = countActiveReservations;
    this.comingReservationsAmount = countComingReservations;
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
    return "noc/" + PhysicalPortController.PAGE_URL;
  }
}
