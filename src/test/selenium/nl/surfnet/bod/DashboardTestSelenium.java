package nl.surfnet.bod;

import nl.surfnet.bod.support.TestExternalSupport;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

public class DashboardTestSelenium extends TestExternalSupport {

  @Before
  public void setUp() {
    final LocalDate startDate = LocalDate.now();
    final LocalDate endDate = startDate;
    final LocalTime startTime = LocalTime.now();
    final LocalTime endTime = startTime.plusHours(1);

    new ReservationTestSelenium().setup();

    getManagerDriver().switchToUser();
    getUserDriver().createNewReservation("Res Coming", startDate.plusDays(1), endDate.plusDays(1),
        startTime.plusHours(1), endTime.plusHours(1));

    getUserDriver().createNewReservation("Res Active, which will not become active");
  }

  @Test
  public void verifyUserStatisticLinksFromDashboard() {
    getUserDriver().verifyDashboardToVirtualPortsLink("selenium-users");
    getUserDriver().verifyDashboardToActiveReservationsLink("selenium-userst");
    getUserDriver().verifyDashboardToComingReservationsLink("selenium-users");
    getUserDriver().verifyDashboardToElapsedReservationsLink("selenium-users");
  }

  @Test
  public void verifyNocStatisticLinksFromDashboard() {
    getManagerDriver().switchToNoc();

    getNocDriver().verifyDashboardToAllocatedPhysicalPortsLink();
    getNocDriver().verifyDashboardToElapsedReservationsLink();
    getNocDriver().verifyDashboardToActiveReservationsLink();
    getNocDriver().verifyDashboardToComingReservationsLink();
    getNocDriver().verifyDashboardToUnalignedPhysicalPortsLink();
  }

  @Test
  public void verifyManagerStatisticLinksFromDashboard() {
    getNocDriver().switchToManager();

    getManagerDriver().verifyDashboardToPhysicalPortsLink();
    getManagerDriver().verifyDashboardToElapsedReservationsLink();
    getManagerDriver().verifyDashboardToActiveReservationsLink();
    getManagerDriver().verifyDashboardToComingReservationsLink();
    getManagerDriver().verifyDashboardToUnalignedPhysicalPortsLink();
  }

}
