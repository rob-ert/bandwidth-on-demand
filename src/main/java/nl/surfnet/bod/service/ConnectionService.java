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
package nl.surfnet.bod.service;

import static com.google.common.base.Preconditions.checkNotNull;
import static nl.surfnet.bod.domain.ReservationStatus.*;
import static nl.surfnet.bod.nsi.ws.v1sc.ConnectionServiceProviderFunctions.NSI_REQUEST_TO_CONNECTION_REQUESTER_PORT;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.xml.ws.Holder;

import nl.surfnet.bod.domain.*;
import nl.surfnet.bod.nsi.ws.ConnectionServiceProviderErrorCodes;
import nl.surfnet.bod.nsi.ws.v1sc.ConnectionServiceProviderWs;
import nl.surfnet.bod.repo.ConnectionRepo;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.ogf.schemas.nsi._2011._10.connection.requester.ConnectionRequesterPort;
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

  private final Logger log = LoggerFactory.getLogger(ConnectionService.class);

  protected final static Map<ReservationStatus, ConnectionStateType> STATE_MAPPING = new ImmutableMap.Builder<ReservationStatus, ConnectionStateType>()
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
        .put(TIMED_OUT, ConnectionStateType.TERMINATED).
        build();

  @Resource
  private ConnectionRepo connectionRepo;

  @Resource
  private ReservationService reservationService;

  @Resource
  private VirtualPortService virtualPortService;

  // FIXME introduces a dependency cycle....
  @Resource
  private ConnectionServiceProviderWs connectionServiceProviderWs;

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
    reservation.setUserCreated(connection.getRequesterNsa());
    reservation.setUserCreated(userDetails.getNameId());
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
      connectionServiceProviderWs.provisionConfirmed(connection, requestDetails);
    }
    else if (isProvisionPossible(connection)) {
      connection.setProvisionRequestDetails(requestDetails);
      connection = connectionRepo.saveAndFlush(connection);
      reservationService.provision(connection.getReservation(), Optional.of(requestDetails));
    }
    else {
      log.info("Provision is not possible for state '{}'", connection.getCurrentState());
      connectionServiceProviderWs.provisionFailed(connection, requestDetails);
    }
  }

  private boolean isProvisionPossible(Connection connection) {
    return ImmutableList.of(ConnectionStateType.RESERVED, ConnectionStateType.SCHEDULED).contains(connection.getCurrentState());
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Async
  public void asyncTerminate(final Long connectionId, final String requesterNsa, final NsiRequestDetails requestDetails) {
    Connection connection = connectionRepo.findOne(connectionId);
    if (isTerminatePossible(connection)) {
      connection.setCurrentState(ConnectionStateType.TERMINATING);
      connectionRepo.saveAndFlush(connection);

      reservationService.cancelWithReason(
          connection.getReservation(),
          "Terminate request by NSI",
          Security.getUserDetails(),
          Optional.of(requestDetails));
    } else {
      log.info("Terminate is not possible for state '{}'", connection.getCurrentState());
      connectionServiceProviderWs.terminateFailed(connection, Optional.of(requestDetails));
    }
  }

  private boolean isTerminatePossible(Connection connection) {
    return !ImmutableList.of(ConnectionStateType.TERMINATING, ConnectionStateType.TERMINATED, ConnectionStateType.CLEANING).contains(connection.getCurrentState());
  }

  @Async
  public void asyncQueryConnections(NsiRequestDetails requestDetails, Collection<String> connectionIds,
      Collection<String> globalReservationIds, QueryOperationType operation, String requesterNsa, String providerNsa) {
    Preconditions.checkArgument(!connectionIds.isEmpty());

    QueryConfirmedType confirmedType = queryConnections(connectionIds, globalReservationIds, operation, requesterNsa, providerNsa);

    sendQueryResult(requestDetails, confirmedType);
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

    sendQueryResult(requestDetails, confirmedType);
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

  private void sendQueryResult(NsiRequestDetails requestDetails, QueryConfirmedType queryResult) {
    ConnectionRequesterPort port = NSI_REQUEST_TO_CONNECTION_REQUESTER_PORT.apply(requestDetails);
    try {
      log.info("query result {}", queryResult);
      port.queryConfirmed(new Holder<>(requestDetails.getCorrelationId()), queryResult);
    }
    catch (org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException e) {
      log.error("Error sending query result", e);
      QueryFailedType failedType = new QueryFailedType();

      final ServiceExceptionType error = new ServiceExceptionType();
      error.setErrorId(ConnectionServiceProviderErrorCodes.CONNECTION.CONNECTION_ERROR.getId());
      failedType.setServiceException(error);
      sendQueryFailed(requestDetails.getCorrelationId(), failedType, port);
    }
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

  private void sendQueryFailed(final String correlationId, QueryFailedType failedType, ConnectionRequesterPort port) {
    try {
      port.queryFailed(new Holder<>(correlationId), failedType);
    }
    catch (org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException e) {
      log.error("Error: ", e);
    }
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
    } else {
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
    }).toImmutableList();
  }
}
