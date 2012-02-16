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

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.mail.internet.MimeMessage;

import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.pages.manager.ListVirtualPortPage;
import nl.surfnet.bod.pages.manager.ListVirtualResourceGroupPage;
import nl.surfnet.bod.pages.manager.NewVirtualPortPage;
import nl.surfnet.bod.pages.manager.NewVirtualResourceGroupPage;
import nl.surfnet.bod.pages.noc.EditPhysicalResourceGroupPage;
import nl.surfnet.bod.pages.noc.ListPhysicalResourceGroupPage;
import nl.surfnet.bod.pages.noc.NewPhysicalResourceGroupPage;
import nl.surfnet.bod.pages.user.ListReservationPage;
import nl.surfnet.bod.pages.user.NewReservationPage;
import nl.surfnet.bod.pages.user.RequestNewVirtualPortRequestPage;
import nl.surfnet.bod.pages.user.RequestNewVirtualPortSelectInstitutePage;

import org.hamcrest.Matcher;
import org.hamcrest.core.CombinableMatcher;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.google.common.io.Files;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;

public class BodWebDriver {

  private static final String URL_UNDER_TEST = withEndingSlash(System.getProperty("selenium.test.url",
      "http://localhost:8080/bod"));

  private FirefoxDriver driver;

  private GreenMail mailServer;

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
  }

  public void takeScreenshot(File screenshot) throws Exception {
    if (driver != null) {
      File temp = driver.getScreenshotAs(OutputType.FILE);
      Files.copy(temp, screenshot);
    }
  }

  public void createNewPhysicalResourceGroup(String institute, String adminGroup, String email) throws Exception {
    NewPhysicalResourceGroupPage page = NewPhysicalResourceGroupPage.get(driver, URL_UNDER_TEST);
    page.sendInstitute(institute);
    page.sendAdminGroup(adminGroup);
    page.sendEmail(email);

    page.save();
  }

  private static String withEndingSlash(String path) {
    return path.endsWith("/") ? path : path + "/";
  }

  public void deletePhysicalGroup(String institute, String adminGroup, String email) {
    ListPhysicalResourceGroupPage page = ListPhysicalResourceGroupPage.get(driver, URL_UNDER_TEST);

    page.delete(institute, adminGroup, email);
  }

  public void verifyGroupWasCreated(String institute, String adminGroup, String email) {
    verifyGroupExists(institute, adminGroup, email, "FALSE");
  }

  public void verifyGroupExists(String institute, String adminGroup, String email, String status) {
    ListPhysicalResourceGroupPage page = ListPhysicalResourceGroupPage.get(driver, URL_UNDER_TEST);

    page.findRow(institute, adminGroup, email, status);
  }

  public void verifyGroupWasDeleted(String institute, String adminGroup, String email) {
    ListPhysicalResourceGroupPage page = ListPhysicalResourceGroupPage.get(driver);

    try {
      page.findRow(institute, adminGroup, email);
      fail("The physical resource group was not deleted");
    }
    catch (NoSuchElementException e) {
      // expected
    }
  }

  public void verifyPhysicalResourceGroupIsActive(String institute, String adminGroup, String email) {
    verifyGroupExists(institute, adminGroup, email, "TRUE");
  }

  private void assertListTable(Matcher<String> tableMatcher) {
    ListPhysicalResourceGroupPage page = ListPhysicalResourceGroupPage.get(driver);
    String row = page.getTable();

    assertThat(row, tableMatcher);
  }

  public NewVirtualResourceGroupPage createNewVirtualResourceGroup(String name) throws Exception {
    NewVirtualResourceGroupPage page = NewVirtualResourceGroupPage.get(driver, URL_UNDER_TEST);
    page.sendName(name);
    page.sendSurfConextGroupName(name);
    page.save();

    return page;
  }

  public void deleteVirtualResourceGroup(String vrgName) {
    ListVirtualResourceGroupPage page = ListVirtualResourceGroupPage.get(driver, URL_UNDER_TEST);

    page.delete(vrgName);
  }

  public void verifyVirtualResourceGroupWasCreated(String name) {
    assertVirtualResourceGroupListTable(containsString(name));
  }

  public void verifyVirtualResourceGroupWasDeleted(String vrgName) {
    assertVirtualResourceGroupListTable(not(containsString(vrgName)));
  }

  private void assertVirtualResourceGroupListTable(Matcher<String> tableMatcher) {
    ListVirtualResourceGroupPage page = ListVirtualResourceGroupPage.get(driver);
    String row = page.getTable();

    assertThat(row, tableMatcher);
  }

  public void verifyHasValidationError() {
    NewVirtualResourceGroupPage page = NewVirtualResourceGroupPage.get(driver);

    assertTrue(page.hasNameValidationError());
  }

  public void createNewVirtualPort(String name, String maxBandwidth) {
    NewVirtualPortPage page = NewVirtualPortPage.get(driver, URL_UNDER_TEST);

    page.sendName(name);
    page.sendMaxBandwidth(maxBandwidth);
    page.save();
  }

  public void verifyVirtualPortWasCreated(String name, String maxBandwidth) {
    ListVirtualPortPage page = ListVirtualPortPage.get(driver);

    String table = page.getTable();

    assertThat(table, containsString(name));
    assertThat(table, containsString(maxBandwidth));
  }

  public void createNewReservation(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
    NewReservationPage page = NewReservationPage.get(driver, URL_UNDER_TEST);

    page.sendStartDate(startDate);
    page.sendStartTime(startTime);
    page.sendEndDate(endDate);
    page.sendEndTime(endTime);
    page.sendBandwidth("500");

    page.save();
  }

  public void verifyReservationWasCreated(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
    ListReservationPage page = ListReservationPage.get(driver);

    String start = ListReservationPage.DATE_TIME_FORMATTER.print(startDate.toLocalDateTime(startTime));
    String end = ListReservationPage.DATE_TIME_FORMATTER.print(endDate.toLocalDateTime(endTime));

    String table = page.getTable();

    assertThat(
        table,
        allOf(
            CombinableMatcher.<String> either(containsString(ReservationStatus.REQUESTED.name())).or(
                containsString(ReservationStatus.SCHEDULED.name())), containsString(start), containsString(end)));
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

  public void deleteVirtualPort(String name) {
    ListVirtualPortPage page = ListVirtualPortPage.get(driver, URL_UNDER_TEST);

    page.delete(name);
  }

  public void verifyVirtualPortWasDeleted(String name) {
    ListVirtualPortPage page = ListVirtualPortPage.get(driver);

    if (page.containsAnyItems()) {
      assertListTable(not(containsString(name)));
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

  public void editPhysicalResoruceGroup(String institute, String finalEmail) {
    ListPhysicalResourceGroupPage page = ListPhysicalResourceGroupPage.get(driver);

    EditPhysicalResourceGroupPage editPage = page.edit(institute);

    editPage.sendEmail(finalEmail);

    editPage.save();
  }

  public void selectInstituteAndRequest(String institute, String message) {
    RequestNewVirtualPortSelectInstitutePage page = RequestNewVirtualPortSelectInstitutePage
        .get(driver, URL_UNDER_TEST);

    RequestNewVirtualPortRequestPage requestPage = page.selectInstitute(institute);
    requestPage.sendMessage(message);
    requestPage.sentRequest();
  }

  public void verifyNewVirtualPortHasPhysicalResourceGroup(String instituteName) {
    NewVirtualPortPage page = NewVirtualPortPage.get(driver);

    String group = page.getSelectedPhysicalResourceGroup();

    assertThat(group, is(instituteName));
  }

  public void managerDashboard() {
    driver.get(URL_UNDER_TEST + "manager");
  }

  public void verifyOnEditPhysicalResourceGroupPage(String expectedMailAdress) {
    nl.surfnet.bod.pages.manager.EditPhysicalResourceGroupPage page = nl.surfnet.bod.pages.manager.EditPhysicalResourceGroupPage
        .get(driver);

    String email = page.getEmailValue();

    assertThat(email, is(expectedMailAdress));
  }

}
