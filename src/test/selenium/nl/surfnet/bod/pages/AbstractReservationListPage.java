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
package nl.surfnet.bod.pages;

import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.support.BodWebDriver;

import org.hamcrest.core.CombinableMatcher;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public abstract class AbstractReservationListPage extends AbstractListPage {

  @FindBy(id = "f_id")
  private WebElement reservationFilter;

  public AbstractReservationListPage(RemoteWebDriver driver) {
    super(driver);
  }

  public WebElement verifyReservationExists(String label, LocalDate startDate, LocalDate endDate, LocalTime startTime,
      LocalTime endTime) {

    return findReservationRow(label, startDate, endDate, startTime, endTime);
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

  public void filterReservations(String filterValue) {
    new Select(reservationFilter).selectByValue(filterValue);
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
