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
package nl.surfnet.bod.pages.user;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.pages.AbstractReservationListPage;
import nl.surfnet.bod.support.BodWebDriver;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

public class ListReservationPage extends AbstractReservationListPage {

  private static final String PAGE = "/reservations";

  public ListReservationPage(RemoteWebDriver driver) {
    super(driver);
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

  public void deleteByDates(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
    String start = BodWebDriver.RESERVATION_DATE_TIME_FORMATTER.print(startDate.toLocalDateTime(startTime));
    String end = BodWebDriver.RESERVATION_DATE_TIME_FORMATTER.print(endDate.toLocalDateTime(endTime));

    delete(start, end);
  }

  public void reservationShouldBe(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime,
      ReservationStatus status) {
    String start = BodWebDriver.RESERVATION_DATE_TIME_FORMATTER.print(startDate.toLocalDateTime(startTime));
    String end = BodWebDriver.RESERVATION_DATE_TIME_FORMATTER.print(endDate.toLocalDateTime(endTime));

    WebElement reservation = findRow(start, end);

    assertThat(reservation.getText(), containsString(status.name()));
  }

}
