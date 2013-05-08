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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import nl.surfnet.bod.domain.ConnectionV2;
import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.repo.ConnectionV2Repo;
import nl.surfnet.bod.util.XmlUtils;

import org.ogf.schemas.nsi._2013._04.connection.requester.ConnectionRequesterPort;
import org.ogf.schemas.nsi._2013._04.connection.requester.ConnectionServiceRequester;
import org.ogf.schemas.nsi._2013._04.connection.requester.ServiceException;
import org.ogf.schemas.nsi._2013._04.connection.types.ConnectionStatesType;
import org.ogf.schemas.nsi._2013._04.connection.types.DirectionalityType;
import org.ogf.schemas.nsi._2013._04.connection.types.PathType;
import org.ogf.schemas.nsi._2013._04.connection.types.QuerySummaryResultType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReservationConfirmCriteriaType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReservationStateEnumType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReservationStateType;
import org.ogf.schemas.nsi._2013._04.connection.types.ScheduleType;
import org.ogf.schemas.nsi._2013._04.connection.types.StpType;
import org.ogf.schemas.nsi._2013._04.framework.headers.CommonHeaderType;
import org.ogf.schemas.nsi._2013._04.framework.types.TypeValuePairListType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

@Component("connectionServiceRequesterV2")
public class ConnectionServiceRequesterV2Callback {

  private static final String WSDL_LOCATION = "/wsdl/2.0/ogf_nsi_connection_requester_v2_0.wsdl";

  private final Logger log = LoggerFactory.getLogger(ConnectionServiceRequesterV2Callback.class);

  @Resource private ConnectionV2Repo connectionRepo;

  public void reserveConfirmed(ConnectionV2 connection, NsiRequestDetails requestDetails) {
    log.info("Sending a reserveConfirmed on endpoint: {} with id: {}", requestDetails.getReplyTo(), connection.getGlobalReservationId());

    connection.setCurrentState(ReservationStateEnumType.RESERVE_HELD);
    connectionRepo.save(connection);

    ReservationConfirmCriteriaType criteria = new ReservationConfirmCriteriaType()
      .withBandwidth(connection.getDesiredBandwidth())
      .withPath(new PathType()
        .withSourceSTP(toStpType(connection.getSourceStpId()))
        .withDestSTP(toStpType(connection.getDestinationStpId()))
        .withDirectionality(DirectionalityType.BIDIRECTIONAL))
      .withSchedule(new ScheduleType()
        .withEndTime(XmlUtils.toGregorianCalendar(connection.getEndTime().get()))
        .withStartTime(XmlUtils.toGregorianCalendar(connection.getStartTime().get())))
      .withServiceAttributes(new TypeValuePairListType())
      .withVersion(0);

    Holder<CommonHeaderType> headerHolder = createHeader(requestDetails, connection.getProviderNsa(), connection.getRequesterNsa());

    ConnectionRequesterPort port = createPort(requestDetails);
    try {
      port.reserveConfirmed(
        connection.getGlobalReservationId(),
        connection.getDescription(),
        connection.getConnectionId(),
        ImmutableList.of(criteria),
        headerHolder);
    } catch (ServiceException e) {
      log.info("Sending reserve confirmed failed", e);
    }
  }

  private StpType toStpType(String sourceStpId) {
    String[] parts = sourceStpId.split(":");
    String networkId = Joiner.on(":").join(Arrays.copyOfRange(parts, 0, parts.length - 2));
    return new StpType()
      .withNetworkId(networkId)
      .withLocalId(parts[parts.length - 1]);
  }

  public void abortConfirmed(ConnectionV2 connection, NsiRequestDetails requestDetails) {
    connection.setCurrentState(ReservationStateEnumType.RESERVED);
    connectionRepo.save(connection);

    ConnectionRequesterPort port = createPort(requestDetails);
    try {
      port.reserveAbortConfirmed(
        connection.getGlobalReservationId(),
        connection.getConnectionId(),
        createHeader(requestDetails, "requester", "provider"));
    } catch (ServiceException e) {
      log.info("Sending reserve abort confirmed failed", e);
    }
  }

  public void reserveCommitConfirmed(ConnectionV2 connection, NsiRequestDetails requestDetails) {
    ConnectionRequesterPort port = createPort(requestDetails);

    try {
      port.reserveCommitConfirmed(
        connection.getGlobalReservationId(),
        connection.getConnectionId(),
        createHeader(requestDetails, "requester", "provider"));
    } catch (ServiceException e) {
      log.info("", e);
    }
  }

  public void querySummaryConfirmed(List<ConnectionV2> connections, NsiRequestDetails requestDetails) {
    List<QuerySummaryResultType> results = new ArrayList<>();

    for (ConnectionV2 connection : connections) {
      results.add(new QuerySummaryResultType()
        .withConnectionId(connection.getConnectionId())
        .withConnectionStates(new ConnectionStatesType()
          .withReservationState(new ReservationStateType().withState(connection.getCurrentState()).withVersion(0))));
    }

    ConnectionRequesterPort port = createPort(requestDetails);
    try {
      port.querySummaryConfirmed(results, createHeader(requestDetails, "provider", "requester"));
    } catch (ServiceException e) {
      log.info("Failed to send query summary confirmed", e);
    }
  }

  private Holder<CommonHeaderType> createHeader(NsiRequestDetails requestDetails, String providerNsa, String requesterNsa) {
    return new Holder<>(new CommonHeaderType()
      .withCorrelationId(requestDetails.getCorrelationId())
      .withProtocolVersion("urn:2.0:FIXME")
      .withProviderNSA(providerNsa)
      .withRequesterNSA(requesterNsa));
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
