package nl.surfnet.bod.web.view;

public class ManagerStatisticsView {

  private int virtualPortAmount;
  private int activeReservationAmount;
  private int futureReservationAmount;

  public ManagerStatisticsView(int virtualPorts, int activeReservations, int futureReservations) {
    this.virtualPortAmount = virtualPorts;
    this.activeReservationAmount = activeReservations;
    this.futureReservationAmount = futureReservations;
  }

  public int getVirtualPortAmount() {
    return virtualPortAmount;
  }

  public int getActiveReservationAmount() {
    return activeReservationAmount;
  }

  public int getFutureReservationAmount() {
    return futureReservationAmount;
  }

}
