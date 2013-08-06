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
package nl.surfnet.bod.domain;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import com.google.common.base.Optional;

import nl.surfnet.bod.domain.ConnectionV2.NotificationBaseTypeUserType;
import nl.surfnet.bod.nsi.v2.ConnectionsV2;
import nl.surfnet.bod.util.XmlUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.ogf.schemas.nsi._2013._07.connection.types.DataPlaneStateChangeRequestType;
import org.ogf.schemas.nsi._2013._07.connection.types.DataPlaneStatusType;
import org.ogf.schemas.nsi._2013._07.connection.types.NotificationBaseType;
import org.ogf.schemas.nsi._2013._07.connection.types.ReservationConfirmCriteriaType;
import org.ogf.schemas.nsi._2013._07.connection.types.ScheduleType;
import org.ogf.schemas.nsi._2013._07.services.point2point.P2PServiceBaseType;
import org.ogf.schemas.nsi._2013._07.services.types.DirectionalityType;
import org.ogf.schemas.nsi._2013._07.services.types.StpType;

public class ConnectionV2Test {

  private static final String CRITERIA_XML =
      "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><criteria version=\"0\" xmlns:ns2=\"http://schemas.ogf.org/nsi/2013/07/connection/types\" xmlns:ns4=\"http://www.w3.org/2001/04/xmlenc#\" xmlns:ns3=\"urn:oasis:names:tc:SAML:2.0:assertion\" xmlns:ns5=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:ns6=\"http://schemas.ogf.org/nsi/2013/07/framework/types\" xmlns:ns7=\"http://schemas.ogf.org/nsi/2013/07/framework/headers\"><schedule/><ns3:p2ps xmlns:ns2=\"http://schemas.ogf.org/nsi/2013/07/framework/types\" xmlns:ns3=\"http://schemas.ogf.org/nsi/2013/07/services/point2point\"><capacity>0</capacity><directionality>Bidirectional</directionality><symmetricPath>true</symmetricPath><sourceSTP><networkId>surfnet.nl</networkId><localId>1</localId></sourceSTP><destSTP><networkId>surfnet.nl</networkId><localId>2</localId></destSTP></ns3:p2ps></criteria>";

  @Test
  public void should_deserialize_criteria_type_from_xml_string() {
    ReservationConfirmCriteriaType result = new ConnectionV2.ReservationConfirmCriteriaTypeUserType().fromXmlString(CRITERIA_XML);

    assertNotNull(result);
    Optional<P2PServiceBaseType> service = ConnectionsV2.findPointToPointService(result);
    assertThat(service.isPresent(), is(true));
    assertThat(service.get().getDestSTP().getNetworkId(), is("surfnet.nl"));
    assertThat(service.get().getDestSTP().getLocalId(), is("2"));
    assertThat(service.get().getDirectionality(), is(DirectionalityType.BIDIRECTIONAL));
  }

  @Test
  public void should_serialize_criteria_type_to_xml_string() {
    P2PServiceBaseType service = new P2PServiceBaseType()
        .withSourceSTP(new StpType().withNetworkId("surfnet.nl").withLocalId("1"))
        .withDestSTP(new StpType().withNetworkId("surfnet.nl").withLocalId("2"))
        .withSymmetricPath(true)
        .withDirectionality(DirectionalityType.BIDIRECTIONAL);
    ReservationConfirmCriteriaType criteria =
        new ReservationConfirmCriteriaType().withSchedule(new ScheduleType());
    ConnectionsV2.addPointToPointService(criteria.getAny(), service);

    String xml = new ConnectionV2.ReservationConfirmCriteriaTypeUserType().toXmlString(criteria);

    assertEquals(CRITERIA_XML, xml);
  }

  private static final String DATA_PLANE_STATE_CHANGE_REQUEST_TYPE_XML =
      "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns2:notificationBaseType xsi:type=\"ns2:DataPlaneStateChangeRequestType\" xmlns:ns2=\"http://schemas.ogf.org/nsi/2013/07/connection/types\" xmlns:ns4=\"http://www.w3.org/2001/04/xmlenc#\" xmlns:ns3=\"urn:oasis:names:tc:SAML:2.0:assertion\" xmlns:ns5=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:ns6=\"http://schemas.ogf.org/nsi/2013/07/framework/types\" xmlns:ns7=\"http://schemas.ogf.org/nsi/2013/07/framework/headers\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><connectionId>ConnectionId</connectionId><notificationId>22</notificationId><timeStamp>2013-06-17T13:10:14Z</timeStamp><dataPlaneStatus><active>true</active><version>0</version><versionConsistent>true</versionConsistent></dataPlaneStatus></ns2:notificationBaseType>";

  @Test
  public void shoud_serialize_data_plane_state_change_request_type_to_xml_string() {
    DataPlaneStateChangeRequestType notification = new DataPlaneStateChangeRequestType()
        .withConnectionId("ConnectionId")
        .withDataPlaneStatus(new DataPlaneStatusType().withActive(true).withVersion(0).withVersionConsistent(true))
        .withNotificationId(22)
        .withTimeStamp(XmlUtils.toGregorianCalendar(new DateTime(2013, 6, 17, 13, 10, 14, DateTimeZone.UTC)));

    String xml = new NotificationBaseTypeUserType().toXmlString(notification);

    assertEquals(DATA_PLANE_STATE_CHANGE_REQUEST_TYPE_XML, xml);
    assertThat(xml, is(DATA_PLANE_STATE_CHANGE_REQUEST_TYPE_XML));
  }

  @Test
  public void should_deserialize_data_plane_state_change_request_type_from_xml_string() {
    NotificationBaseType dataPlaneNotification = new NotificationBaseTypeUserType().fromXmlString(DATA_PLANE_STATE_CHANGE_REQUEST_TYPE_XML);

    assertThat(dataPlaneNotification, is(instanceOf(DataPlaneStateChangeRequestType.class)));
  }
}
