package nl.surfnet.bod;

import nl.surfnet.bod.support.TestExternalSupport;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

public class DashboardSeleniumTest extends TestExternalSupport {

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

    getUserDriver().createNewReservation("Res Active", startDate, endDate, startTime.minusHours(1),
        endTime.plusHours(1));

    getUserDriver().createNewReservation("Res Elapsed", startDate.minusDays(1), endDate.minusDays(1), startTime,
        endTime);
  }

  @Test
  public void verifyStaticLinksFromDashboard() {

    getUserDriver().verifyDashboardToComingReservationsLink("Res Coming");
//    getUserDriver().verifyDashboardToElapsedReservationsLink("Res Elapsed");
//    getUserDriver().verifyDashboardToActiveReservationsLink("Res Active");

  }

}
