/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.pages.user;

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

  public void reservationShouldBe(String label, ReservationStatus status) {
    WebElement reservation = findRow(label);

    waitForStatus(reservation, status);
  }

  public void reservationShouldBe(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime,
      ReservationStatus status) {
    String start = BodWebDriver.RESERVATION_DATE_TIME_FORMATTER.print(startDate.toLocalDateTime(startTime));
    String end = BodWebDriver.RESERVATION_DATE_TIME_FORMATTER.print(endDate.toLocalDateTime(endTime));

    WebElement reservation = findRow(start, end);

    waitForStatus(reservation, status);
  }

  private void waitForStatus(WebElement row, ReservationStatus status) {
    getProbes().assertTextPresent(row, status.name());
  }

  public void verifyIsCurrentPage() {
    super.verifyIsCurrentPage(PAGE);
  }
}