/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.pages.user;

import nl.surfnet.bod.pages.AbstractFormPage;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.ObjectArrays;

public class NewReservationPage extends AbstractFormPage {

  private static final String PAGE = "/reservations/create";

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("H:mm");

  @FindBy(id = "_name_id")
  private WebElement labelInput;

  @FindBy(id = "_startDate_id")
  private WebElement startDateInput;

  @FindBy(id = "_endDate_id")
  private WebElement endDateInput;

  @FindBy(id = "_startTime_id")
  private WebElement startTimeInput;

  @FindBy(id = "_endTime_id")
  private WebElement endTimeInput;

  @FindBy(id = "_bandwidth_id")
  private WebElement bandwidhtInput;

  @FindBy(id = "_startDate_error_id")
  private WebElement startDateError;

  @FindBy(id = "now_chk")
  private WebElement nowCheckbox;

  @FindBy(id = "forever_chk")
  private WebElement foreverCheckbox;

  public NewReservationPage(RemoteWebDriver driver) {
    super(driver);
  }

  public static NewReservationPage get(RemoteWebDriver driver, String urlUnderTest) {
    driver.get(urlUnderTest + PAGE);
    return get(driver);
  }

  public static NewReservationPage get(RemoteWebDriver driver) {
    NewReservationPage page = new NewReservationPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public void sendStartDate(LocalDate startDate) {
    sendDate(startDateInput, startDate);
  }

  public void sendStartTime(LocalTime startTime) {
    startTimeInput.clear();
    startTimeInput.sendKeys(TIME_FORMATTER.print(startTime));
  }

  public void sendEndDate(LocalDate endDate) {
    sendDate(endDateInput, endDate);
  }

  private void sendDate(WebElement input, LocalDate date) {
    String dateString = DATE_FORMATTER.print(date);

    System.out.println("Entering a date in " + input.getAttribute("name") + " date: " + dateString);

    input.click();

    input.sendKeys(ObjectArrays.concat(Keys.END, FluentIterable.from(Iterables.cycle(Keys.BACK_SPACE)).limit(15)
        .toArray(Keys.class)));

    System.out.println(String.format("Text before send keys 1 of %s: %s", input.getAttribute("name"),
        input.getAttribute("value")));

    input.clear();

    System.out.println(String.format("Text before send keys  2 of %s: %s", input.getAttribute("name"),
        input.getAttribute("value")));

    input.sendKeys(Keys.chord(Keys.CONTROL, "a"), dateString);

    System.out.println(String.format("Text after send keys of %s: %s", input.getAttribute("name"),
        input.getAttribute("value")));
  }

  public void sendEndTime(LocalTime endTime) {
    endTimeInput.clear();
    endTimeInput.sendKeys(TIME_FORMATTER.print(endTime));
  }

  public void sendBandwidth(String bandwidth) {
    bandwidhtInput.clear();
    bandwidhtInput.sendKeys(bandwidth);
  }

  public String getStartDateError() {
    if (startDateError == null) {
      return "";
    }

    return startDateError.getText();
  }

  public void sendLabel(String label) {
    labelInput.clear();
    labelInput.sendKeys(label);
  }

  public void clickStartNow() {
    nowCheckbox.click();
  }

  public void clickForever() {
    foreverCheckbox.click();
  }

  public void verifyIsCurrentPage() {
    super.verifyIsCurrentPage(PAGE);
  }

}
