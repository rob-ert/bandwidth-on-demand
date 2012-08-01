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

import static nl.surfnet.bod.nsi.ws.v1sc.ConnectionServiceProviderFunctions.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.hamcrest.text.IsEmptyString;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.BandwidthType;
import org.ogf.schemas.nsi._2011._10.connection.types.PathType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReservationInfoType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReserveType;
import org.ogf.schemas.nsi._2011._10.connection.types.ScheduleType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceParametersType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceTerminationPointType;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.repo.ConnectionRepo;
import nl.surfnet.bod.service.ConnectionServiceProviderService;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.support.ConnectionFactory;
import nl.surfnet.bod.support.VirtualPortFactory;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionServiceProviderTest {

  @InjectMocks
  private ConnectionServiceProviderWs subject;

  @Mock
  private ConnectionRepo connectionRepoMock;
  
  @Mock
  private VirtualPortService virtualPortServiceMock;
  
  @Mock
  private ReservationService reservationServiceMock;
  
  @Mock
  private ConnectionServiceProviderService connectionServiceProviderComponent;

  private String nsaProvider = "nsa:surfnet.nl";

  @Before
  public void setup() {
    subject.addNsaProvider(nsaProvider);
  }

  @Test(expected = ServiceException.class)
  public void shouldComplainAboutTheProviderNsa() throws ServiceException {
    NsiRequestDetails request = new NsiRequestDetails("http://localhost", "123456");
    Connection connection = new ConnectionFactory()
      .setSourceStpId("Source Port").setDestinationStpId("Destination Port")
      .setProviderNSA("non:existingh").create();

    VirtualPort sourcePort = new VirtualPortFactory().create();
    VirtualPort destinationPort = new VirtualPortFactory().setVirtualResourceGroup(sourcePort.getVirtualResourceGroup()).create();

    when(virtualPortServiceMock.findByNsiStpId("Source Port")).thenReturn(sourcePort);
    when(virtualPortServiceMock.findByNsiStpId("Destination Port")).thenReturn(destinationPort);

    subject.reserve(connection, request);
  }

  @Test(expected = ServiceException.class)
  public void shouldComplainAboutNonExistingPort() throws ServiceException {
    NsiRequestDetails request = new NsiRequestDetails("http://localhost", "123456");
    Connection connection = new ConnectionFactory()
      .setSourceStpId("Source Port").setDestinationStpId("Destination Port")
      .setProviderNSA(nsaProvider).create();

    VirtualPort sourcePort = new VirtualPortFactory().create();

    when(virtualPortServiceMock.findByNsiStpId("Source Port")).thenReturn(sourcePort);
    when(virtualPortServiceMock.findByNsiStpId("Destination Port")).thenReturn(null);

    subject.reserve(connection, request);
  }

  @Test
  public void shouldMakeAReservation() throws ServiceException {
    NsiRequestDetails request = new NsiRequestDetails("http://localhost", "123456");
    Connection connection = new ConnectionFactory()
      .setSourceStpId("Source Port").setDestinationStpId("Destination Port")
      .setProviderNSA(nsaProvider).create();

    VirtualPort sourcePort = new VirtualPortFactory().create();
    VirtualPort destinationPort = new VirtualPortFactory().setVirtualResourceGroup(sourcePort.getVirtualResourceGroup()).create();

    when(virtualPortServiceMock.findByNsiStpId("Source Port")).thenReturn(sourcePort);
    when(virtualPortServiceMock.findByNsiStpId("Destination Port")).thenReturn(destinationPort);

    subject.reserve(connection, request);
  }

  @Test
  public void reserveTypeWithoutGlobalReservationIdShouldGetOne() {
    ReserveRequestType reserveRequestType = createReservationRequestType(1000, Optional.<String>absent());

    Connection connection = RESERVE_REQUEST_TO_CONNECTION.apply(reserveRequestType);

    assertThat(connection.getGlobalReservationId(), not(IsEmptyString.isEmptyOrNullString()));
    assertThat(connection.getDesiredBandwidth(), is(1000));
  }

  @Test
  public void reserveTypeWithGlobalReservationId() {
    ReserveRequestType reserveRequestType = createReservationRequestType(1000, Optional.of("urn:surfnet.nl:12345"));

    Connection connection = RESERVE_REQUEST_TO_CONNECTION.apply(reserveRequestType);

    assertThat(connection.getGlobalReservationId(), is("urn:surfnet.nl:12345"));
  }

  private ReserveRequestType createReservationRequestType(int desiredBandwidth, Optional<String> globalReservationId) {
    ServiceTerminationPointType dest = new ServiceTerminationPointType();
    dest.setStpId("urn:stp:1");

    ServiceTerminationPointType source = new ServiceTerminationPointType();
    source.setStpId("urn:stp:1");

    PathType path = new PathType();
    path.setDestSTP(dest);
    path.setSourceSTP(source);

    ScheduleType schedule = new ScheduleType();
    XMLGregorianCalendar xmlGregorianCalendar;
    try {
      xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar();
      schedule.setStartTime(xmlGregorianCalendar);
      schedule.setEndTime(xmlGregorianCalendar);
    }
    catch (DatatypeConfigurationException e) {
      Throwables.propagate(e);
    }

    BandwidthType bandwidth = new BandwidthType();
    bandwidth.setDesired(desiredBandwidth);

    ServiceParametersType serviceParameters = new ServiceParametersType();
    serviceParameters.setSchedule(schedule);
    serviceParameters.setBandwidth(bandwidth);

    ReservationInfoType reservation = new ReservationInfoType();
    reservation.setPath(path);
    reservation.setServiceParameters(serviceParameters);
    reservation.setGlobalReservationId(globalReservationId.orNull());

    ReserveType reserve = new ReserveType();
    reserve.setReservation(reservation);

    ReserveRequestType reserveRequestType = new ReserveRequestType();
    reserveRequestType.setReserve(reserve);

    return reserveRequestType;
  }

}
