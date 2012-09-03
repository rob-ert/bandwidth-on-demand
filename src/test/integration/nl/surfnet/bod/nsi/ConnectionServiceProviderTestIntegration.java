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
package nl.surfnet.bod.nsi;

import java.util.Calendar;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.Resource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.nsi.ws.v1sc.ConnectionServiceProviderListener;
import nl.surfnet.bod.nsi.ws.v1sc.ConnectionServiceProviderWs;
import nl.surfnet.bod.repo.ConnectionRepo;
import nl.surfnet.bod.repo.InstituteRepo;
import nl.surfnet.bod.repo.PhysicalPortRepo;
import nl.surfnet.bod.repo.PhysicalResourceGroupRepo;
import nl.surfnet.bod.repo.VirtualPortRepo;
import nl.surfnet.bod.repo.VirtualResourceGroupRepo;
import nl.surfnet.bod.service.ReservationEventPublisher;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.ReservationStatusChangeEvent;
import nl.surfnet.bod.support.ConnectionServiceProviderFactory;
import nl.surfnet.bod.support.MockHttpServer;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.ReserveRequestTypeFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ogf.schemas.nsi._2011._10.connection._interface.GenericAcknowledgmentType;
import org.ogf.schemas.nsi._2011._10.connection._interface.ProvisionRequestType;
import org.ogf.schemas.nsi._2011._10.connection._interface.QueryRequestType;
import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;
import org.ogf.schemas.nsi._2011._10.connection._interface.TerminateRequestType;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericRequestType;
import org.ogf.schemas.nsi._2011._10.connection.types.PathType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceTerminationPointType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import static nl.surfnet.bod.nsi.ws.ConnectionServiceProvider.URN_PROVIDER_NSA;
import static nl.surfnet.bod.nsi.ws.ConnectionServiceProvider.URN_STP;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring/appCtx.xml", "/spring/appCtx-jpa-integration.xml",
    "/spring/appCtx-nbi-client.xml", "/spring/appCtx-idd-client.xml" })
@TransactionConfiguration(defaultRollback = true, transactionManager = "transactionManager")
public class ConnectionServiceProviderTestIntegration extends AbstractTransactionalJUnit4SpringContextTests {

  private static MockHttpServer requesterEndpoint = new MockHttpServer(ConnectionServiceProviderFactory.PORT);

  @Resource
  private ConnectionServiceProviderWs nsiProvider;

  @Resource
  private VirtualPortRepo virtualPortRepo;

  @Resource
  private PhysicalPortRepo physicalPortRepo;

  @Resource
  private VirtualResourceGroupRepo virtualResourceGroupRepo;

  @Resource
  private PhysicalResourceGroupRepo physicalResourceGroupRepo;

  @Resource
  private InstituteRepo instituteRepo;

  @Resource
  private ConnectionRepo connectionRepo;

  @Resource
  private ReservationService reservationService;

