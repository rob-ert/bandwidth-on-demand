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
package nl.surfnet.bod.nbi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.List;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.nbi.generated.NetworkMonitoringServiceFault;
import nl.surfnet.bod.nbi.generated.NetworkMonitoringService_v30Stub;
import nl.surfnet.bod.support.ReservationFactory;

import org.apache.xmlbeans.XmlException;
import org.joda.time.DateTimeUtils;
import org.joda.time.Days;
import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd.SecurityDocument;
import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointRequestDocument;
import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointResponseDocument;
import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointsRequestDocument;
import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointsResponseDocument;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.CreateReservationScheduleRequestDocument;

@RunWith(MockitoJUnitRunner.class)
public class NbiOpenDracWsClientTest {

  @InjectMocks
  private NbiOpenDracWsClient subject;

  @Mock
  private NetworkMonitoringService_v30Stub networkingServiceMock;

  private QueryEndpointsResponseDocument endpointsResponse;

  private QueryEndpointResponseDocument endpointResponse;

  @Before
  public void init() {
    subject.setPassword("292c2cdcb5f669a8");

    try {
      endpointsResponse = QueryEndpointsResponseDocument.Factory.parse(new File(
          "src/test/resources/opendrac/queryEndpointsResponse.xml"));

      endpointResponse = QueryEndpointResponseDocument.Factory.parse(new File(
          "src/test/resources/opendrac/queryEndpointResponse.xml"));
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void findAllPhysicalPorts() throws XmlException, NetworkMonitoringServiceFault, IOException {

    when(networkingServiceMock.queryEndpoints(any(QueryEndpointsRequestDocument.class), any(SecurityDocument.class)))
        .thenReturn(endpointsResponse);
    when(networkingServiceMock.queryEndpoint(any(QueryEndpointRequestDocument.class), any(SecurityDocument.class)))
        .thenReturn(endpointResponse);

    List<PhysicalPort> ports = subject.findAllPhysicalPorts();

    assertThat(ports, hasSize(1));
  }

  @Test
  public void shouldCreateReservationWithGivenStartTime() throws Exception {
    LocalDateTime start = LocalDateTime.now();
    LocalDateTime end = start.plus(Days.ONE);
    Reservation reservation = new ReservationFactory().setStartDateTime(start).setEndDateTime(end).create();
    // Set ports to mock values in xml
    reservation.getSourcePort().getPhysicalPort().setNetworkElementPk("00-21-E1-D9-CC-70_ETH-1-36-4");
    reservation.getDestinationPort().getPhysicalPort().setNetworkElementPk("00-21-E1-D9-CC-70_ETH-1-36-4");

    when(networkingServiceMock.queryEndpoints(any(QueryEndpointsRequestDocument.class), any(SecurityDocument.class)))
        .thenReturn(endpointsResponse);

    when(networkingServiceMock.queryEndpoint(any(QueryEndpointRequestDocument.class), any(SecurityDocument.class)))
        .thenReturn(endpointResponse);

    CreateReservationScheduleRequestDocument schedule = null;

    schedule = subject.createSchedule(reservation);

    assertThat(schedule.getCreateReservationScheduleRequest().getReservationSchedule().getStartTime().getTime(),
        equalTo(start.toDate()));
  }

  @Test
  public void shouldCreateReservationNow() throws Exception {
    LocalDateTime start = LocalDateTime.now();
    LocalDateTime end = start.plus(Days.ONE);

    DateTimeUtils.setCurrentMillisFixed(start.toDate().getTime());
    try {
      Reservation reservation = new ReservationFactory().setEndDateTime(end).create();
      reservation.setStartDate(null);
      reservation.setStartTime(null);

      // Set ports to mock values in xml
      reservation.getSourcePort().getPhysicalPort().setNetworkElementPk("00-21-E1-D9-CC-70_ETH-1-36-4");
      reservation.getDestinationPort().getPhysicalPort().setNetworkElementPk("00-21-E1-D9-CC-70_ETH-1-36-4");

      when(networkingServiceMock.queryEndpoints(any(QueryEndpointsRequestDocument.class), any(SecurityDocument.class)))
          .thenReturn(endpointsResponse);

      when(networkingServiceMock.queryEndpoint(any(QueryEndpointRequestDocument.class), any(SecurityDocument.class)))
          .thenReturn(endpointResponse);

      CreateReservationScheduleRequestDocument schedule = null;

      schedule = subject.createSchedule(reservation);

      long scheduleStart = schedule.getCreateReservationScheduleRequest().getReservationSchedule().getStartTime()
          .getTime().getTime()-1;

      assertThat(scheduleStart, equalTo(start.toDate().getTime()));
    }
    finally {
      DateTimeUtils.setCurrentMillisSystem();
    }
  }
}
