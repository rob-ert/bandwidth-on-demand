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
package nl.surfnet.bod.support;

import static nl.surfnet.bod.support.BodWebDriver.URL_UNDER_TEST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.pages.user.*;

import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.Assert;
import org.openqa.selenium.remote.RemoteWebDriver;

public class BodUserWebDriver extends AbstractBoDWebDriver<DashboardPage> {

  private final RemoteWebDriver driver;

  public BodUserWebDriver(RemoteWebDriver driver) {
    this.driver = driver;
  }

  public void requestVirtualPort(String team) {
    DashboardPage page = DashboardPage.get(driver, URL_UNDER_TEST);

    page.selectInstitute(team);
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

  public void verifyReservationIsCanceled(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
    ListReservationPage page = ListReservationPage.get(driver);

    page.reservationShouldBe(startDate, endDate, startTime, endTime, ReservationStatus.CANCELLED);
  }

  public void verifyReservationIsAutoStart(String label) {
    ListReservationPage page = ListReservationPage.get(driver);

    page.reservationShouldBe(label, ReservationStatus.AUTO_START);
  }

  public void selectTeamInstituteAndRequest(String team, String institute, String userLabel, Integer bandwidth,
      String message) {
    ListVirtualPortPage page = ListVirtualPortPage.get(driver, URL_UNDER_TEST);

    RequestNewVirtualPortSelectTeamPage selectTeamPage = page.requestVirtualPort();
    selectTeamPage.selectTeam(team);

    selectInstituteAndRequest(institute, userLabel, bandwidth, message);
  }

  public void selectInstituteAndRequest(String institute, String userLabel, Integer bandwidth, String message) {
    RequestNewVirtualPortSelectInstitutePage page = RequestNewVirtualPortSelectInstitutePage.get(driver);

    RequestNewVirtualPortRequestPage requestPage = page.selectInstitute(institute);

    requestPage.sendUserLabel(userLabel);
    requestPage.sendBandwidth("" + bandwidth);
    requestPage.sendMessage(message);
    requestPage.sentRequest();
  }

  public void selectInstituteAndRequest(String institute, Integer bandwidth, String message) {
    selectInstituteAndRequest(institute, "", bandwidth, message);
  }

  public void verifyRequestVirtualPortInstituteInactive(String instituteName) {
    RequestNewVirtualPortSelectInstitutePage page = RequestNewVirtualPortSelectInstitutePage.get(driver);

    try {
      page.selectInstitute(instituteName);
      Assert.fail("Found a link for institute " + instituteName);
    }
    catch (org.openqa.selenium.NoSuchElementException e) {
      // expected
    }
  }

  public void createNewReservation(String label, LocalDateTime startDateTime, LocalDateTime endDateTime) {
    createNewReservation(label, startDateTime.toLocalDate(), endDateTime.toLocalDate(), startDateTime.toLocalTime(),
        endDateTime.toLocalTime());
  }

  public void createNewReservation(String label, LocalDate startDate, LocalDate endDate, LocalTime startTime,
      LocalTime endTime) {
    NewReservationPage page = NewReservationPage.get(driver, URL_UNDER_TEST);

    page.sendLabel(label);
    page.sendStartDate(startDate);
    page.sendStartTime(startTime);
    page.sendEndDate(endDate);
    page.sendEndTime(endTime);
    page.sendBandwidth("500");

    page.save();
  }

  public void createNewReservation(String label) {
    NewReservationPage page = NewReservationPage.get(driver, URL_UNDER_TEST);

    page.sendLabel(label);
    page.clickStartNow();
    page.clickForever();

    page.save();

  }

  public void editVirtualPort(String oldLabel, String newLabel) {
    ListVirtualPortPage listPage = ListVirtualPortPage.get(driver, URL_UNDER_TEST);

    EditVirtualPortPage editPage = listPage.edit(oldLabel);
    editPage.sendUserLabel(newLabel);
    editPage.save();
  }

  public void verifyVirtualPortExists(String... fields) {
    ListVirtualPortPage listPage = ListVirtualPortPage.get(driver, URL_UNDER_TEST);

    listPage.findRow(fields);
  }

  public void verifyLogEventExists(String... fields) {
    verifyLogEventExistsCreatedWithin(-1, fields);
  }

  public void verifyLogEventExistsCreatedWithin(int seconds, String... fields) {
    ListLogEventsPage page = ListLogEventsPage.get(driver, URL_UNDER_TEST);

    page.logEventShouldBe(DateTime.now(), seconds, fields);
  }

  public void verifyLogEventDoesNotExist(String... fields) {
    ListLogEventsPage page = ListLogEventsPage.get(driver, URL_UNDER_TEST);
    page.verifyRowWithLabelDoesNotExist(fields);
  }

  public void verifyReservationIsCancellable(String reservationLabel, LocalDate startDate, LocalDate endDate,
      LocalTime startTime, LocalTime endTime) {

    ListReservationPage page = ListReservationPage.get(driver);

    page.verifyReservationIsCancellable(reservationLabel, startDate, endDate, startTime, endTime);
  }

  public void verifyReservationWasCreated(String reservationLabel, LocalDate startDate, LocalDate endDate,
      LocalTime startTime, LocalTime endTime) {
    ListReservationPage page = ListReservationPage.get(driver);

    page.verifyReservationExists(reservationLabel, startDate, endDate, startTime, endTime);
  }

  public void verifyReservationWasCreated(String label) {
    ListReservationPage page = ListReservationPage.get(driver);

    assertThat(page.getInfoMessages(), Matchers.<String> hasItem(containsString(label)));

    page.verifyRowWithLabelExists(label);
  }

  public void verifyNotMemberOf(String teamName) {
    DashboardPage page = DashboardPage.get(driver, URL_UNDER_TEST);

    assertThat(page.getTeams(), not(hasItem(teamName)));
  }

  public void verifyMemberOf(String teamName) {
    DashboardPage page = DashboardPage.get(driver, URL_UNDER_TEST);

    assertThat(page.getTeams(), hasItem(teamName));
  }

  public void verifyReservationByFilterAndSearch(String filterValue, String searchString, String... reservationLabels) {
    ListReservationPage page = ListReservationPage.get(driver, URL_UNDER_TEST);

    page.filterReservations(filterValue);
    page.search(searchString);

    int expectedAmount = reservationLabels == null ? 0 : reservationLabels.length;
    assertThat(page.getNumberOfRows(), is(expectedAmount));

    for (String label : reservationLabels) {
      page.verifyRowWithLabelExists(label);
    }
  }

  public void verifyDashboardToComingReservationsLink(String team) {
    DashboardPage dashboardPage = DashboardPage.get(driver, URL_UNDER_TEST);

    int numberOfItems = dashboardPage.getNumberFromRowWithLinkAndClick(team, String.format(
        "reservations/filter/coming/search?search=team:%%22%s%%22", team), "Show");

    ListReservationPage reservationPage = ListReservationPage.get(driver, URL_UNDER_TEST);
    reservationPage.filterReservations(ReservationFilterViewFactory.COMING);
    reservationPage.verifyIsCurrentPage();
    assertThat(numberOfItems, is(reservationPage.getNumberOfRows()));
  }

  public void verifyDashboardToElapsedReservationsLink(String team) {
    DashboardPage dashboardPage = DashboardPage.get(driver, URL_UNDER_TEST);

    int numberOfItems = dashboardPage.getNumberFromRowWithLinkAndClick(team, String.format(
        "reservations/filter/elapsed/search?search=team:%%22%s%%22", team), "Show");

    ListReservationPage reservationPage = ListReservationPage.get(driver, URL_UNDER_TEST);
    reservationPage.filterReservations(ReservationFilterViewFactory.ELAPSED);
    reservationPage.verifyIsCurrentPage();
    assertThat(numberOfItems, is(reservationPage.getNumberOfRows()));

  }

  public void verifyDashboardToVirtualPortsLink(String team) {
    DashboardPage dashboardPage = DashboardPage.get(driver, URL_UNDER_TEST);

    int numberOfItems = dashboardPage.getNumberFromRowWithLinkAndClick(team, String.format(
        "virtualports/search?search=team:%%22%s%%22", team), "Show");
    ListVirtualPortPage vpPage = ListVirtualPortPage.get(driver, URL_UNDER_TEST);
    vpPage.verifyIsCurrentPage();
    assertThat(numberOfItems, is(vpPage.getNumberOfRows()));
  }

  public void verifyMenu() {
    DashboardPage dashboardPage = DashboardPage.get(driver, URL_UNDER_TEST);
    dashboardPage.verifyNumberOfMenuItems();
    dashboardPage.verifyMenuOverview();
    dashboardPage.verifyMenuReservations();
    dashboardPage.verifyMenuVirtualPorts();
    dashboardPage.verifyMenuLogEvents();
    dashboardPage.verifyMenuAdvanced();
    dashboardPage.verifyMenuReport();
  }

  @Override
  protected DashboardPage getDashboardPage() {
    return DashboardPage.get(driver, URL_UNDER_TEST);
  }
}
