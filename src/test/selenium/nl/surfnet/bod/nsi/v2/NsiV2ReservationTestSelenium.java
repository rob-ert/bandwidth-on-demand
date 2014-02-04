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
package nl.surfnet.bod.nsi.v2;

import static com.jayway.awaitility.Awaitility.await;
import static nl.surfnet.bod.nsi.NsiHelper.generateCorrelationId;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.xml.XMLConstants;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Holder;
import javax.xml.ws.handler.MessageContext;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ogf.schemas.nsi._2013._12.connection.provider.ConnectionProviderPort;
import org.ogf.schemas.nsi._2013._12.connection.provider.ConnectionServiceProvider;
import org.ogf.schemas.nsi._2013._12.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2013._12.connection.types.GenericConfirmedType;
import org.ogf.schemas.nsi._2013._12.connection.types.ProvisionStateEnumType;
import org.ogf.schemas.nsi._2013._12.connection.types.QueryResultConfirmedType;
import org.ogf.schemas.nsi._2013._12.connection.types.QueryResultResponseType;
import org.ogf.schemas.nsi._2013._12.connection.types.QuerySummaryConfirmedType;
import org.ogf.schemas.nsi._2013._12.connection.types.QuerySummaryResultType;
import org.ogf.schemas.nsi._2013._12.connection.types.ReservationRequestCriteriaType;
import org.ogf.schemas.nsi._2013._12.connection.types.ReservationStateEnumType;
import org.ogf.schemas.nsi._2013._12.connection.types.ReserveConfirmedType;
import org.ogf.schemas.nsi._2013._12.connection.types.ScheduleType;
import org.ogf.schemas.nsi._2013._12.framework.headers.CommonHeaderType;
import org.ogf.schemas.nsi._2013._12.services.point2point.P2PServiceBaseType;
import org.ogf.schemas.nsi._2013._12.services.types.DirectionalityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import com.google.common.base.Optional;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Longs;

import nl.surfnet.bod.domain.NsiVersion;
import nl.surfnet.bod.nsi.NsiHelper;
import nl.surfnet.bod.nsi.v2.SoapReplyListener.Message;
import nl.surfnet.bod.service.DatabaseTestHelper;
import nl.surfnet.bod.support.BodWebDriver;
import nl.surfnet.bod.support.SeleniumWithSingleSetup;
import nl.surfnet.bod.util.JaxbUserType;
import nl.surfnet.bod.util.XmlUtils;

public class NsiV2ReservationTestSelenium extends SeleniumWithSingleSetup {

  private static final Logger LOG = LoggerFactory.getLogger(NsiV2ReservationTestSelenium.class);

  private static final String WSDL_LOCATION = "/wsdl/2.0/ogf_nsi_connection_provider_v2_0.wsdl";
  private static final String ENDPOINT = BodWebDriver.URL_UNDER_TEST + "/nsi/v2/provider";

  private static final int REPLY_SERVER_PORT = 31337;
  private static final String REPLY_ADDRESS = "http://localhost:" + REPLY_SERVER_PORT + "/requester";
  private static final int VLAN_ID = 23;

  private SoapReplyListener soapReplyListener;
  private Endpoint soapReplyListenerEndpoint;
  private DateTime startTime;
  private DateTime endTime;
  private ConnectionProviderPort connectionServiceProviderPort;
  private List<String> virtualPortIds;
  private String sourceStp;
  private String destStp;
  private Message<?> lastConvertedMessage;

  private NsiHelper nsiHelper = new NsiHelper("surfnet.nl", "surfnet.nl:1990", "bod-selenium", "surfnet6:testbed", "urn:nl:surfnet:diensten:bod");

  public void before(){
    lastConvertedMessage = null;
  }

  @Override
  public void setupInitialData() {
    getNocDriver().createNewApiBasedPhysicalResourceGroup(GROUP_SURFNET, ICT_MANAGERS_GROUP, "test@example.com");
    getNocDriver().linkUniPort(NMS_PORT_ID_2, "First port", GROUP_SURFNET);
    getNocDriver().linkUniPort(NMS_PORT_ID_3, "Second port", GROUP_SURFNET);

    getWebDriver().clickLinkInLastEmail();

    getUserDriver().requestVirtualPort("Selenium users");
    getUserDriver().selectInstituteAndRequest(GROUP_SURFNET, 1200, "port 1");
    getWebDriver().clickLinkInLastEmail();
    getManagerDriver().acceptVirtualPort("First port", "First port", Optional.<String>absent(), Optional.of(VLAN_ID));

    getUserDriver().requestVirtualPort("Selenium users");
    getUserDriver().selectInstituteAndRequest(GROUP_SURFNET, 1200, "port 2");
    getWebDriver().clickLinkInLastEmail();
    getManagerDriver().acceptVirtualPort("Second port", "Second port", Optional.<String>absent(), Optional.of(VLAN_ID));
  }

