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
package nl.surfnet.bod;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Holder;
import javax.xml.ws.handler.MessageContext;

import nl.surfnet.bod.nsi.NsiConstants;
import nl.surfnet.bod.nsi.NsiHelper;
import nl.surfnet.bod.nsi.v2.ConnectionsV2;
import nl.surfnet.bod.nsi.v2.Converters;
import nl.surfnet.bod.service.DatabaseTestHelper;
import nl.surfnet.bod.support.BodWebDriver;
import nl.surfnet.bod.support.ReservationFilterViewFactory;
import nl.surfnet.bod.support.SeleniumWithSingleSetup;
import nl.surfnet.bod.support.soap.SoapReplyListener;
import nl.surfnet.bod.support.soap.SoapReplyListener.Message;
import nl.surfnet.bod.util.XmlUtils;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.After;
import org.junit.Test;
import org.ogf.schemas.nsi._2013._04.connection.provider.ConnectionProviderPort;
import org.ogf.schemas.nsi._2013._04.connection.provider.ConnectionServiceProvider;
import org.ogf.schemas.nsi._2013._04.connection.types.DirectionalityType;
import org.ogf.schemas.nsi._2013._04.connection.types.GenericConfirmedType;
import org.ogf.schemas.nsi._2013._04.connection.types.PathType;
import org.ogf.schemas.nsi._2013._04.connection.types.ProvisionStateEnumType;
import org.ogf.schemas.nsi._2013._04.connection.types.QuerySummaryConfirmedType;
import org.ogf.schemas.nsi._2013._04.connection.types.QuerySummaryResultType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReservationRequestCriteriaType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReservationStateEnumType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReserveConfirmedType;
import org.ogf.schemas.nsi._2013._04.connection.types.ScheduleType;
import org.ogf.schemas.nsi._2013._04.connection.types.ServiceAttributesType;
import org.ogf.schemas.nsi._2013._04.connection.types.StpType;
import org.ogf.schemas.nsi._2013._04.framework.headers.CommonHeaderType;
import org.springframework.core.io.ClassPathResource;

public class ReservationTestSelenium extends SeleniumWithSingleSetup {

  private static final String WSDL_LOCATION = "/wsdl/2.0/ogf_nsi_connection_provider_v2_0.wsdl";
  private static final String ENDPOINT = BodWebDriver.URL_UNDER_TEST + "/nsi/v2/provider";

  private static final Integer REPLY_SERVER_PORT = 31337;
  private static final String REPLY_ADDRESS = "http://localhost:" + REPLY_SERVER_PORT + "/requester";

  @Override
  public void setupInitialData() {
    getNocDriver().createNewApiBasedPhysicalResourceGroup(GROUP_SURFNET, ICT_MANAGERS_GROUP, "test@example.com");
    getNocDriver().linkPhysicalPort(NMS_PORT_ID_1, "First port", GROUP_SURFNET);
    getNocDriver().linkPhysicalPort(NMS_PORT_ID_2, "Second port", GROUP_SURFNET);

    getWebDriver().clickLinkInLastEmail();

    getUserDriver().requestVirtualPort("Selenium users");
    getUserDriver().selectInstituteAndRequest(GROUP_SURFNET, 1200, "port 1");
    getWebDriver().clickLinkInLastEmail();
    getManagerDriver().createVirtualPort("First port");

    getUserDriver().requestVirtualPort("Selenium users");
    getUserDriver().selectInstituteAndRequest(GROUP_SURFNET, 1200, "port 2");
    getWebDriver().clickLinkInLastEmail();
    getManagerDriver().createVirtualPort("Second port");
  }

  @After
  public void dropReservations() {
    DatabaseTestHelper.deleteReservationsFromSeleniumDatabase();
  }

