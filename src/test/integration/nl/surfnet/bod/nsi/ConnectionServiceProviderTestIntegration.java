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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;

import javax.annotation.Resource;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.nsi.ws.v1sc.ConnectionServiceProvider;
import nl.surfnet.bod.repo.PhysicalPortRepo;
import nl.surfnet.bod.repo.VirtualPortRepo;
import nl.surfnet.bod.repo.VirtualResourceGroupRepo;
import nl.surfnet.bod.support.MockHttpServer;
import nl.surfnet.bod.support.NsiReservationFactory;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ogf.schemas.nsi._2011._10.connection._interface.GenericAcknowledgmentType;
import org.ogf.schemas.nsi._2011._10.connection._interface.ProvisionRequestType;
import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericRequestType;
import org.ogf.schemas.nsi._2011._10.connection.types.PathType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceTerminationPointType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring/appCtx.xml", "/spring/appCtx-jpa-integration.xml",
    "/spring/appCtx-nbi-client.xml", "/spring/appCtx-idd-client.xml" })
@TransactionConfiguration(defaultRollback = true, transactionManager = "transactionManager")
public class ConnectionServiceProviderTestIntegration extends AbstractTransactionalJUnit4SpringContextTests {

  private static MockHttpServer requesterEndpoint = new MockHttpServer(NsiReservationFactory.PORT);

  @Resource(name = "nsiProvider_v1_sc")
  private ConnectionServiceProvider nsiProvider;

  @Resource
  private VirtualPortRepo virtualPortRepo;

  @Resource
  private PhysicalPortRepo physicalPortRepo;

  @Resource
  private VirtualResourceGroupRepo virtualResourceGroupRepo;

  private final String correlationId = "urn:uuid:f32cc82e-4d87-45ab-baab-4b7011652a2e";

  private final String virtualResourceGroupName = "nsi:group";

  private final String sourceLabel = "00-20-D8-DF-33-59_ETH-1-1-4";
  private final String destinationLabel = "00-20-D8-DF-33-86_ETH-1-13-1";

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

    final VirtualResourceGroup virtualResourceGroup = new VirtualResourceGroup();
    virtualResourceGroup.setDescription("description");
    virtualResourceGroup.setName(virtualResourceGroupName);
    // final VirtualPort[] ports = new VirtualPort[]{sourcePort,
    // destinationPort};
    // virtualResourceGroup.setVirtualPorts(new
    // HashSet<VirtualPort>(Arrays.asList(ports)));
    virtualResourceGroup.setSurfconextGroupId("some:surf:conext:group:id");
    VirtualResourceGroup savedVirtualResourceGroup = virtualResourceGroupRepo.save(virtualResourceGroup);

    final PhysicalPort ppSource = new PhysicalPort();
    ppSource.setManagerLabel(sourceLabel);
    ppSource.setNocLabel(sourceLabel);
    ppSource.setBodPortId(sourceLabel);
    ppSource.setNmsPortId(sourceLabel);
    final PhysicalPort savedSourcePp = physicalPortRepo.save(ppSource);

    final PhysicalPort ppDest = new PhysicalPort();
    ppDest.setManagerLabel(destinationLabel);
    ppDest.setNocLabel(destinationLabel);
    ppDest.setBodPortId(destinationLabel);
    ppDest.setNmsPortId(destinationLabel);
    final PhysicalPort savedDestinationPp = physicalPortRepo.save(ppDest);

    final VirtualPort sourcePort = new VirtualPort();
    sourcePort.setUserLabel(sourceLabel);
    sourcePort.setManagerLabel(sourceLabel);
    sourcePort.setMaxBandwidth(100);
    sourcePort.setPhysicalPort(savedSourcePp);
    sourcePort.setVirtualResourceGroup(savedVirtualResourceGroup);
    final VirtualPort savedSourcePort = virtualPortRepo.save(sourcePort);

    final VirtualPort destinationPort = new VirtualPort();
    destinationPort.setUserLabel(destinationLabel);
    destinationPort.setManagerLabel(destinationLabel);
    destinationPort.setMaxBandwidth(100);
    destinationPort.setPhysicalPort(savedDestinationPp);
    destinationPort.setVirtualResourceGroup(savedVirtualResourceGroup);

    final VirtualPort savedDestination = virtualPortRepo.save(destinationPort);
    final VirtualPort[] ports = new VirtualPort[] { savedSourcePort, savedDestination };
    savedVirtualResourceGroup.setVirtualPorts(new HashSet<VirtualPort>(Arrays.asList(ports)));
    savedVirtualResourceGroup = virtualResourceGroupRepo.save(virtualResourceGroup);
    savedVirtualResourceGroup.setSurfconextGroupId("some:surf:conext:group:id");
    virtualResourceGroupRepo.save(savedVirtualResourceGroup);
  }

  @Test
  public void should_return_generic_acknowledgement() throws Exception {
    final XMLGregorianCalendar startTime = DatatypeFactory.newInstance().newXMLGregorianCalendar();
    startTime.setDay(Calendar.getInstance().get(Calendar.DATE) + 7);
    startTime.setMonth(Calendar.getInstance().get(Calendar.MONTH));
    startTime.setYear(Calendar.getInstance().get(Calendar.YEAR) + 1);

    final XMLGregorianCalendar endTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(
        startTime.toGregorianCalendar());
    endTime.setDay(startTime.getDay() + 1);

    final PathType path = new PathType();

    final ServiceTerminationPointType dest = new ServiceTerminationPointType();
    dest.setStpId(destinationLabel);
    path.setDestSTP(dest);

    final ServiceTerminationPointType source = new ServiceTerminationPointType();
    source.setStpId(sourceLabel);
    path.setSourceSTP(source);

    final ReserveRequestType reservationRequest = new NsiReservationFactory().setScheduleStartTime(startTime)
        .setScheduleEndTime(endTime).setCorrelationId(correlationId).setProviderNsa("urn:ogf:network:nsa:netherlight")
        .setPath(path).createReservation();

    final GenericAcknowledgmentType genericAcknowledgmentType = nsiProvider.reserve(reservationRequest);

    assertThat(genericAcknowledgmentType.getCorrelationId(), is(reservationRequest.getCorrelationId()));

    final ProvisionRequestType provisionRequestType = new ProvisionRequestType();
    provisionRequestType.setCorrelationId(correlationId);
    provisionRequestType.setReplyTo(reservationRequest.getReplyTo());
    final GenericRequestType genericRequestType = new GenericRequestType();
    genericRequestType.setProviderNSA(reservationRequest.getReserve().getProviderNSA());
    genericRequestType.setRequesterNSA(reservationRequest.getReserve().getRequesterNSA());
    genericRequestType.setConnectionId(reservationRequest.getReserve().getReservation().getConnectionId());
    provisionRequestType.setProvision(genericRequestType);


    final GenericAcknowledgmentType provisionAck = nsiProvider.provision(provisionRequestType);
    assertThat(provisionAck.getCorrelationId(), is(reservationRequest.getCorrelationId()));

  }
}