  @Before
  public void setUp() throws Exception {
    soapReplyListener = new SoapReplyListener();
    soapReplyListenerEndpoint = Endpoint.publish(REPLY_ADDRESS, soapReplyListener);

    startTime = DateTime.now();
    endTime = DateTime.now().plusHours(1);
    String oauthToken = "f00f";

    connectionServiceProviderPort = createPort(oauthToken);
    virtualPortIds = getUserDriver().getVirtualPortIds(NsiVersion.TWO);

    assertTrue("We need at least two portDefinitions to be able to continue", virtualPortIds.size() >= 2);
    sourceStp = virtualPortIds.get(0);
    destStp = virtualPortIds.get(1);
  }

  @After
  public void tearDown() {
    soapReplyListenerEndpoint.stop();
    soapReplyListener = null;
  }

  @After
  public void dropReservations() {
    DatabaseTestHelper.deleteReservationsFromSeleniumDatabase();
  }

  @Test
  public void reserveAndProvision() throws Exception {
    String description = "NSI v2 Reservation";
    String globalReservationId = nsiHelper.generateGlobalReservationId();
    ReservationRequestCriteriaType criteria = new ReservationRequestCriteriaType()
      .withSchedule(new ScheduleType()
        .withStartTime(XmlUtils.toGregorianCalendar(startTime))
        .withEndTime(XmlUtils.toGregorianCalendar(endTime)))
      .withServiceType("http://services.ogf.org/nsi/2013/12/descriptions/EVTS.A-GOLE");
    ConnectionsV2.addPointToPointService(criteria.getAny(), new P2PServiceBaseType()
        .withCapacity(100)
        .withDirectionality(DirectionalityType.BIDIRECTIONAL)
        .withSourceSTP(sourceStp)
        .withDestSTP(destStp));

    // Initial reserve
    String reserveCorrelationId = generateCorrelationId();
    Holder<String> connectionId = new Holder<>(null);
    Holder<CommonHeaderType> reserveHeader = createHeader(reserveCorrelationId);
    connectionServiceProviderPort.reserve(connectionId, globalReservationId, description, criteria, reserveHeader);
    assertThat(connectionId.value, is(notNullValue()));
    assertThat(reserveHeader.value.getReplyTo(), is(nullValue()));

    await().until(convertableMessageArrived(Converters.RESERVE_CONFIRMED_CONVERTER));
    Message<ReserveConfirmedType> reserveConfirmed = (Message<ReserveConfirmedType>) lastConvertedMessage;
    assertThat("reserve confirmed correlation id", reserveConfirmed.header.getCorrelationId(), is(reserveCorrelationId));
    assertThat("connection id must match", reserveConfirmed.body.getConnectionId(), is(connectionId.value));
    assertThat("global reservation id must match", reserveConfirmed.body.getGlobalReservationId(), is(globalReservationId));

    // Reserve commit
    String reserveCommitCorrelationId = generateCorrelationId();
    connectionServiceProviderPort.reserveCommit(connectionId.value, createHeader(reserveCommitCorrelationId));


    await().until(convertableMessageArrived(Converters.RESERVE_COMMIT_CONFIRMED_CONVERTER));
    Message<GenericConfirmedType> reserveCommitConfirmed = (Message<GenericConfirmedType>) lastConvertedMessage;
    assertThat("reserve commit confirmed correlation id", reserveCommitConfirmed.header.getCorrelationId(), is(reserveCommitCorrelationId));

    // perform query, assert that reservation_state == reserve_start
    connectionServiceProviderPort.querySummary(Arrays.asList(connectionId.value), Collections.<String>emptyList(), createHeader(generateCorrelationId()));

    await().until(convertableMessageArrived(Converters.QUERY_SUMMARY_CONFIRMED_CONVERTER));
    Message<QuerySummaryConfirmedType> querySummaryConfirmed = (Message<QuerySummaryConfirmedType>) lastConvertedMessage;
    List<QuerySummaryResultType> result = querySummaryConfirmed.body.getReservation();
    assertThat(result, hasSize(1));
    assertThat(result.get(0).getConnectionStates().getReservationState(), is(ReservationStateEnumType.RESERVE_START));

    // do a provision, assert that we receive a provisionConfirmed message
    String provisionCorrelationId = generateCorrelationId();
    connectionServiceProviderPort.provision(connectionId.value, createHeader(provisionCorrelationId));

    await().until(convertableMessageArrived(Converters.PROVISION_CONFIRMED_CONVERTER));
    Message<GenericConfirmedType> provisionConfirmed = (Message<GenericConfirmedType>) lastConvertedMessage;
    assertThat("provision confirmed correlation id", provisionConfirmed.header.getCorrelationId(), is(provisionCorrelationId));

    // query again, see that provisionState is now 'provisioned'
    connectionServiceProviderPort.querySummary(Arrays.asList(connectionId.value), Collections.<String>emptyList(), createHeader(generateCorrelationId()));

    await().until(convertableMessageArrived(Converters.QUERY_SUMMARY_CONFIRMED_CONVERTER));
    querySummaryConfirmed = (Message<QuerySummaryConfirmedType>) lastConvertedMessage;
    result = querySummaryConfirmed.body.getReservation();
    assertThat(result, hasSize(1));
    assertThat(result.get(0).getConnectionStates().getProvisionState(), is(ProvisionStateEnumType.PROVISIONED));

    // finally, query result and see that all the appropriate state changes appear
    final String queryResultCorrelationId = generateCorrelationId();
    connectionServiceProviderPort.queryResult(connectionId.value, null, null, createHeader(queryResultCorrelationId));

    await().until(convertableMessageArrived(Converters.QUERY_RESULT_CONFIRMED_CONVERTER));
    Message<QueryResultConfirmedType> queryResultConfirmedTypeMessage = (Message<QueryResultConfirmedType>) lastConvertedMessage;
    final List<QueryResultResponseType> queryResults = queryResultConfirmedTypeMessage.body.getResult();

    // sort the collection on resultId, because i'm not sure if we can rely on Jaxb ordering
    Ordering<QueryResultResponseType> byResultIdOrdering = new Ordering<QueryResultResponseType>() {
      public int compare(QueryResultResponseType left, QueryResultResponseType right) {
        return Longs.compare(left.getResultId(), right.getResultId());
      }
    };
    Collections.sort(queryResults, byResultIdOrdering);
    // first result must be reserveConfirmed, second reserveCommitConfirmed and last provisionConfirmed
    assertThat(queryResults, hasSize(3));

    QueryResultResponseType reserveConfirmedEntry = queryResults.get(0);
    assertThat(reserveConfirmedEntry.getResultId(), equalTo(1L));
    QueryResultResponseType reserveCommitConfirmedEntry = queryResults.get(1);
    assertThat(reserveCommitConfirmedEntry.getResultId(), equalTo(2L));
    QueryResultResponseType provisionConfirmedEntry = queryResults.get(2);
    assertThat(provisionConfirmedEntry.getResultId(), equalTo(3L));

    assertThat(reserveConfirmedEntry.getReserveConfirmed(), notNullValue());
    assertThat(reserveCommitConfirmedEntry.getReserveCommitConfirmed(), notNullValue());
    assertThat(provisionConfirmedEntry.getProvisionConfirmed(), notNullValue());

    // see that they all belong to the same Connection
    assertThat(reserveConfirmedEntry.getReserveConfirmed().getConnectionId(), equalTo(reserveCommitConfirmedEntry.getReserveCommitConfirmed().getConnectionId()));
    assertThat(provisionConfirmedEntry.getProvisionConfirmed().getConnectionId(), equalTo(reserveCommitConfirmedEntry.getReserveCommitConfirmed().getConnectionId()));

  }

