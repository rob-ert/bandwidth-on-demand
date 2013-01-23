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
package nl.surfnet.bod.service;

import static com.google.common.base.Preconditions.checkNotNull;
import static nl.surfnet.bod.domain.ReservationStatus.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.*;
import nl.surfnet.bod.nsi.v1sc.ConnectionServiceRequesterCallback;
import nl.surfnet.bod.repo.ConnectionRepo;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.ogf.schemas.nsi._2011._10.connection.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@Service
public class ConnectionService {

  protected static final Map<ReservationStatus, ConnectionStateType> STATE_MAPPING =
    new ImmutableMap.Builder<ReservationStatus, ConnectionStateType>()
      .put(AUTO_START, ConnectionStateType.AUTO_PROVISION)
      .put(CANCEL_FAILED, ConnectionStateType.TERMINATED)
      .put(CANCELLED, ConnectionStateType.TERMINATED)
      .put(FAILED, ConnectionStateType.TERMINATED)
      .put(REQUESTED, ConnectionStateType.INITIAL)
      .put(NOT_ACCEPTED, ConnectionStateType.TERMINATED)
      .put(RESERVED, ConnectionStateType.RESERVED)
      .put(RUNNING, ConnectionStateType.PROVISIONED)
      .put(SCHEDULED, ConnectionStateType.SCHEDULED)
      .put(SUCCEEDED, ConnectionStateType.TERMINATED)
      .put(TIMED_OUT, ConnectionStateType.TERMINATED)
      .build();

  private final Logger log = LoggerFactory.getLogger(ConnectionService.class);

  @Resource
  private ConnectionRepo connectionRepo;

  @Resource
  private ReservationService reservationService;

  @Resource
  private VirtualPortService virtualPortService;

