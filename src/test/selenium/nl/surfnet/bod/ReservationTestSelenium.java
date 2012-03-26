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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ReservationTestSelenium extends TestExternalSupport {

  @Before
  public void setup() {
    getNocDriver().createNewPhysicalResourceGroup("SURFnet bv", ICT_MANAGERS_GROUP, "test@example.com");
    getNocDriver().linkPhysicalPort(NETWORK_ELEMENT_PK, "First port", "SURFnet bv");
    getNocDriver().linkPhysicalPort(NETWORK_ELEMENT_PK_2, "Second port", "SURFnet bv");
    getWebDriver().refreshGroups();
    getManagerDriver().createNewVirtualResourceGroup("Selenium research", USERS_GROUP);
//    getManagerDriver().createNewVirtualPort("Port 1", 1000, null, null, "First port");
//    getManagerDriver().createNewVirtualPort("Port 2", 1000, null, null, "Second port");
  }

  @Test
  public void createAndDeleteAReservation() {
    LocalDateTime creationDateTime = LocalDateTime.now();
    LocalDate startDate = LocalDate.now().plusDays(3);
    LocalDate endDate = LocalDate.now().plusDays(5);
    LocalTime startTime = LocalTime.now().plusHours(1);
    LocalTime endTime = LocalTime.now();

    getWebDriver().createNewReservation(startDate, endDate, startTime, endTime);

    getWebDriver().verifyReservationWasCreated(startDate, endDate, startTime, endTime, creationDateTime);

    getManagerDriver().verifyReservationExists(startDate, endDate, startTime, endTime, creationDateTime);

    getWebDriver().cancelReservation(startDate, endDate, startTime, endTime);

    getWebDriver().verifyReservationWasCanceled(startDate, endDate, startTime, endTime);
  }

  @After
  public void teardown() {
    getManagerDriver().deleteVirtualResourceGroup("Selenium research");
    getNocDriver().unlinkPhysicalPort(NETWORK_ELEMENT_PK);
    getNocDriver().unlinkPhysicalPort(NETWORK_ELEMENT_PK_2);
    getNocDriver().deletePhysicalResourceGroup("SURFnet bv");
  }

}
