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
package nl.surfnet.bod.pages;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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

  public void verifyReservationIsCancellable(String reservationLabel, LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
    assertTrue(isReservationCancallable(reservationLabel, startDate, endDate, startTime, endTime));
    assertFalse(isReservationCancallableDisabled(reservationLabel, startDate, endDate, startTime, endTime));
  }

  public void verifyReservationIsNotCancellable(String reservationLabel, LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
    assertFalse(isReservationCancallable(reservationLabel, startDate, endDate, startTime, endTime));
    assertTrue(isReservationCancallableDisabled(reservationLabel, startDate, endDate, startTime, endTime));
  }

  public boolean isReservationCancallableDisabled(String label, LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
    WebElement row = verifyReservationExists(label, startDate, endDate, startTime, endTime);

    try {
      return row.findElement(By.cssSelector("span.disabled-icon")).isDisplayed();
    } catch (NoSuchElementException e) {
      return false;
    }
  }

  public boolean isReservationCancallable(String label, LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
    WebElement row = verifyReservationExists(label, startDate, endDate, startTime, endTime);

    try {
      return row.findElement(By.cssSelector("a i.icon-remove")).isDisplayed();
    } catch (NoSuchElementException e) {
      return false;
    }

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
            containsString(ReservationStatus.AUTO_START.name())));

    return row;
  }
}
