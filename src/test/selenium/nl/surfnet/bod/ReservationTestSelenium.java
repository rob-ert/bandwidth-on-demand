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
package nl.surfnet.bod;

import nl.surfnet.bod.support.TestExternalSupport;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;

public class ReservationTestSelenium extends TestExternalSupport {

  @Test
  public void createAReservation() {
    LocalDate startDate = LocalDate.now().plusDays(3);
    LocalDate endDate = LocalDate.now().plusDays(5);
    LocalTime startTime = LocalTime.now().plusHours(1);
    LocalTime endTime = LocalTime.now();

    getWebDriver().createNewReservation(startDate, endDate, startTime, endTime);

    getWebDriver().verifyReservationWasCreated(startDate, endDate, startTime, endTime);

    getWebDriver().cancelReservation(startDate, endDate, startTime, endTime);

    getWebDriver().verifyReservationWasCanceled(startDate, endDate, startTime, endTime);
  }

  @Test
  public void createAReservationWithWrongStartDate() {
    LocalDate startDate = LocalDate.now().minusDays(1);
    LocalDate endDate = LocalDate.now();
    LocalTime startTime = LocalTime.now();
    LocalTime endTime = LocalTime.now();

    getWebDriver().createNewReservation(startDate, endDate, startTime, endTime);

    getWebDriver().verifyReservationStartDateHasError("not be in the past");
  }

}