  private final String correlationId = "urn:uuid:f32cc82e-4d87-45ab-baab-4b7011652a2e";
  private final String connectionId = "urn:uuid:f32cc82e-4d87-45ab-baab-4b7011652a2f";
  private final String virtualResourceGroupName = "nsi:group";
  private VirtualPort sourceVirtualPort;
  private VirtualPort destinationVirtualPort;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    requesterEndpoint.addResponse("/bod/nsi/requester", new ClassPathResource(
        "web/services/nsi/mockNsiReservationFailedResponse.xml"));
    requesterEndpoint.startServer();
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    requesterEndpoint.stopServer();
  }

  @Before
  public void setup() {
    VirtualResourceGroup virtualResourceGroup = new VirtualResourceGroupFactory().setName(virtualResourceGroupName)
        .create();
    virtualResourceGroup = virtualResourceGroupRepo.save(virtualResourceGroup);

    Institute institute = instituteRepo.findAll().get(0);
    PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().setInstitute(institute).create();
    physicalResourceGroup = physicalResourceGroupRepo.save(physicalResourceGroup);

    PhysicalPort savedSourcePp = physicalPortRepo.save(new PhysicalPortFactory().setPhysicalResourceGroup(
        physicalResourceGroup).create());
    PhysicalPort savedDestinationPp = physicalPortRepo.save(new PhysicalPortFactory().setPhysicalResourceGroup(
        physicalResourceGroup).create());

    VirtualPort sourcePort = new VirtualPortFactory().setMaxBandwidth(100).setPhysicalPort(savedSourcePp)
        .setVirtualResourceGroup(virtualResourceGroup).create();
    sourceVirtualPort = virtualPortRepo.save(sourcePort);

    VirtualPort destinationPort = new VirtualPortFactory().setMaxBandwidth(100).setPhysicalPort(savedDestinationPp)
        .setVirtualResourceGroup(virtualResourceGroup).create();
    destinationVirtualPort = virtualPortRepo.save(destinationPort);

    virtualResourceGroup.setVirtualPorts(Lists.newArrayList(sourceVirtualPort, destinationVirtualPort));
    virtualResourceGroup.setSurfconextGroupId("some:surf:conext:group:id");
    virtualResourceGroup = virtualResourceGroupRepo.save(virtualResourceGroup);
  }

  @Test
  public void shouldValidateReserve() throws DatatypeConfigurationException, ServiceException {
    ReserveRequestType reservationRequest = createReserveRequest();

    GenericAcknowledgmentType genericAcknowledgment = nsiProvider.reserve(reservationRequest);

    assertThat(genericAcknowledgment.getCorrelationId(), is(correlationId));
  }

  @Test
  public void shouldValidateQuery() throws ServiceException {
    QueryRequestType queryRequest = createQueryRequest();

    GenericAcknowledgmentType genericAcknowledgment = nsiProvider.query(queryRequest);

    assertThat(genericAcknowledgment.getCorrelationId(), is(correlationId));
  }

  @Test
  public void shouldValidateTerminate() throws ServiceException, DatatypeConfigurationException {
    // First reserve
    ReserveRequestType reservationRequest = createReserveRequest();
    nsiProvider.reserve(reservationRequest);

    // Then terminate
    TerminateRequestType terminateRequest = createTerminateRequest(reservationRequest.getReserve().getReservation()
        .getConnectionId(), reservationRequest.getCorrelationId());

    GenericAcknowledgmentType terminateAck = nsiProvider.terminate(terminateRequest);

    // Assert
    assertThat(terminateAck.getCorrelationId(), is(correlationId));
  }

  @Test
  public void shouldValidateProvision() throws ServiceException, DatatypeConfigurationException {
    // Setup
    // First reserve
    ReserveRequestType reservationRequest = createReserveRequest();
    nsiProvider.reserve(reservationRequest);

    ProvisionRequestType provisionRequest = createProvisionRequest(reservationRequest.getReserve().getReservation()
        .getConnectionId(), reservationRequest.getCorrelationId());

    // Execute
    GenericAcknowledgmentType provisionAck = nsiProvider.provision(provisionRequest);

    // Verify
    assertThat(provisionAck.getCorrelationId(), is(correlationId));
  }

  @Test
  public void shouldReturnGenericAcknowledgement() throws Exception {
    ReserveRequestType reservationRequest = createReserveRequest();

    // send reserve request
    GenericAcknowledgmentType genericAcknowledgmentType = nsiProvider.reserve(reservationRequest);

    assertThat(genericAcknowledgmentType.getCorrelationId(), is(correlationId));

    // send provision request
    final ProvisionRequestType provisionRequestType = new ProvisionRequestType();
    provisionRequestType.setCorrelationId(correlationId);
    provisionRequestType.setReplyTo(reservationRequest.getReplyTo());
    final GenericRequestType genericRequestType = new GenericRequestType();
    genericRequestType.setProviderNSA(reservationRequest.getReserve().getProviderNSA());
    genericRequestType.setRequesterNSA(reservationRequest.getReserve().getRequesterNSA());
    genericRequestType.setConnectionId(reservationRequest.getReserve().getReservation().getConnectionId());
    provisionRequestType.setProvision(genericRequestType);

    final GenericAcknowledgmentType provisionAck = nsiProvider.provision(provisionRequestType);
    assertThat(provisionAck.getCorrelationId(), is(correlationId));
  }

  @Test
  @Ignore("transaction issues")
  public void shouldTerminateWhenCancelledByGUI() throws ServiceException, DatatypeConfigurationException,
      InterruptedException, ExecutionException {
    RichUserDetails user = new RichUserDetailsFactory().addNocRole().create();

    // First reserve
    ReserveRequestType reservationRequest = createReserveRequest();
    nsiProvider.reserve(reservationRequest);

    String connectionId = reservationRequest.getReserve().getReservation().getConnectionId();
    Connection connection = connectionRepo.findByConnectionId(connectionId);

    assertThat(connection.getCurrentState(), isOneOf(ConnectionStateType.RESERVED, ConnectionStateType.RESERVING));
    assertThat(connection.getReservation(), notNullValue());

    // Then terminate
    Optional<Future<Long>> cancelFuture = reservationService.cancel(connection.getReservation(), user);
    // Wait for cancel to finish
    cancelFuture.get().get();

    // Manually notify listener
    setupListener().notifyListeners(
        new ReservationStatusChangeEvent(ReservationStatus.RESERVED, connection.getReservation()));

    // Verify
    // connection =
    // connectionServiceProviderService.findByConnectionId(connectionId);
    assertThat(connection.getConnectionId(), is(connectionId));
    assertThat(connection.getCurrentState(), isOneOf(ConnectionStateType.TERMINATED, ConnectionStateType.TERMINATING));
    assertThat(connection.getReservation().getStatus(), is(ReservationStatus.CANCELLED));
  }

  private ReservationEventPublisher setupListener() {
    ReservationEventPublisher publisher = new ReservationEventPublisher();
    publisher.addListener(new ConnectionServiceProviderListener());

    return publisher;
  }

  private ReserveRequestType createReserveRequest() throws DatatypeConfigurationException {
    XMLGregorianCalendar startTime = DatatypeFactory.newInstance().newXMLGregorianCalendar();
    startTime.setDay(1);
    startTime.setMonth(Calendar.getInstance().get(Calendar.MONTH));
    startTime.setYear(Calendar.getInstance().get(Calendar.YEAR) + 1);

    XMLGregorianCalendar endTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(
        startTime.toGregorianCalendar());
    endTime.setDay(2);
    endTime.setMonth(Calendar.getInstance().get(Calendar.MONTH));
    endTime.setYear(Calendar.getInstance().get(Calendar.YEAR) + 1);

    PathType path = new PathType();

    ServiceTerminationPointType dest = new ServiceTerminationPointType();
    dest.setStpId(URN_STP + ":" + sourceVirtualPort.getId());
    path.setDestSTP(dest);

    ServiceTerminationPointType source = new ServiceTerminationPointType();
    source.setStpId(URN_STP + ":" + destinationVirtualPort.getId());
    path.setSourceSTP(source);

    ReserveRequestType reservationRequest = new ReserveRequestTypeFactory().setScheduleStartTime(startTime)
        .setScheduleEndTime(endTime).setConnectionId(connectionId).setCorrelationId(correlationId)
        .setConnectionId(connectionId).setProviderNsa(URN_PROVIDER_NSA).setPath(path).create();

    return reservationRequest;
  }

  private QueryRequestType createQueryRequest() {
    return new ConnectionServiceProviderFactory().setConnectionId(connectionId).setCorrelationId(correlationId)
        .setProviderNsa(URN_PROVIDER_NSA).createQueryRequest();

  }

  private TerminateRequestType createTerminateRequest(String connId, String corrId) {
    return new ConnectionServiceProviderFactory().setCorrelationId(corrId).setConnectionId(connId)
        .setProviderNsa(URN_PROVIDER_NSA).createTerminateRequest();
  }

  private ProvisionRequestType createProvisionRequest(String connId, String corrId) {
    return new ConnectionServiceProviderFactory().setConnectionId(connId).setCorrelationId(corrId)
        .setProviderNsa(URN_PROVIDER_NSA).createProvisionRequest();
  }

}
