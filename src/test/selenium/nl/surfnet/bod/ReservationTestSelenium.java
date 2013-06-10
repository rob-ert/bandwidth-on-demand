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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Collections;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.handler.MessageContext;

import nl.surfnet.bod.nsi.NsiHelper;
import nl.surfnet.bod.service.DatabaseTestHelper;
import nl.surfnet.bod.support.ReservationFilterViewFactory;
import nl.surfnet.bod.support.SeleniumWithSingleSetup;

import nl.surfnet.bod.support.soap.SoapReplyListener;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ogf.schemas.nsi._2013._04.connection.provider.ConnectionProviderPort;
import org.ogf.schemas.nsi._2013._04.connection.provider.ConnectionServiceProvider;
import org.ogf.schemas.nsi._2013._04.connection.types.DirectionalityType;
import org.ogf.schemas.nsi._2013._04.connection.types.PathType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReservationRequestCriteriaType;
import org.ogf.schemas.nsi._2013._04.connection.types.ScheduleType;
import org.ogf.schemas.nsi._2013._04.connection.types.StpType;
import org.ogf.schemas.nsi._2013._04.framework.headers.CommonHeaderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

public class ReservationTestSelenium extends SeleniumWithSingleSetup {

  private static final Logger LOG = LoggerFactory.getLogger(ReservationTestSelenium.class);

  private static final String WSDL_LOCATION = "/wsdl/2.0/ogf_nsi_connection_provider_v2_0.wsdl";
  private static final String ENDPOINT = "http://localhost:8083/bod/nsi/v2/provider";

  private static final Integer REPLY_SERVER_PORT = 31337;
  private static final String REPLY_PATH = "http://localhost:" + REPLY_SERVER_PORT + "/";

  private String OAUTH_TOKEN = "a7dc882f-64b2-40d6-a208-3f59efb7a7ef";

  private SoapReplyListener soapReplyListener;
  private ConnectionProviderPort connectionServiceProviderPort;

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

  @Before
  public void before(){

    // set and retrieve the oauth token that we can use when using the soap api


    soapReplyListener = new SoapReplyListener(REPLY_SERVER_PORT);
    connectionServiceProviderPort = createPort();

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
    getUserDriver().verifyReservationWasCreated(reservationLabel, startDate, endDate, startTime, endTime);
    getUserDriver().verifyReservationIsNotCancellable(reservationLabel, startDate, endDate, startTime, endTime);
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
  public void createReservationWithNowAndForever() {
    String reservationLabel = "Starts now and forever";

    getManagerDriver().switchToUserRole();

    getUserDriver().createNewReservation(reservationLabel);

    getUserDriver().verifyReservationWasCreated(reservationLabel);

    getUserDriver().verifyAndWaitForReservationIsAutoStart(reservationLabel);
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
  @Ignore
  public void createAndCancelAReservationThroughSoap() {

    LocalDateTime startTime = LocalDateTime.now().plusHours(1);
    LocalDateTime endTime = LocalDateTime.now();


    try {

      GregorianCalendar gregorianCalendar = new GregorianCalendar();

      gregorianCalendar.setTime(startTime.toDate());
      XMLGregorianCalendar xmlGregorianCalendarStart = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);

      gregorianCalendar.setTime(endTime.toDate());
      XMLGregorianCalendar xmlGregorianCalendarEnd = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);


      String description = "NSI v2 Reservation";
      String globalReservationId = NsiHelper.generateGlobalReservationId();
      ReservationRequestCriteriaType criteria = new ReservationRequestCriteriaType();

      criteria.setSchedule(new ScheduleType().withStartTime(xmlGregorianCalendarStart).withEndTime(xmlGregorianCalendarEnd));
      criteria.setBandwidth(100);
      criteria.setPath(new PathType()
          .withDirectionality(DirectionalityType.BIDIRECTIONAL)
          .withSourceSTP(new StpType().withNetworkId(NMS_PORT_ID_1))
          .withDestSTP(new StpType().withNetworkId(NMS_PORT_ID_2))
      );

      connectionServiceProviderPort.reserve(null, globalReservationId, description, criteria, getHeader());
      String reply = soapReplyListener.waitForReply();
      LOG.debug(reply);
      assertTrue(reply != null);
      // assert that it was indeed created

      // cancel it (as a NOC manager?)

      // verify that it is indeed cancelled.
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Holder<CommonHeaderType> getHeader(){
    return new Holder<CommonHeaderType>(new CommonHeaderType()
        .withProtocolVersion("2.0")
        .withRequesterNSA("reqNSA")
        .withProviderNSA("provnsa")
        .withReplyTo(REPLY_PATH)
    );
  }

  private ConnectionProviderPort createPort() {


    ConnectionProviderPort port = new ConnectionServiceProvider(wsdlUrl()).getConnectionServiceProviderPort();
    ((BindingProvider) port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ENDPOINT);

    ((BindingProvider) port).getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, Collections.singletonMap("Authorization", Collections.singletonList("bearer " + OAUTH_TOKEN)));

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