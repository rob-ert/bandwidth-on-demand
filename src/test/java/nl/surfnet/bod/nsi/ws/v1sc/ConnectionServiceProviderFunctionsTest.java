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
package nl.surfnet.bod.nsi.ws.v1sc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.support.ReserveRequestTypeFactory;

import org.joda.time.DateTime;
import org.junit.Test;
import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;

public class ConnectionServiceProviderFunctionsTest {

  @Test
  public void reserveToConnectionWithoutStartTime() throws DatatypeConfigurationException {
    ReserveRequestType reserveRequest = new ReserveRequestTypeFactory()
      .setScheduleStartTime(null)
      .setScheduleEndTime(
          DatatypeFactory.newInstance().newXMLGregorianCalendar(2012, 5, 18, 14, 0, 0, 0, DatatypeConstants.FIELD_UNDEFINED))
      .setDuration(null)
      .setConnectionId("connectionId1").create();

    Connection connection = ConnectionServiceProviderFunctions.RESERVE_REQUEST_TO_CONNECTION.apply(reserveRequest);

    assertThat(connection.getConnectionId(), is("connectionId1"));
    assertThat(connection.getStartTime().isPresent(), is(false));

    DateTime endTime = new DateTime(connection.getEndTime().get());
    assertThat(endTime, is(new DateTime(2012, 5, 18, 14, 0)));
  }

  @Test
  public void reserveToConnectionWithADuration() throws DatatypeConfigurationException {
    ReserveRequestType reserveRequest = new ReserveRequestTypeFactory()
      .setScheduleStartTime(
          DatatypeFactory.newInstance().newXMLGregorianCalendar(2012, 5, 18, 14, 0, 0, 0, DatatypeConstants.FIELD_UNDEFINED))
      .setScheduleEndTime(null)
      .setDuration(DatatypeFactory.newInstance().newDuration(true, 0, 0, 2, 5, 10, 0))
      .setConnectionId("connectionId1").create();

    Connection connection = ConnectionServiceProviderFunctions.RESERVE_REQUEST_TO_CONNECTION.apply(reserveRequest);

    assertThat(connection.getConnectionId(), is("connectionId1"));
    DateTime startTime = new DateTime(connection.getStartTime().get());
    assertThat(startTime, is(new DateTime(2012, 5, 18, 14, 0)));

    DateTime endTime = new DateTime(connection.getEndTime().get());
    assertThat(endTime, is(new DateTime(2012, 5, 20, 19, 10)));
  }

}
