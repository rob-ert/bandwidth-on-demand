/**
 * Copyright (c) 2012, 2013 SURFnet BV
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
package nl.surfnet.bod.nsi.v1sc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;

import nl.surfnet.bod.domain.ConnectionV1;
import nl.surfnet.bod.domain.ProtectionType;
import nl.surfnet.bod.nsi.NsiHelper;
import nl.surfnet.bod.support.ReserveRequestTypeFactory;
import oasis.names.tc.saml._2_0.assertion.AttributeStatementType;
import oasis.names.tc.saml._2_0.assertion.AttributeType;

import org.joda.time.DateTime;
import org.junit.Test;
import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;
import org.ogf.schemas.nsi._2011._10.connection.types.TechnologySpecificAttributesType;

public class ConnectionServiceProviderFunctionsTest {

  private NsiHelper mockNsiHelper = new NsiHelper("", "", "");

  @Test
  public void reserveToConnectionWithoutStartTime() throws DatatypeConfigurationException {
    ReserveRequestType reserveRequest = new ReserveRequestTypeFactory().setScheduleStartTime(null).setScheduleEndTime(
      DatatypeFactory.newInstance().newXMLGregorianCalendar(2012, 5, 18, 14, 0, 0, 0,
        DatatypeConstants.FIELD_UNDEFINED)).setDuration(null).setConnectionId("connectionId1").create();

    ConnectionV1 connection = ConnectionServiceProviderFunctions.reserveRequestToConnection(mockNsiHelper, ProtectionType.PROTECTED).apply(reserveRequest);

    assertThat(connection.getConnectionId(), is("connectionId1"));
    assertThat(connection.getStartTime().isPresent(), is(false));

    DateTime endTime = new DateTime(connection.getEndTime().get());
    assertThat(endTime.getMillis(), is(new DateTime(2012, 5, 18, 14, 0).getMillis()));
  }

  @Test
  public void reserveToConnectionWithADuration() throws DatatypeConfigurationException {
    ReserveRequestType reserveRequest = new ReserveRequestTypeFactory().setScheduleStartTime(
        DatatypeFactory.newInstance().newXMLGregorianCalendar(2012, 5, 18, 14, 0, 0, 0,
            DatatypeConstants.FIELD_UNDEFINED)).setScheduleEndTime(null).setDuration(
        DatatypeFactory.newInstance().newDuration(true, 0, 0, 2, 5, 10, 0)).setConnectionId("connectionId1").create();

    ConnectionV1 connection = ConnectionServiceProviderFunctions.reserveRequestToConnection(mockNsiHelper, ProtectionType.PROTECTED).apply(reserveRequest);

    assertThat(connection.getConnectionId(), is("connectionId1"));
    DateTime startTime = new DateTime(connection.getStartTime().get());
    assertThat(startTime.getMillis(), is(new DateTime(2012, 5, 18, 14, 0).getMillis()));

    DateTime endTime = new DateTime(connection.getEndTime().get());
    assertThat(endTime.getMillis(), is(new DateTime(2012, 5, 20, 19, 10).getMillis()));
  }

  @Test
  public void reserveAnUnprotectedPath() throws DatatypeConfigurationException {
    ReserveRequestType reserveRequest = new ReserveRequestTypeFactory().create();

    AttributeType protectedAttr = new AttributeType();
    protectedAttr.setName("sNCP");
    protectedAttr.getAttributeValue().add("  UnProtected   ");
    AttributeStatementType guranteed = new AttributeStatementType();
    guranteed.getAttributeOrEncryptedAttribute().add(protectedAttr);
    TechnologySpecificAttributesType serviceAttributes = new TechnologySpecificAttributesType();
    serviceAttributes.setGuaranteed(guranteed);
    reserveRequest.getReserve().getReservation().getServiceParameters().setServiceAttributes(serviceAttributes);

    ConnectionV1 connection = ConnectionServiceProviderFunctions.reserveRequestToConnection(mockNsiHelper, ProtectionType.PROTECTED).apply(reserveRequest);

    assertThat(connection.getProtectionType(), is(ProtectionType.UNPROTECTED));
  }

  @Test
  public void reserveAnPathDefaultIsProtected() throws DatatypeConfigurationException {
    ReserveRequestType reserveRequest = new ReserveRequestTypeFactory().create();

    reserveRequest.getReserve().getReservation().getServiceParameters().setServiceAttributes(null);

    ConnectionV1 connection = ConnectionServiceProviderFunctions.reserveRequestToConnection(mockNsiHelper, ProtectionType.PROTECTED).apply(reserveRequest);

    assertThat(connection.getProtectionType(), is(ProtectionType.PROTECTED));
  }
}