  @Test
  public void idempotency() throws Exception {
    String globalReservationId = nsiHelper.generateGlobalReservationId();
    String description = "NSI v2 Reservation";
    ReservationRequestCriteriaType criteria = new ReservationRequestCriteriaType()
      .withSchedule(new ScheduleType()
        .withStartTime(XmlUtils.toGregorianCalendar(startTime))
        .withEndTime(XmlUtils.toGregorianCalendar(endTime)))
      .withServiceType("http://services.ogf.org/nsi/2013/12/descriptions/EVTS.A-GOLE");
    ConnectionsV2.addPointToPointService(criteria.getAny(), new P2PServiceBaseType()
      .withCapacity(100)
      .withDirectionality(DirectionalityType.BIDIRECTIONAL)
      .withSourceSTP(sourceStp)
      .withDestSTP(destStp));

    // Initial reserve
    String reserveCorrelationId = generateCorrelationId();
    Holder<String> connectionId1 = new Holder<>(null);
    connectionServiceProviderPort.reserve(connectionId1, globalReservationId, description, criteria, createHeader(reserveCorrelationId));
    assertThat(connectionId1.value, is(notNullValue()));

    await().until(convertableMessageArrived(Converters.RESERVE_CONFIRMED_CONVERTER));
    Message<ReserveConfirmedType> reserveConfirmed1 = (Message<ReserveConfirmedType>) lastConvertedMessage;
    assertThat("reserve confirmed correlation id", reserveConfirmed1.header.getCorrelationId(), is(reserveCorrelationId));

    // Reserve with same content and correlation id.
    Holder<String> connectionId2 = new Holder<>(null);
    connectionServiceProviderPort.reserve(connectionId2, globalReservationId, description, criteria, createHeader(reserveCorrelationId));
    assertThat(connectionId2.value, is(notNullValue()));

    await().until(convertableMessageArrived(Converters.RESERVE_CONFIRMED_CONVERTER));
    Message<ReserveConfirmedType> reserveConfirmed2 = (Message<ReserveConfirmedType>) lastConvertedMessage;
    assertThat("reserve confirmed correlation id", reserveConfirmed2.header.getCorrelationId(), is(reserveCorrelationId));

    assertThat(connectionId1.value, is(connectionId2.value));
    assertThat(Converters.RESERVE_CONFIRMED_CONVERTER.toXmlString(reserveConfirmed1.body), is(Converters.RESERVE_CONFIRMED_CONVERTER.toXmlString(reserveConfirmed2.body)));

    // perform query, assert that there is only a single reservation
    connectionServiceProviderPort.querySummary(null, Arrays.asList(globalReservationId), createHeader(generateCorrelationId()));

    await().until(convertableMessageArrived(Converters.QUERY_SUMMARY_CONFIRMED_CONVERTER));
    Message<QuerySummaryConfirmedType> querySummaryConfirmed = (Message<QuerySummaryConfirmedType>) lastConvertedMessage;

    List<QuerySummaryResultType> result = querySummaryConfirmed.body.getReservation();
    assertThat(result, hasSize(1));
    assertThat(result.get(0).getConnectionId(), is(connectionId1.value));

    // Reserve with same correlation id but different content should generate an error response!
    criteria.setServiceType("changedServiceType");

    Holder<String> connectionId3 = new Holder<>(null);
    try {
      connectionServiceProviderPort.reserve(connectionId3, globalReservationId, description, criteria, createHeader(reserveCorrelationId));
      fail("ServiceExceptione expected");
    } catch (ServiceException expected) {
      assertThat(connectionId3.value, is(nullValue()));
      assertThat(expected.getFaultInfo().getErrorId(), is("100"));
      assertThat(expected.getFaultInfo().getText(), is("PAYLOAD_ERROR"));
    }
  }