  @Resource
  private ConnectionServiceRequesterCallback connectionServiceRequester;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void reserve(Connection connection, NsiRequestDetails requestDetails, boolean autoProvision,
      RichUserDetails userDetails) {
    connection.setCurrentState(ConnectionStateType.RESERVING);
    connection = connectionRepo.saveAndFlush(connection);

    final VirtualPort sourcePort = virtualPortService.findByNsiStpId(connection.getSourceStpId());
    final VirtualPort destinationPort = virtualPortService.findByNsiStpId(connection.getDestinationStpId());

    Reservation reservation = new Reservation();
    reservation.setConnection(connection);
    reservation.setName(connection.getDescription());
    reservation.setStartDateTime(connection.getStartTime().orNull());
    reservation.setEndDateTime(connection.getEndTime().orNull());
    reservation.setSourcePort(sourcePort);
    reservation.setDestinationPort(destinationPort);
    reservation.setVirtualResourceGroup(sourcePort.getVirtualResourceGroup());
    reservation.setBandwidth(connection.getDesiredBandwidth());
    reservation.setUserCreated(userDetails.getNameId());
    reservation.setProtectionType(ProtectionType.valueOf(connection.getProtectionType()));

    reservation.setConnection(connection);
    connection.setReservation(reservation);
    reservationService.create(reservation, autoProvision, Optional.of(requestDetails));
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void provision(Long connectionId, NsiRequestDetails requestDetails) {
    Connection connection = connectionRepo.findOne(connectionId);
    checkNotNull(connection);

    if (connection.getCurrentState() == ConnectionStateType.PROVISIONED) {
      log.info("Connection is already provisioned", connection.getCurrentState());
      connectionServiceRequester.provisionConfirmed(connection, requestDetails);
    }
    else if (isProvisionPossible(connection)) {
      connection.setProvisionRequestDetails(requestDetails);
      connection = connectionRepo.saveAndFlush(connection);
      reservationService.provision(connection.getReservation(), Optional.of(requestDetails));
    }
    else {
      log.info("Provision is not possible for state '{}'", connection.getCurrentState());
      connectionServiceRequester.provisionFailed(connection, requestDetails);
    }
  }

  private boolean isProvisionPossible(Connection connection) {
    return ImmutableList.of(ConnectionStateType.RESERVED, ConnectionStateType.SCHEDULED).contains(connection.getCurrentState());
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Async
  public void asyncTerminate(Long connectionId, String requesterNsa, NsiRequestDetails requestDetails, RichUserDetails user) {
    Connection connection = connectionRepo.findOne(connectionId);

    if (isTerminatePossible(connection)) {
      connection.setCurrentState(ConnectionStateType.TERMINATING);
      connectionRepo.saveAndFlush(connection);

      reservationService.cancelWithReason(
          connection.getReservation(),
          "NSI terminate by " + user.getNameId(),
          user,
          Optional.of(requestDetails));
    }
    else {
      log.info("Terminate is not possible for state '{}'", connection.getCurrentState());
      connectionServiceRequester.terminateFailed(connection, Optional.of(requestDetails));
    }
  }

  private boolean isTerminatePossible(Connection connection) {
    return !ImmutableList.of(
        ConnectionStateType.TERMINATING,
        ConnectionStateType.TERMINATED,
        ConnectionStateType.CLEANING).contains(connection.getCurrentState());
  }

  @Async
  public void asyncQueryConnections(NsiRequestDetails requestDetails, Collection<String> connectionIds,
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
  public void asyncQueryAllForRequesterNsa(NsiRequestDetails requestDetails,
      QueryOperationType operation, String requesterNsa, String providerNsa) {

    QueryConfirmedType confirmedType = queryAllForRequesterNsa(operation, requesterNsa, providerNsa);

    connectionServiceRequester.queryConfirmed(confirmedType, requestDetails);
  }

  protected QueryConfirmedType queryAllForRequesterNsa(QueryOperationType operation, String requesterNsa, String providerNsa) {
    QueryConfirmedType confirmedType = getConfirmedType(requesterNsa, providerNsa);

    for (Connection connection : findByRequesterNsa(requesterNsa)) {
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

  private Connection findByGlobalReservationId(String globalReservationId) {
    return connectionRepo.findByGlobalReservationId(globalReservationId);
  }

  private Connection findByConnectionId(String connectionId) {
    return connectionRepo.findByConnectionId(connectionId);
  }

  private List<Connection> findByRequesterNsa(String requesterNsa) {
    return connectionRepo.findByRequesterNsa(requesterNsa);
  }

  private void addQueryResult(QueryConfirmedType confirmedType, Connection connection, QueryOperationType operation) {
    if (connection == null) {
      return;
    }

    if (operation.equals(QueryOperationType.DETAILS)) {
      confirmedType.getReservationDetails().add(getQueryDetailsResult(connection));
    }
    else {
      confirmedType.getReservationSummary().add(getQuerySummaryResult(connection));
    }
  }

  private QueryDetailsResultType getQueryDetailsResult(Connection connection) {
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

  private QuerySummaryResultType getQuerySummaryResult(Connection connection) {
    QuerySummaryResultType result = new QuerySummaryResultType();
    result.setConnectionId(connection.getConnectionId());
    result.setConnectionState(connection.getCurrentState());
    result.setDescription(connection.getDescription());

    if (StringUtils.hasText(connection.getGlobalReservationId())) {
      result.setGlobalReservationId(connection.getGlobalReservationId());
    }

    return result;
  }

  public Collection<Connection> findAll() {
    return connectionRepo.findAll();
  }

  public List<Connection> findEntries(int firstResult, int maxResults, Sort sort) {
    return connectionRepo.findAll(new PageRequest(firstResult / maxResults, maxResults, sort)).getContent();
  }

  public long count() {
    return connectionRepo.count();
  }

  protected boolean hasValidState(Connection connection) {
    if (connection.getReservation() == null) {
      return connection.getCurrentState() == ConnectionStateType.TERMINATED;
    }
    else {
        return STATE_MAPPING.get(connection.getReservation().getStatus()) == connection.getCurrentState();
    }
  }

  public Collection<Connection> findWithIllegalState() {
    List<Connection> connections = connectionRepo.findAll();

    return FluentIterable.from(connections).filter(new Predicate<Connection>() {
      @Override
      public boolean apply(Connection connection) {
        return !hasValidState(connection);
      }
    }).toList();
  }
}