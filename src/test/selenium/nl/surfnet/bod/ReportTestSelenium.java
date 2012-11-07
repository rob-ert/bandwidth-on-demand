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
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ReportTestSelenium extends TestExternalSupport {

  private final LocalDateTime reportStart = LocalDateTime.now().plusDays(1).plusHours(1);
  private final LocalDateTime reportEnd = reportStart.plusDays(1).plusHours(1);

  @Before
  public void setUp() {
    final LocalDate startDate = reportStart.toLocalDate();
    final LocalDate endDate = reportEnd.toLocalDate();
    final LocalTime startTime = reportStart.toLocalTime();
    final LocalTime endTime = reportEnd.toLocalTime();

    new ReservationTestSelenium().setup();

    getManagerDriver().switchToUser();
    getUserDriver().createNewReservation("Res Coming", startDate, endDate, startTime, endTime);

    getUserDriver().createNewReservation("Res Active, which will not become active");
  }

  @Ignore("not implemented yet")
  @Test
  public void verifyNocReport() {
    getManagerDriver().switchToNoc();

    getNocDriver().verifyReport();
  }

}
