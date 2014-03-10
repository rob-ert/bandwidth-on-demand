/**
 * Copyright (c) 2012, 2013 SURFnet BV
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
package nl.surfnet.bod.nsi.v1sc;

import static com.google.common.base.Preconditions.checkNotNull;
import static nl.surfnet.bod.nsi.ConnectionServiceProviderError.CONNECTION_EXISTS;
import static nl.surfnet.bod.nsi.ConnectionServiceProviderError.UNAUTHORIZED;
import static nl.surfnet.bod.nsi.ConnectionServiceProviderError.UNKNOWN_STP;
import static nl.surfnet.bod.nsi.ConnectionServiceProviderError.UNSUPPORTED_PARAMETER;

import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import java.util.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import nl.surfnet.bod.domain.ConnectionV1;
import nl.surfnet.bod.domain.NsiV1RequestDetails;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationEndPoint;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.nsi.ConnectionServiceProviderError;
import nl.surfnet.bod.nsi.NsiHelper;
import nl.surfnet.bod.repo.ConnectionV1Repo;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.ogf.schemas.nsi._2011._10.connection.types.DetailedPathType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryDetailsResultType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryOperationType;
import org.ogf.schemas.nsi._2011._10.connection.types.QuerySummaryResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class ConnectionServiceV1 {

  private final Logger log = LoggerFactory.getLogger(ConnectionServiceV1.class);

  @Resource private NsiHelper nsiHelper;
  @Resource private ConnectionV1Repo connectionRepo;
  @Resource private ReservationService reservationService;
  @Resource private VirtualPortService virtualPortService;
  @Resource private ConnectionServiceRequesterV1 connectionServiceRequester;

  public void reserve(ConnectionV1 connection, NsiV1RequestDetails requestDetails, boolean autoProvision, RichUserDetails userDetails) throws ValidationException {
    checkConnection(connection, userDetails);

    connection.setReserveRequestDetails(requestDetails);
    connection.setCurrentState(ConnectionStateType.RESERVING);
    connection = connectionRepo.saveAndFlush(connection);

    VirtualPort sourcePort = virtualPortService.findByNsiV1StpId(connection.getSourceStpId());
    VirtualPort destinationPort = virtualPortService.findByNsiV1StpId(connection.getDestinationStpId());

    Reservation reservation = new Reservation();
    reservation.setConnectionV1(connection);
    reservation.setName(connection.getDescription());
    reservation.setStartDateTime(connection.getStartTime().orElse(null));
    reservation.setEndDateTime(connection.getEndTime().orElse(null));
    reservation.setSourcePort(new ReservationEndPoint(sourcePort));
    reservation.setDestinationPort(new ReservationEndPoint(destinationPort));
    reservation.setVirtualResourceGroup(sourcePort.getVirtualResourceGroup());
    reservation.setBandwidth(connection.getDesiredBandwidth());
    reservation.setUserCreated(userDetails.getNameId());
    reservation.setProtectionType(connection.getProtectionType());
    connection.setReservation(reservation);

    reservationService.create(reservation, autoProvision);
  }

  @SuppressWarnings("serial")
  public static class ValidationException extends Exception {
    private final String attributeName;
    private final ConnectionServiceProviderError error;

    public ValidationException(String attributeName, ConnectionServiceProviderError error, String errorMessage) {
      super(errorMessage);
      this.attributeName = attributeName;
      this.error = error;
    }

    public String getAttributeName() {
      return attributeName;
    }

    public ConnectionServiceProviderError getError() {
      return error;
    }
  }

  private void checkConnection(ConnectionV1 connection, RichUserDetails richUserDetails) throws ValidationException {
    checkConnectionId(connection.getConnectionId()); // can not persist the connection due db constraint

    try {
      checkProviderNsa(connection.getProviderNsa());
      checkPort(connection.getSourceStpId(), "sourceSTP", richUserDetails);
      checkPort(connection.getDestinationStpId(), "destSTP", richUserDetails);
    } catch (ValidationException e) {
      connection.setCurrentState(ConnectionStateType.TERMINATED);
      connectionRepo.save(connection);
      throw e;
    }
  }

  private void checkProviderNsa(String providerNsa) throws ValidationException {
    if (!nsiHelper.getProviderNsaV1().equals(providerNsa)) {
      log.warn("ProviderNsa '{}' is not accepted", providerNsa);

      throw new ValidationException("providerNSA", UNSUPPORTED_PARAMETER, String.format("ProviderNsa '%s' is not accepted", providerNsa));
    }
  }

  private void checkConnectionId(String connectionId) throws ValidationException {
    if (!StringUtils.hasText(connectionId)) {
      log.warn("ConnectionId was empty", connectionId);
      throw new ValidationException("connectionId", UNSUPPORTED_PARAMETER, "Connection id is empty");
    }

    if (connectionRepo.findByConnectionId(connectionId) != null) {
      log.warn("ConnectionId {} was not unique", connectionId);
      throw new ValidationException("connectionId", CONNECTION_EXISTS, "Connection id already exists");
    }
  }

  private void checkPort(String stpId, String attribute, RichUserDetails user) throws ValidationException {
    VirtualPort port = virtualPortService.findByNsiV1StpId(stpId);

    if (port == null) {
      throw new ValidationException(attribute, UNKNOWN_STP, String.format("Unknown STP '%s'", stpId));
    }

    if (!user.getUserGroupIds().contains(port.getVirtualResourceGroup().getAdminGroup())) {
      throw new ValidationException(attribute, UNAUTHORIZED, String.format("Unauthorized for STP '%s'", stpId));
    }
  }

  public void provision(Long connectionId, NsiV1RequestDetails requestDetails) {
    ConnectionV1 connection = connectionRepo.findOne(connectionId);
    checkNotNull(connection);

    if (connection.getCurrentState() == ConnectionStateType.PROVISIONED) {
      log.info("Connection is already provisioned", connection.getCurrentState());
      connectionServiceRequester.provisionConfirmed(connection, requestDetails);
    } else if (isProvisionPossible(connection)) {
      connection.setProvisionRequestDetails(requestDetails);
      connection = connectionRepo.saveAndFlush(connection);
      reservationService.provision(connection.getReservation());
    } else {
      log.info("Provision is not possible for state '{}'", connection.getCurrentState());
      connectionServiceRequester.provisionFailedDontUpdateState(connection, requestDetails);
    }
  }

  private boolean isProvisionPossible(ConnectionV1 connection) {
    return ImmutableList.of(ConnectionStateType.RESERVED, ConnectionStateType.SCHEDULED).contains(connection.getCurrentState());
  }

  @Async
  public void asyncTerminate(Long connectionId, String requesterNsa, NsiV1RequestDetails requestDetails, RichUserDetails user) {
    ConnectionV1 connection = connectionRepo.findOne(connectionId);

    if (isTerminatePossible(connection)) {
      connection.setTerminateRequestDetails(requestDetails);
      connection.setCurrentState(ConnectionStateType.TERMINATING);
      connectionRepo.saveAndFlush(connection);

      reservationService.cancelWithReason(
          connection.getReservation(),
          "NSI terminate by " + user.getNameId(),
          user);
    } else {
      log.info("Terminate is not possible for state '{}'", connection.getCurrentState());
      connectionServiceRequester.terminateFailed(connection, Optional.of(requestDetails));
    }
  }

  private boolean isTerminatePossible(ConnectionV1 connection) {
    return !ImmutableList.of(
        ConnectionStateType.RESERVING,
        ConnectionStateType.TERMINATING,
        ConnectionStateType.TERMINATED,
        ConnectionStateType.CLEANING).contains(connection.getCurrentState());
  }

  @Async
  public void asyncQueryConnections(NsiV1RequestDetails requestDetails, Collection<String> connectionIds,
      Collection<String> globalReservationIds, QueryOperationType operation, String requesterNsa, String providerNsa) {
    Preconditions.checkArgument(!connectionIds.isEmpty());

    QueryConfirmedType confirmedType =
      queryConnections(connectionIds, globalReservationIds, operation, requesterNsa, providerNsa);

    connectionServiceRequester.queryConfirmed(confirmedType, requestDetails);
  }

  protected QueryConfirmedType queryConnections(Collection<String> connectionIds, Collection<String> globalReservationIds,
      QueryOperationType operation, String requesterNsa, String providerNsa) {
    QueryConfirmedType confirmedType = getConfirmedType(requesterNsa, providerNsa);

    for (String connectionId : connectionIds) {
      addQueryResult(confirmedType, findByConnectionId(connectionId), operation);
    }

    for (String globalReservationId : globalReservationIds) {
      addQueryResult(confirmedType, findByGlobalReservationId(globalReservationId), operation);
    }

    return confirmedType;
  }

  @Async
  public void asyncQueryAllForRequesterNsa(NsiV1RequestDetails requestDetails,
      QueryOperationType operation, String requesterNsa, String providerNsa) {

    QueryConfirmedType confirmedType = queryAllForRequesterNsa(operation, requesterNsa, providerNsa);

    connectionServiceRequester.queryConfirmed(confirmedType, requestDetails);
  }

  protected QueryConfirmedType queryAllForRequesterNsa(QueryOperationType operation, String requesterNsa, String providerNsa) {
    QueryConfirmedType confirmedType = getConfirmedType(requesterNsa, providerNsa);

    for (ConnectionV1 connection : findByRequesterNsa(requesterNsa)) {
      addQueryResult(confirmedType, connection, operation);
    }

    return confirmedType;
  }

  private QueryConfirmedType getConfirmedType(String requesterNsa, String providerNsa) {
    QueryConfirmedType confirmedType = new QueryConfirmedType();
    confirmedType.setProviderNSA(providerNsa);
    confirmedType.setRequesterNSA(requesterNsa);
    return confirmedType;
  }

  private ConnectionV1 findByGlobalReservationId(String globalReservationId) {
    return connectionRepo.findByGlobalReservationId(globalReservationId);
  }

  private ConnectionV1 findByConnectionId(String connectionId) {
    return connectionRepo.findByConnectionId(connectionId);
  }

  private List<ConnectionV1> findByRequesterNsa(String requesterNsa) {
    return connectionRepo.findByRequesterNsa(requesterNsa);
  }

  private void addQueryResult(QueryConfirmedType confirmedType, ConnectionV1 connection, QueryOperationType operation) {
    if (connection == null) {
      return;
    }

    if (operation.equals(QueryOperationType.DETAILS)) {
      confirmedType.getReservationDetails().add(getQueryDetailsResult(connection));
    } else {
      confirmedType.getReservationSummary().add(getQuerySummaryResult(connection));
    }
  }

  private QueryDetailsResultType getQueryDetailsResult(ConnectionV1 connection) {
    QueryDetailsResultType result = new QueryDetailsResultType();
    result.setConnectionId(connection.getConnectionId());
    result.setDescription(connection.getDescription());
    result.setServiceParameters(connection.getServiceParameters());

    DetailedPathType path = new DetailedPathType();
    path.setConnectionState(connection.getCurrentState());
    path.setConnectionId(connection.getConnectionId());
    path.setProviderNSA(connection.getProviderNsa());
    result.setDetailedPath(path);

    if (StringUtils.hasText(connection.getGlobalReservationId())) {
      result.setGlobalReservationId(connection.getGlobalReservationId());
    }

    return result;
  }

  private QuerySummaryResultType getQuerySummaryResult(ConnectionV1 connection) {
    QuerySummaryResultType result = new QuerySummaryResultType();
    result.setConnectionId(connection.getConnectionId());
    result.setConnectionState(connection.getCurrentState());
    result.setDescription(connection.getDescription());

    if (StringUtils.hasText(connection.getGlobalReservationId())) {
      result.setGlobalReservationId(connection.getGlobalReservationId());
    }

    return result;
  }

}