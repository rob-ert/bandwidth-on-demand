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
package nl.surfnet.bod.nsi.v2;

import static com.google.common.collect.Lists.transform;
import static nl.surfnet.bod.nsi.v2.ConnectionsV2.toQuerySummaryResultType;
import static nl.surfnet.bod.nsi.v2.ConnectionsV2.toQueryRecursiveResultType;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import nl.surfnet.bod.domain.ConnectionV2;
import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.nsi.NsiHelper;
import nl.surfnet.bod.repo.ConnectionV2Repo;
import nl.surfnet.bod.util.XmlUtils;

import org.joda.time.DateTime;
import org.ogf.schemas.nsi._2013._04.connection.requester.ConnectionRequesterPort;
import org.ogf.schemas.nsi._2013._04.connection.requester.ConnectionServiceRequester;
import org.ogf.schemas.nsi._2013._04.connection.requester.ServiceException;
import org.ogf.schemas.nsi._2013._04.connection.types.DataPlaneStatusType;
import org.ogf.schemas.nsi._2013._04.connection.types.LifecycleStateEnumType;
import org.ogf.schemas.nsi._2013._04.connection.types.ProvisionStateEnumType;
import org.ogf.schemas.nsi._2013._04.connection.types.QueryRecursiveResultType;
import org.ogf.schemas.nsi._2013._04.connection.types.QuerySummaryResultType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReservationConfirmCriteriaType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReservationStateEnumType;
import org.ogf.schemas.nsi._2013._04.framework.headers.CommonHeaderType;
import org.ogf.schemas.nsi._2013._04.framework.types.TypeValuePairListType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;
import com.sun.xml.ws.client.ClientTransportException;

@Component("connectionServiceRequesterV2")
public class ConnectionServiceRequesterV2 {

  private static final String WSDL_LOCATION = "/wsdl/2.0/ogf_nsi_connection_requester_v2_0.wsdl";

  private final Logger log = LoggerFactory.getLogger(ConnectionServiceRequesterV2.class);

  @Resource private ConnectionV2Repo connectionRepo;

  public void reserveConfirmed(Long connectionId, NsiRequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(connectionId);
    connection.setReservationState(ReservationStateEnumType.RESERVE_HELD);
    connectionRepo.save(connection);

    log.info("Sending a reserveConfirmed on endpoint: {} for connectionId: {}", requestDetails.getReplyTo(), connection.getConnectionId());

    ReservationConfirmCriteriaType criteria = new ReservationConfirmCriteriaType()
      .withBandwidth(connection.getDesiredBandwidth())
      .withPath(connection.getPath())
      .withSchedule(connection.getSchedule())
      .withServiceAttributes(new TypeValuePairListType())
      .withVersion(0);

    ConnectionRequesterPort port = createPort(requestDetails);
    try {
      port.reserveConfirmed(
        connection.getConnectionId(),
        connection.getGlobalReservationId(),
        connection.getDescription(),
        ImmutableList.of(criteria),
        new Holder<>(requestDetails.getCommonHeaderType()));
    } catch (ClientTransportException | ServiceException e) {
      log.info("Sending Reserve Confirmed failed", e);
    }
  }

  public void abortConfirmed(Long connectionId, NsiRequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(connectionId);
    connection.setReservationState(ReservationStateEnumType.RESERVE_START);
    connectionRepo.save(connection);

    ConnectionRequesterPort port = createPort(requestDetails);
    try {
      port.reserveAbortConfirmed(connection.getConnectionId(), new Holder<>(requestDetails.getCommonHeaderType()));
    } catch (ServiceException e) {
      log.info("Sending Reserve Abort Confirmed failed", e);
    }
  }

  public void terminateConfirmed(Long connectionId, NsiRequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(connectionId);
    connection.setLifecycleState(LifecycleStateEnumType.TERMINATED);
    connectionRepo.save(connection);

    ConnectionRequesterPort port = createPort(requestDetails);
    try {
      port.terminateConfirmed(connection.getConnectionId(), new Holder<>(requestDetails.getCommonHeaderType()));
    } catch (ClientTransportException | ServiceException e) {
      log.info("Sending Terminate Confirmed failed", e);
    }
  }