  @Test
  public void createAndCancelAReservation() {
    LocalDate startDate = LocalDate.now().plusDays(3);
    LocalDate endDate = LocalDate.now().plusDays(5);
    LocalTime startTime = LocalTime.now().plusHours(1);
    LocalTime endTime = LocalTime.now();
    String reservationLabel = "Selenium Reservation";

    getManagerDriver().switchToUserRole();
    getUserDriver().createNewReservation(reservationLabel, startDate, endDate, startTime, endTime);
    getUserDriver().verifyAndWaitForReservationIsAutoStart(reservationLabel);
    getUserDriver().verifyReservationIsCancellable(reservationLabel, startDate, endDate, startTime, endTime);

    getUserDriver().switchToManagerRole(GROUP_SURFNET);
    getManagerDriver().verifyReservationWasCreated(reservationLabel, startDate, endDate, startTime, endTime);
    getManagerDriver().verifyReservationIsCancellable(reservationLabel, startDate, endDate, startTime, endTime);
    getManagerDriver().verifyStatistics();

    getManagerDriver().switchToNocRole();
    getNocDriver().verifyReservationIsCancellable(reservationLabel, startDate, endDate, startTime, endTime);
    getNocDriver().verifyStatistics();

    getManagerDriver().switchToUserRole();
    getUserDriver().cancelReservation(startDate, endDate, startTime, endTime);
    getUserDriver().verifyReservationIsCanceled(startDate, endDate, startTime, endTime);
  }

  @Test
  public void createReservationWithStartTimeNowAndEndTimeForever() {
    String reservationLabel = "Starts now and forever";

    getManagerDriver().switchToUserRole();

    getUserDriver().createNewReservation(reservationLabel);

    getUserDriver().verifyReservationWasCreated(reservationLabel);

    getUserDriver().verifyAndWaitForReservationIsAutoStartOrRunning(reservationLabel);
  }

  @Test
  public void searchAndFilterReservations() {
    String even = "Even reservation";
    String odd = "Odd reservation";
    LocalDate date = LocalDate.now().plusDays(1);
    LocalTime startTime = new LocalTime(8, 0);

    getManagerDriver().switchToUserRole();

    getUserDriver().createNewReservation(even, date, date, startTime, startTime.plusHours(1));
    getUserDriver().createNewReservation(odd, date, date, startTime.plusHours(2), startTime.plusHours(2 + 1));

    // Filter on this year, and no search String. All should be found.
    getUserDriver().verifyReservationByFilterAndSearch(String.valueOf(date.getYear()), "", even, odd);

    // Search on even
    getUserDriver().verifyReservationByFilterAndSearch(ReservationFilterViewFactory.COMING, "even", even);

    // Search on odd
    getUserDriver().verifyReservationByFilterAndSearch(ReservationFilterViewFactory.COMING, "odd", odd);

  }

