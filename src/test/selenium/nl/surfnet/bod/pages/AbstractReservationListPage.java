package nl.surfnet.bod.pages;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.support.BodWebDriver;

import org.hamcrest.core.CombinableMatcher;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

public abstract class AbstractReservationListPage extends AbstractListPage {

  public AbstractReservationListPage(RemoteWebDriver driver) {
    super(driver);
  }

  public WebElement verifyReservationExists(String label, LocalDate startDate, LocalDate endDate,
      LocalTime startTime, LocalTime endTime) {

    return findReservationRow(label, startDate, endDate, startTime, endTime);
  }

  public WebElement verifyReservationExists(String label) {

    return findRow(label);
  }

  public void verifyReservationIsCancellable(String label, LocalDate startDate, LocalDate endDate, LocalTime startTime,
      LocalTime endTime) {

    WebElement row = verifyReservationExists(label, startDate, endDate, startTime, endTime);

    try {
      row.findElement(By.cssSelector("span.disabled-icon"));
      assertThat("Reservation should not contain disabled Icon", false);
    }
    catch (NoSuchElementException e) {
      // Expected
    }
  }

  public void verifyReservationIsNotCancellable(String reservationLabel, LocalDate startDate, LocalDate endDate,
      LocalTime startTime, LocalTime endTime, String toolTipText) {

    WebElement row = verifyReservationExists(reservationLabel, startDate, endDate, startTime, endTime);

    WebElement deleteElement = row.findElement(By.cssSelector("span.disabled-icon"));
    String deleteTooltip = deleteElement.getAttribute("data-original-title");

    assertThat(deleteTooltip, containsString(toolTipText));
  }

  private WebElement findReservationRow(String label, LocalDate startDate, LocalDate endDate, LocalTime startTime,
      LocalTime endTime) {

    String start = BodWebDriver.RESERVATION_DATE_TIME_FORMATTER.print(startDate.toLocalDateTime(startTime));
    String end = BodWebDriver.RESERVATION_DATE_TIME_FORMATTER.print(endDate.toLocalDateTime(endTime));

    WebElement row = findRow(label, start, end);

    assertThat(
        row.getText(),
        CombinableMatcher.<String> either(containsString(ReservationStatus.REQUESTED.name())).or(
            containsString(ReservationStatus.SCHEDULED.name())));

    return row;
  }
}
