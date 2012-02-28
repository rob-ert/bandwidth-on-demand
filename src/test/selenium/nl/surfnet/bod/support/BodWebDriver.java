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
package nl.surfnet.bod.support;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.mail.internet.MimeMessage;

import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.pages.user.EditVirtualPortPage;
import nl.surfnet.bod.pages.user.ListReservationPage;
import nl.surfnet.bod.pages.user.ListVirtualPortPage;
import nl.surfnet.bod.pages.user.NewReservationPage;
import nl.surfnet.bod.pages.user.RequestNewVirtualPortRequestPage;
import nl.surfnet.bod.pages.user.RequestNewVirtualPortSelectInstitutePage;

import org.hamcrest.core.CombinableMatcher;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.google.common.io.Files;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;

public class BodWebDriver {

  public static final String URL_UNDER_TEST = withEndingSlash(System.getProperty("selenium.test.url",
      "http://localhost:8082/bod"));

  public static final DateTimeFormatter RESERVATION_DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd H:mm");

  private FirefoxDriver driver;
  private GreenMail mailServer;

  private BodManagerWebDriver managerDriver;
  private BodNocWebDriver nocDriver;

  private static String withEndingSlash(String path) {
    return path.endsWith("/") ? path : path + "/";
  }

  public synchronized void initializeOnce() {
    if (driver == null) {
      this.driver = new FirefoxDriver();
      this.driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          if (driver != null) {
            driver.quit();
          }
        }
      });
    }

    if (mailServer == null) {
      mailServer = new GreenMail();
      mailServer.start();

      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          if (mailServer != null) {
            mailServer.stop();
          }
        }
      });
    }

    managerDriver = new BodManagerWebDriver(driver);
    nocDriver = new BodNocWebDriver(driver);
  }

  public BodManagerWebDriver getManagerDriver() {
    return managerDriver;
  }

  public BodNocWebDriver getNocDriver() {
    return nocDriver;
  }

  public void takeScreenshot(File screenshot) throws Exception {
    if (driver != null) {
      File temp = driver.getScreenshotAs(OutputType.FILE);
      Files.copy(temp, screenshot);
    }
  }

  private MimeMessage getLastEmail() {
    MimeMessage[] mails = mailServer.getReceivedMessages();
    return mails[mails.length - 1];
  }

  public void verifyLastEmailRecipient(String to) {
    MimeMessage lastMail = getLastEmail();

    assertThat(GreenMailUtil.getHeaders(lastMail), containsString("To: " + to));
  }

  public void clickLinkInLastEmail() {
    String body = GreenMailUtil.getBody(getLastEmail());

    driver.get(extractLink(body));
  }

  private String extractLink(String message) {
    Pattern pattern = Pattern.compile(".*(https?://[\\w:/\\-\\.\\?&=]+).*", Pattern.DOTALL);

    java.util.regex.Matcher matcher = pattern.matcher(message);

    if (matcher.matches()) {
      return matcher.group(1);
    }

    throw new AssertionError("Could not find link in message");
  }

  public void verifyReservationWasCreated(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime, LocalDateTime creationDateTime) {
    ListReservationPage page = ListReservationPage.get(driver);

    String start = RESERVATION_DATE_TIME_FORMATTER.print(startDate.toLocalDateTime(startTime));
    String end = RESERVATION_DATE_TIME_FORMATTER.print(endDate.toLocalDateTime(endTime));
    String creation = RESERVATION_DATE_TIME_FORMATTER.print(creationDateTime);
    
    WebElement row = page.findRow(start, end, creation);

    assertThat(
        row.getText(),
         CombinableMatcher.<String> either(containsString(ReservationStatus.REQUESTED.name())).or(
                containsString(ReservationStatus.SCHEDULED.name())));
  }

  public void verifyReservationStartDateHasError(String string) {
    NewReservationPage page = NewReservationPage.get(driver);
    String error = page.getStartDateError();

    assertThat(error, containsString(string));
  }

  public void cancelReservation(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
    ListReservationPage page = ListReservationPage.get(driver, URL_UNDER_TEST);

    page.deleteByDates(startDate, endDate, startTime, endTime);
  }

  public void verifyReservationWasCanceled(LocalDate startDate, LocalDate endDate, LocalTime startTime,
      LocalTime endTime) {
    ListReservationPage page = ListReservationPage.get(driver);

    page.reservationShouldBe(startDate, endDate, startTime, endTime, ReservationStatus.CANCELLED);
  }

  public void selectInstituteAndRequest(String institute, String message) {
    RequestNewVirtualPortSelectInstitutePage page = RequestNewVirtualPortSelectInstitutePage
        .get(driver, URL_UNDER_TEST);

    RequestNewVirtualPortRequestPage requestPage = page.selectInstitute(institute);

    requestPage.sendMessage(message);
    requestPage.sentRequest();
  }

  public void verifyRequestVirtualPortInstituteInactive(String instituteName) {
    RequestNewVirtualPortSelectInstitutePage page = RequestNewVirtualPortSelectInstitutePage
        .get(driver, URL_UNDER_TEST);

    WebElement row = page.findRow(instituteName);
    assertThat(row.getText(), containsString("Not active"));
  }

  public void createNewReservation(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
    NewReservationPage page = NewReservationPage.get(driver, BodWebDriver.URL_UNDER_TEST);

    page.sendStartDate(startDate);
    page.sendStartTime(startTime);
    page.sendEndDate(endDate);
    page.sendEndTime(endTime);
    page.sendBandwidth("500");

    page.save();
  }

  public void refreshGroups() {
    driver.get(URL_UNDER_TEST + "shibboleth/refresh");
  }

  public void editVirtualPort(String oldLabel, String newLabel) {
    ListVirtualPortPage listPage = ListVirtualPortPage.get(driver, BodWebDriver.URL_UNDER_TEST);

    EditVirtualPortPage editPage = listPage.edit(oldLabel);
    editPage.sendUserLabel(newLabel);
    editPage.save();
  }

  public void verifyVirtualPortExists(String userLabel) {
    ListVirtualPortPage listPage = ListVirtualPortPage.get(driver);

    listPage.findRow(userLabel);
  }

}
