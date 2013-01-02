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
package nl.surfnet.bod;

import nl.surfnet.bod.support.ReservationFilterViewFactory;
import nl.surfnet.bod.support.TestExternalSupport;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

public class ReservationTestSelenium extends TestExternalSupport {

  public static final String INSTITUTE_NAME = "SURFnet Netwerk";

  @Before
  public void setup() {
    getNocDriver().createNewPhysicalResourceGroup(INSTITUTE_NAME, ICT_MANAGERS_GROUP, "test@example.com");
    getNocDriver().linkPhysicalPort(NMS_PORT_ID_1, "First port", INSTITUTE_NAME);
    getNocDriver().linkPhysicalPort(NMS_PORT_ID_2, "Second port", INSTITUTE_NAME);

    getWebDriver().clickLinkInLastEmail();

    getUserDriver().requestVirtualPort("selenium-users");
    getUserDriver().selectInstituteAndRequest(INSTITUTE_NAME, 1200, "port 1");
    getWebDriver().clickLinkInLastEmail();
    getManagerDriver().createVirtualPort("First port");

    getUserDriver().requestVirtualPort("selenium-users");
    getUserDriver().selectInstituteAndRequest(INSTITUTE_NAME, 1200, "port 2");
    getWebDriver().clickLinkInLastEmail();
    getManagerDriver().createVirtualPort("Second port");
  }

  @Test
  public void createAndCancelAReservation() {
    final LocalDate startDate = LocalDate.now().plusDays(3);
    final LocalDate endDate = LocalDate.now().plusDays(5);
    final LocalTime startTime = LocalTime.now().plusHours(1);
    final LocalTime endTime = LocalTime.now();
    final String reservationLabel = "Selenium Reservation";

    getManagerDriver().switchToUser();
    getUserDriver().createNewReservation(reservationLabel, startDate, endDate, startTime, endTime);
    getUserDriver().verifyReservationWasCreated(reservationLabel, startDate, endDate, startTime, endTime);
    getUserDriver().verifyReservationIsCancellable(reservationLabel, startDate, endDate, startTime, endTime);

    getUserDriver().switchToManager(INSTITUTE_NAME);
    getManagerDriver().verifyReservationWasCreated(reservationLabel, startDate, endDate, startTime, endTime);
    getManagerDriver().verifyReservationIsCancellable(reservationLabel, startDate, endDate, startTime, endTime);
    getManagerDriver().verifyStatistics();

    getManagerDriver().switchToNoc();
    getNocDriver().verifyReservationIsCancellable(reservationLabel, startDate, endDate, startTime, endTime);
    getNocDriver().verifyStatistics();

    getManagerDriver().switchToUser();
    getUserDriver().cancelReservation(startDate, endDate, startTime, endTime);
    getUserDriver().verifyReservationWasCanceled(startDate, endDate, startTime, endTime);
  }

  @Test
  public void createReservationWithNowAndForever() {
    getManagerDriver().switchToUser();

    getUserDriver().createNewReservation("Starts now and forever");

    getUserDriver().verifyReservationWasCreated("Starts now and forever");
  }

  @Test
  public void searchAndFilterReservations() {
    final String even = "Even reservation";
    final String odd = "Odd reservation";
    final LocalDate date = LocalDate.now().plusDays(1);
    final LocalTime startTime = new LocalTime(8, 0);

    getManagerDriver().switchToUser();

    getUserDriver().createNewReservation(even, date, date, startTime, startTime.plusHours(1));
    getUserDriver().createNewReservation(odd, date, date, startTime.plusHours(2), startTime.plusHours(2 + 1));

    // Filter on this year, and no search String. All should be found.
    getUserDriver().verifyReservationByFilterAndSearch(
        String.valueOf(date.getYear()),
        "",
        even, odd);

    // Search on even
    getUserDriver().verifyReservationByFilterAndSearch(
        ReservationFilterViewFactory.COMING,
        "even",
        even);

    // Search on odd
    getUserDriver().verifyReservationByFilterAndSearch(
        ReservationFilterViewFactory.COMING,
        "odd",
        odd);
  }
}