  public void reserveAbortConfirmed(Long connectionId, NsiRequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(connectionId);
    connection.setReservationState(ReservationStateEnumType.RESERVE_START);
    connectionRepo.save(connection);

    ConnectionRequesterPort port = createPort(requestDetails);
    try {
      port.reserveAbortConfirmed(connection.getConnectionId(), new Holder<>(requestDetails.getCommonHeaderType()));
    } catch (ClientTransportException | ServiceException e) {
      log.info("Sending Reserve Abort Confirmed failed", e);
    }
  }

  public void reserveCommitConfirmed(Long connectionId, NsiRequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(connectionId);
    connection.setReservationState(ReservationStateEnumType.RESERVE_START);
    connection.setProvisionState(ProvisionStateEnumType.RELEASED);
    connection.setLifecycleState(LifecycleStateEnumType.CREATED);
    connectionRepo.save(connection);

    ConnectionRequesterPort port = createPort(requestDetails);
    try {
      port.reserveCommitConfirmed(connection.getConnectionId(), new Holder<>(requestDetails.getCommonHeaderType()));
    } catch (ClientTransportException | ServiceException e) {
      log.info("Sending Reserve Commit Confirmed failed", e);
    }
  }

  public void provisionConfirmed(Long connectionId, NsiRequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(connectionId);
    connection.setProvisionState(ProvisionStateEnumType.PROVISIONED);
    connectionRepo.save(connection);

    ConnectionRequesterPort port = createPort(requestDetails);
    try {
      port.provisionConfirmed(connection.getConnectionId(), new Holder<>(requestDetails.getCommonHeaderType()));
    } catch (ClientTransportException | ServiceException e) {
      log.info("Sending Provision Confirmed failed", e);
    }
  }

  public void dataPlaneActivated(Long id, NsiRequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(id);
    connection.setDataPlaneActive(true);
    connectionRepo.save(connection);

    sendDataPlaneStatus(requestDetails, connection, DateTime.now());
  }

  public void dataPlaneDeactivated(Long id, NsiRequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(id);
    connection.setDataPlaneActive(false);
    connectionRepo.save(connection);

    sendDataPlaneStatus(requestDetails, connection, DateTime.now());
  }

  private void sendDataPlaneStatus(NsiRequestDetails requestDetails, ConnectionV2 connection, DateTime when) {
    DataPlaneStatusType dataPlaneStatus = new DataPlaneStatusType().withActive(connection.isDataPlaneActive()).withVersion(0).withVersionConsistent(true);
    XMLGregorianCalendar timeStamp = XmlUtils.toGregorianCalendar(when);

    ConnectionRequesterPort port = createPort(requestDetails);
    try {
      CommonHeaderType header = requestDetails.getCommonHeaderType().withCorrelationId(NsiHelper.generateCorrelationId());
      port.dataPlaneStateChange(connection.getConnectionId(), dataPlaneStatus, timeStamp, new Holder<>(header));
    } catch (ClientTransportException | ServiceException e) {
      log.info("Failed to send Data Plane State Change");
    }
  }

  public void querySummaryConfirmed(List<ConnectionV2> connections, NsiRequestDetails requestDetails) {
    List<QuerySummaryResultType> results = transform(connections, toQuerySummaryResultType);

    ConnectionRequesterPort port = createPort(requestDetails);
    try {
      port.querySummaryConfirmed(results, new Holder<>(requestDetails.getCommonHeaderType()));
    } catch (ClientTransportException | ServiceException e) {
      log.info("Failed to send query summary confirmed", e);
    }
  }

  public void queryRecursiveConfirmed(List<ConnectionV2> connections, NsiRequestDetails requestDetails){
    List<QueryRecursiveResultType> result = transform(connections, toQueryRecursiveResultType);

    ConnectionRequesterPort port = createPort(requestDetails);
    try {
      port.queryRecursiveConfirmed(result, new Holder<>(requestDetails.getCommonHeaderType()));
    } catch (ClientTransportException | ServiceException e) {
      log.info("Failed to send query recursive confirmed", e);
    }


  }

  private ConnectionRequesterPort createPort(NsiRequestDetails requestDetails) {
    ConnectionRequesterPort port = new ConnectionServiceRequester(wsdlUrl()).getConnectionServiceRequesterPort();
    ((BindingProvider) port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, requestDetails.getReplyTo());

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