  @Test
  public void nsiTopologyShouldBeValidAgainstCurrentXSD() throws Exception {
    final String url = BodWebDriver.URL_UNDER_TEST + "/nsi-topology";

    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    URL schemaFileUrl = this.getClass().getResource("/topology/nsi-ext.xsd");
    Schema schema = schemaFactory.newSchema(new File(schemaFileUrl.toURI()));

    try (CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = httpclient.execute(new HttpGet(url))) {
      HttpEntity entity = response.getEntity();

      Header header = response.getLastHeader("Content-Type");
      assertTrue("application/xml;charset=UTF-8".equals(header.getValue()));

      String xml = EntityUtils.toString(entity);
      assertTrue(xml.length() > 0);
      LOG.debug("Response content: " + xml);
      Validator validator = schema.newValidator();
      validator.validate(new StreamSource(new StringReader(xml)));
    }
  }

  private Holder<CommonHeaderType> createHeader(final String correlationId){
    return new Holder<>(new CommonHeaderType()
        .withProtocolVersion("2.0")
        .withRequesterNSA("urn:ogf:network:nsa:foo")
        .withProviderNSA(nsiHelper.getProviderNsaV2())
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

  private <T> Callable<Boolean> convertableMessageArrived(final JaxbUserType<T> converter) {
    return new Callable<Boolean>() {
      public Boolean call() throws Exception {
        final SOAPMessage message = soapReplyListener.getNextMessage();
        try{
          CommonHeaderType header = Converters.parseNsiHeader(message);
          T body = Converters.parseBody(converter, message);
          lastConvertedMessage = new Message<>(header, body);
          return true;
        }catch (Exception e){
          return false;
        }
      }
    };
  }

  private URL wsdlUrl() {
    try {
      return new ClassPathResource(WSDL_LOCATION).getURL();
    } catch (IOException e) {
      throw new RuntimeException("Could not find the requester wsdl", e);
    }
  }

}
