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

import static com.jayway.awaitility.Awaitility.await;
import static nl.surfnet.bod.nsi.ws.ConnectionServiceProvider.URN_PROVIDER_NSA;
import static nl.surfnet.bod.nsi.ws.ConnectionServiceProvider.URN_STP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.xml.datatype.DatatypeConfigurationException;

import nl.surfnet.bod.domain.*;
import nl.surfnet.bod.nsi.ws.v1sc.ConnectionServiceProviderWs;
import nl.surfnet.bod.repo.*;
import nl.surfnet.bod.service.ReservationEventPublisher;
import nl.surfnet.bod.service.ReservationListener;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.ReservationStatusChangeEvent;
import nl.surfnet.bod.support.*;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ogf.schemas.nsi._2011._10.connection._interface.*;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.ogf.schemas.nsi._2011._10.connection.types.PathType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceTerminationPointType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring/appCtx.xml", "/spring/appCtx-jpa-integration.xml",
    "/spring/appCtx-nbi-client.xml", "/spring/appCtx-idd-client.xml" })
@TransactionConfiguration(defaultRollback = false, transactionManager = "transactionManager")
@Transactional
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
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

  @Resource
  private ReservationEventPublisher reservationEventPublisher;

  @PersistenceContext
  private EntityManager entityManager;

  @Resource
  private EntityManagerFactory entityManagerFactory;

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

  @BeforeTransaction
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

  @AfterTransaction
  public void teardown() {
    EntityManager em = entityManagerFactory.createEntityManager();
    SQLQuery query = ((Session) em.getDelegate())
        .createSQLQuery("truncate physical_resource_group, virtual_resource_group, connection cascade;");
    query.executeUpdate();
  }

  @Test
  public void shouldReserveProvisionTerminate() throws Exception {
    ReserveRequestType reservationRequest = createReserveRequest();
    final String connectionId = reservationRequest.getReserve().getReservation().getConnectionId();
    final String reserveCorrelationId = reservationRequest.getCorrelationId();

    final DummyReservationListener listener = new DummyReservationListener();
    reservationEventPublisher.addListener(listener);

    GenericAcknowledgmentType reserveAcknowledgment = nsiProvider.reserve(reservationRequest);

    assertThat(reserveAcknowledgment.getCorrelationId(), is(reserveCorrelationId));

    final Connection connection = connectionRepo.findByConnectionId(connectionId);
    assertThat(connection.getConnectionId(), is(connectionId));

    listener.waitForEventWithNewStatus(ReservationStatus.RESERVED);

    Reservation reservation = reservationService.find(connection.getReservation().getId());
    entityManager.refresh(reservation);
    entityManager.refresh(connection);

    assertThat(reservation.getStatus(), is(ReservationStatus.RESERVED));
    assertThat(connection.getCurrentState(), is(ConnectionStateType.RESERVED));

    ProvisionRequestType provisionRequest = createProvisionRequest(connectionId);

    GenericAcknowledgmentType provisionAck = nsiProvider.provision(provisionRequest);
    final String provisionCorrelationId = provisionAck.getCorrelationId();

    assertThat(provisionAck.getCorrelationId(), is(provisionCorrelationId));

    listener.waitForEventWithNewStatus(ReservationStatus.SCHEDULED);

    entityManager.refresh(reservation);
    entityManager.refresh(connection);
    assertThat(reservation.getStatus(), is(ReservationStatus.SCHEDULED));
    assertThat(connection.getCurrentState(), is(ConnectionStateType.AUTO_PROVISION));

    TerminateRequestType terminateRequest = createTerminateRequest(connectionId);
    GenericAcknowledgmentType terminateAck = nsiProvider.terminate(terminateRequest);
    final String terminateCorrelationId = terminateAck.getCorrelationId();

    assertThat(terminateCorrelationId, is(terminateRequest.getCorrelationId()));

    listener.waitForEventWithNewStatus(ReservationStatus.CANCELLED);

    entityManager.refresh(reservation);
    entityManager.refresh(connection);
    assertThat(reservation.getStatus(), is(ReservationStatus.CANCELLED));
    assertThat(connection.getCurrentState(), is(ConnectionStateType.TERMINATED));
  }

  private class DummyReservationListener implements ReservationListener {
    private Optional<ReservationStatusChangeEvent> event = Optional.absent();

    @Override
    public void onStatusChange(ReservationStatusChangeEvent event) {
      this.event = Optional.of(event);
    }

    public Optional<ReservationStatusChangeEvent> getEvent() {
      return event;
    }

    public void reset() {
      event = Optional.absent();
    }

    public void waitForEventWithNewStatus(final ReservationStatus status) throws Exception {
      await().atMost(10, TimeUnit.SECONDS).until(new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
          return getEvent().isPresent()
              && getEvent().get().getNewStatus().equals(status);
        }
      });

      reset();
    }

  }

  @Test
  public void shouldQuery() throws ServiceException {
    QueryRequestType queryRequest = createQueryRequest();

    GenericAcknowledgmentType genericAcknowledgment = nsiProvider.query(queryRequest);

    assertThat(genericAcknowledgment.getCorrelationId(), is(queryRequest.getCorrelationId()));


  }

  @Test
  public void shouldTerminateWhenCancelledByGUI() throws Exception {
    final DummyReservationListener listener = new DummyReservationListener();
    reservationEventPublisher.addListener(listener);

    ReserveRequestType reservationRequest = createReserveRequest();
    final String connectionId = reservationRequest.getReserve().getReservation().getConnectionId();

    nsiProvider.reserve(reservationRequest);

    listener.waitForEventWithNewStatus(ReservationStatus.RESERVED);

    Connection connection = connectionRepo.findByConnectionId(connectionId);
    reservationService.cancel(connection.getReservation(), new RichUserDetailsFactory().addNocRole().create());

    listener.waitForEventWithNewStatus(ReservationStatus.CANCELLED);

    Reservation reservation = connection.getReservation();
    entityManager.refresh(connection);
    entityManager.refresh(reservation);

    assertThat(connection.getCurrentState(), is(ConnectionStateType.TERMINATED));
    assertThat(reservation.getStatus(), is(ReservationStatus.CANCELLED));
  }

  private ReserveRequestType createReserveRequest() throws DatatypeConfigurationException {
    PathType path = new PathType();

    ServiceTerminationPointType dest = new ServiceTerminationPointType();
    dest.setStpId(URN_STP + ":" + sourceVirtualPort.getId());
    path.setDestSTP(dest);

    ServiceTerminationPointType source = new ServiceTerminationPointType();
    source.setStpId(URN_STP + ":" + destinationVirtualPort.getId());
    path.setSourceSTP(source);

    ReserveRequestType reservationRequest = new ReserveRequestTypeFactory()
       .setProviderNsa(URN_PROVIDER_NSA).setPath(path).create();

    return reservationRequest;
  }

  private QueryRequestType createQueryRequest() {
    return new ConnectionServiceProviderFactory().setProviderNsa(URN_PROVIDER_NSA).createQueryRequest();

  }

  private TerminateRequestType createTerminateRequest(String connId) {
    return new ConnectionServiceProviderFactory().setConnectionId(connId).setProviderNsa(URN_PROVIDER_NSA)
        .createTerminateRequest();
  }

  private ProvisionRequestType createProvisionRequest(String connId) {
    return new ConnectionServiceProviderFactory().setConnectionId(connId).setProviderNsa(URN_PROVIDER_NSA)
        .createProvisionRequest();
  }

}
