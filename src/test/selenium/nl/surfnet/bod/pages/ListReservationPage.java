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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.util.List;

import nl.surfnet.bod.domain.ReservationStatus;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ListReservationPage {

  public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd H:mm");

  private static final String PAGE = "/reservations";

  private final RemoteWebDriver driver;

  @FindBy(css = "table.zebra-striped tbody")
  private WebElement table;

  public ListReservationPage(RemoteWebDriver driver) {
    this.driver = driver;
  }

  public static ListReservationPage get(RemoteWebDriver driver) {
    ListReservationPage page = new ListReservationPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public static ListReservationPage get(RemoteWebDriver driver, String urlUnderTest) {
    driver.get(urlUnderTest + PAGE);
    return get(driver);
  }

  public String getTable() {
    return table.getText();
  }

  public void deleteByDates(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
    WebElement reservation = findReservation(startDate, endDate, startTime, endTime);

    WebElement cancelButton = reservation.findElement(By.cssSelector("input[type=image]"));
    cancelButton.click();
    driver.switchTo().alert().accept();
  }

  public void reservationShouldBe(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime,
      ReservationStatus status) {
    WebElement reservation = findReservation(startDate, endDate, startTime, endTime);

    assertThat(reservation.getText(), containsString(status.name()));
  }

  private WebElement findReservation(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
    List<WebElement> rows = table.findElements(By.tagName("tr"));

    String start = ListReservationPage.DATE_TIME_FORMATTER.print(startDate.toLocalDateTime(startTime));
    String end = ListReservationPage.DATE_TIME_FORMATTER.print(endDate.toLocalDateTime(endTime));

    for (WebElement row : rows) {
      if (row.getText().contains(start) && row.getText().contains(end)) {
        return row;
      }
    }

    throw new AssertionError(String.format("Reservation with start date '%s' and end date '%s' not found", startDate,
        endDate));
  }

}
