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

import static nl.surfnet.bod.nsi.ws.ConnectionServiceProvider.URN_PROVIDER_NSA;
import static nl.surfnet.bod.nsi.ws.ConnectionServiceProvider.URN_STP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Calendar;

import javax.annotation.Resource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import nl.surfnet.bod.domain.*;
import nl.surfnet.bod.nsi.ws.v1sc.ConnectionServiceProviderWs;
import nl.surfnet.bod.repo.*;
import nl.surfnet.bod.support.*;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ogf.schemas.nsi._2011._10.connection._interface.*;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericRequestType;
import org.ogf.schemas.nsi._2011._10.connection.types.PathType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceTerminationPointType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.google.common.collect.Lists;

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
