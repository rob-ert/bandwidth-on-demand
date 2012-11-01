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

import java.io.File;
import java.io.IOException;
import java.util.List;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.nbi.generated.NetworkMonitoringServiceFault;
import nl.surfnet.bod.nbi.generated.NetworkMonitoringService_v30Stub;
import nl.surfnet.bod.nbi.generated.ResourceAllocationAndSchedulingService_v30Stub;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;

import org.apache.xmlbeans.XmlException;
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
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.CreateReservationScheduleResponseDocument;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.PathRequestT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ValidProtectionTypeT;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NbiOpenDracWsClientTest {

  @InjectMocks
  private NbiOpenDracWsClient subject;

  @Mock
  private NetworkMonitoringService_v30Stub networkingServiceMock;
  @Mock
  private ResourceAllocationAndSchedulingService_v30Stub schedulingServiceMock;

  private QueryEndpointsResponseDocument endpointsResponse;
  private QueryEndpointResponseDocument endpointResponse;
  private VirtualPort sourcePort;
  private VirtualPort destPort;

  @Before
  public void init() throws Exception {
    subject.setPassword("292c2cdcb5f669a8");

    endpointsResponse = QueryEndpointsResponseDocument.Factory.parse(new File(
        "src/test/resources/opendrac/queryEndpointsResponse.xml"));
    endpointResponse = QueryEndpointResponseDocument.Factory.parse(new File(
        "src/test/resources/opendrac/queryEndpointResponse.xml"));

    VirtualResourceGroup vrg = new VirtualResourceGroupFactory().create();
    sourcePort = new VirtualPortFactory()
        .setPhysicalPort(new PhysicalPortFactory().setNmsPortId("00-21-E1-D9-CC-70_ETH-1-36-4").create())
        .setVirtualResourceGroup(vrg).create();
    destPort = new VirtualPortFactory()
        .setPhysicalPort(new PhysicalPortFactory().setNmsPortId("00-21-E1-D9-CC-70_ETH-1-36-4").create())
        .setVirtualResourceGroup(vrg).create();

    when(networkingServiceMock.queryEndpoints(any(QueryEndpointsRequestDocument.class), any(SecurityDocument.class)))
        .thenReturn(endpointsResponse);

    when(networkingServiceMock.queryEndpoint(any(QueryEndpointRequestDocument.class), any(SecurityDocument.class)))
        .thenReturn(endpointResponse);
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
    Reservation reservation = new ReservationFactory().setSourcePort(sourcePort).setDestinationPort(destPort).create();

    CreateReservationScheduleRequestDocument schedule = subject.createSchedule(reservation, true);

    assertThat(schedule.getCreateReservationScheduleRequest().getReservationSchedule().getStartTime().getTime(),
        is(reservation.getStartDateTime().toDate()));
  }

  @Test
  public void createReservationShouldFailWithMessage() throws Exception {
    Reservation reservation = new ReservationFactory().setSourcePort(sourcePort).setDestinationPort(destPort).create();
    CreateReservationScheduleResponseDocument responseDocument = CreateReservationScheduleResponseDocument.Factory
        .parse(new File("src/test/resources/opendrac/createReservationScheduleResponse.xml"));

    when(
        schedulingServiceMock.createReservationSchedule(any(CreateReservationScheduleRequestDocument.class),
            any(SecurityDocument.class))).thenReturn(responseDocument);

    Reservation scheduledReservation = subject.createReservation(reservation, true);

    assertThat(scheduledReservation.getStatus(), is(ReservationStatus.NOT_ACCEPTED));
    assertThat(scheduledReservation.getFailedReason(), is("No available bandwidth on source port"));
  }

  @Test
  public void createReservationWithoutEndTime() throws Exception {
    Reservation foreverReservation = new ReservationFactory().setSourcePort(sourcePort).setDestinationPort(destPort)
        .setEndDateTime(null).create();

    CreateReservationScheduleRequestDocument schedule = subject.createSchedule(foreverReservation, true);

    assertThat(schedule.getCreateReservationScheduleRequest().getReservationSchedule()
        .getReservationOccurrenceDuration(), is(Integer.MAX_VALUE));
  }

  @Test
  public void aProtectedResevationShouldCreateAProtectedPath() throws NetworkMonitoringServiceFault {
    Reservation reservation = new ReservationFactory().setSourcePort(sourcePort).setDestinationPort(destPort)
        .withProtection().create();

    PathRequestT path = subject.createPath(reservation);

    assertThat(path.getProtectionType(), is(ValidProtectionTypeT.X_1_PLUS_1_PATH));
  }

  @Test
  public void aUnProtectedReservationShouldCreateAUnProtectedPath() throws NetworkMonitoringServiceFault {
    Reservation reservation = new ReservationFactory().setSourcePort(sourcePort).setDestinationPort(destPort)
        .withoutProtection().create();

    PathRequestT path = subject.createPath(reservation);

    assertThat(path.getProtectionType(), is(ValidProtectionTypeT.UNPROTECTED));
  }
}