  @Test
  public void soapReservationHappyFlow() throws Exception {

    DateTime startTime = DateTime.now().plusHours(1);
    DateTime endTime = DateTime.now();
    String oauthToken = "f00f";

    SoapReplyListener soapReplyListener = new SoapReplyListener();
    Endpoint.publish(REPLY_ADDRESS, soapReplyListener);

    ConnectionProviderPort connectionServiceProviderPort = createPort(oauthToken);

    List<String> virtualPortIds = getUserDriver().getVirtualPortIds();

    assertTrue("We need at least two portDefinitions to be able to continue", virtualPortIds.size() >= 2);
    StpType sourceStp = ConnectionsV2.toStpType(virtualPortIds.get(0));
    StpType destStp = ConnectionsV2.toStpType(virtualPortIds.get(1));

    String description = "NSI v2 Reservation";
    String globalReservationId = NsiHelper.generateGlobalReservationId();
    ReservationRequestCriteriaType criteria = new ReservationRequestCriteriaType()
      .withSchedule(new ScheduleType()
        .withStartTime(XmlUtils.toGregorianCalendar(startTime))
        .withEndTime(XmlUtils.toGregorianCalendar(endTime)))
        .withBandwidth(100)
        .withPath(new PathType()
          .withDirectionality(DirectionalityType.BIDIRECTIONAL)
          .withSourceSTP(sourceStp)
          .withDestSTP(destStp))
        .withServiceAttributes(new ServiceAttributesType());


    // Initial reserve
    String reserveCorrelationId = "urn:uuid:" + UUID.randomUUID().toString();
    Holder<String> connectionId = new Holder<>(null);
    connectionServiceProviderPort.reserve(connectionId, globalReservationId, description, criteria, createHeader(reserveCorrelationId));
    assertThat(connectionId.value, is(notNullValue()));

    Message<ReserveConfirmedType> reserveConfirmed = soapReplyListener.getLastReply(Converters.RESERVE_CONFIRMED_CONVERTER);
    assertThat("reserve confirmed correlation id", reserveConfirmed.header.getCorrelationId(), is(reserveCorrelationId));
    assertThat("connection id must match", reserveConfirmed.body.getConnectionId(), is(connectionId.value));
    assertThat("global reservation id must match", reserveConfirmed.body.getGlobalReservationId(), is(globalReservationId));


    // Reserve commit
    String reserveCommitCorrelationId = "urn:uuid:" + UUID.randomUUID().toString();
    connectionServiceProviderPort.reserveCommit(connectionId.value, createHeader(reserveCommitCorrelationId));

    Message<GenericConfirmedType> reserveCommitConfirmed = soapReplyListener.getLastReply(Converters.RESERVE_COMMIT_CONFIRMED_CONVERTER);
    assertThat("reserve commit confirmed correlation id", reserveCommitConfirmed.header.getCorrelationId(), is(reserveCommitCorrelationId));


    // perform query, assert that reservation_state == reserve_start
    connectionServiceProviderPort.querySummary(Arrays.asList(connectionId.value), Collections.<String>emptyList(), createHeader("urn:uuid:" + UUID.randomUUID()));
    Message<QuerySummaryConfirmedType> querySummaryConfirmed = soapReplyListener.getLastReply(Converters.QUERY_SUMMARY_CONFIRMED_CONVERTER);

    List<QuerySummaryResultType> result = querySummaryConfirmed.body.getReservation();
    assertThat(result, hasSize(1));
    assertThat(result.get(0).getConnectionStates().getReservationState(), is(ReservationStateEnumType.RESERVE_START));


    // do a provision, assert that we receive a provisionConfirmed message
    String provisionCorrelationId = "urn:uuid:" + UUID.randomUUID().toString();
    connectionServiceProviderPort.provision(connectionId.value, createHeader(provisionCorrelationId));
    Message<GenericConfirmedType> provisionConfirmed = soapReplyListener.getLastReply(Converters.PROVISION_CONFIRMED_CONVERTER);
    assertThat("provision confirmed correlation id", provisionConfirmed.header.getCorrelationId(), is(provisionCorrelationId));


    // query again, see that provisionState is now 'provisioned'
    connectionServiceProviderPort.querySummary(Arrays.asList(connectionId.value), Collections.<String>emptyList(), createHeader("urn:uuid:" + UUID.randomUUID().toString()));
    querySummaryConfirmed = soapReplyListener.getLastReply(Converters.QUERY_SUMMARY_CONFIRMED_CONVERTER);
    result = querySummaryConfirmed.body.getReservation();
    assertThat(result, hasSize(1));
    assertThat(result.get(0).getConnectionStates().getProvisionState(), is(ProvisionStateEnumType.PROVISIONED));
  }

  private Holder<CommonHeaderType> createHeader(final String correlationId){
    return new Holder<>(new CommonHeaderType()
        .withProtocolVersion("2.0")
        .withRequesterNSA("urn:ogf:network:nsa:foo")
        .withProviderNSA(NsiConstants.URN_PROVIDER_NSA)
        .withReplyTo(REPLY_ADDRESS)
        .withCorrelationId(correlationId)
    );
  }

  private ConnectionProviderPort createPort(String oauthToken) {
    ConnectionProviderPort port = new ConnectionServiceProvider(wsdlUrl()).getConnectionServiceProviderPort();
    Map<String,Object> requestContext = ((BindingProvider) port).getRequestContext();
    requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ENDPOINT);
    requestContext.put(MessageContext.HTTP_REQUEST_HEADERS, Collections.singletonMap("Authorization", Collections.singletonList("bearer " + oauthToken)));
    return port;
  }


  private URL wsdlUrl() {
    try {
      return new ClassPathResource(WSDL_LOCATION).getURL();
    }
    catch (IOException e) {
      throw new RuntimeException("Could not find the requester wsdl", e);
    }
  }
}
