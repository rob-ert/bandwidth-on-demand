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
package nl.surfnet.bod.service;

import static com.google.common.base.Preconditions.checkNotNull;
import static nl.surfnet.bod.domain.ReservationStatus.AUTO_START;
import static nl.surfnet.bod.domain.ReservationStatus.CANCELLED;
import static nl.surfnet.bod.domain.ReservationStatus.CANCEL_FAILED;
import static nl.surfnet.bod.domain.ReservationStatus.FAILED;
import static nl.surfnet.bod.domain.ReservationStatus.LOST;
import static nl.surfnet.bod.domain.ReservationStatus.NOT_ACCEPTED;
import static nl.surfnet.bod.domain.ReservationStatus.PASSED_END_TIME;
import static nl.surfnet.bod.domain.ReservationStatus.REQUESTED;
import static nl.surfnet.bod.domain.ReservationStatus.RESERVED;
import static nl.surfnet.bod.domain.ReservationStatus.RUNNING;
import static nl.surfnet.bod.domain.ReservationStatus.SCHEDULED;
import static nl.surfnet.bod.domain.ReservationStatus.SUCCEEDED;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.ConnectionV1;
import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.domain.ProtectionType;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.nsi.v1sc.ConnectionServiceRequesterV1;
import nl.surfnet.bod.repo.ConnectionV1Repo;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.ogf.schemas.nsi._2011._10.connection.types.DetailedPathType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryDetailsResultType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryOperationType;
import org.ogf.schemas.nsi._2011._10.connection.types.QuerySummaryResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class ConnectionServiceV1 extends AbstractFullTextSearchService<ConnectionV1> {

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
      .put(PASSED_END_TIME, ConnectionStateType.TERMINATED)
      .put(LOST, ConnectionStateType.UNKNOWN)
      .build();

  private final Logger log = LoggerFactory.getLogger(ConnectionServiceV1.class);

  @Resource private Environment bodEnvironment;
  @Resource private ConnectionV1Repo connectionRepo;
  @Resource private ReservationService reservationService;
  @Resource private VirtualPortService virtualPortService;
  @Resource private ConnectionServiceRequesterV1 connectionServiceRequester;

  @PersistenceContext private EntityManager entityManager;

  public void reserve(ConnectionV1 connection, NsiRequestDetails requestDetails, boolean autoProvision, RichUserDetails userDetails) throws ValidationException {
    checkConnection(connection, userDetails);

    connection.setCurrentState(ConnectionStateType.RESERVING);
    connection = connectionRepo.saveAndFlush(connection);

    VirtualPort sourcePort = virtualPortService.findByNsiStpId(connection.getSourceStpId());
    VirtualPort destinationPort = virtualPortService.findByNsiStpId(connection.getDestinationStpId());

    Reservation reservation = new Reservation();
    reservation.setConnectionV1(connection);
    reservation.setName(connection.getDescription());
    reservation.setStartDateTime(connection.getStartTime().orNull());
    reservation.setEndDateTime(connection.getEndTime().orNull());
    reservation.setSourcePort(sourcePort);
    reservation.setDestinationPort(destinationPort);
    reservation.setVirtualResourceGroup(sourcePort.getVirtualResourceGroup());
    reservation.setBandwidth(connection.getDesiredBandwidth());
    reservation.setUserCreated(userDetails.getNameId());
    reservation.setProtectionType(ProtectionType.valueOf(connection.getProtectionType()));
    connection.setReservation(reservation);

    reservationService.create(reservation, autoProvision, Optional.of(requestDetails));
  }

  @SuppressWarnings("serial")
  public static class ValidationException extends Exception {
    private final String attributeName;
    private final String errorCode;

    public ValidationException(String attributeName, String errorCode, String errorMessage) {
      super(errorMessage);
      this.attributeName = attributeName;
      this.errorCode = errorCode;
    }

    public String getAttributeName() {
      return attributeName;
    }

    public String getErrorCode() {
      return errorCode;
    }
  }

  private void checkConnection(ConnectionV1 connection, RichUserDetails richUserDetails) throws ValidationException {
    checkConnectionId(connection.getConnectionId()); // can not persist the connection due db constraint

    try {
      checkProviderNsa(connection.getProviderNsa());
      checkPort(connection.getSourceStpId(), "sourceSTP", richUserDetails);
      checkPort(connection.getDestinationStpId(), "destSTP", richUserDetails);
    }
    catch (ValidationException e) {
      connection.setCurrentState(ConnectionStateType.TERMINATED);
      connectionRepo.save(connection);
      throw e;
    }
  }

  private void checkProviderNsa(String providerNsa) throws ValidationException {
    if (!bodEnvironment.getNsiProviderNsa().equals(providerNsa)) {
      log.warn("ProviderNsa '{}' is not accepted", providerNsa);

      throw new ValidationException("providerNSA", "0100", String.format("ProviderNsa '%s' is not accepted", providerNsa));
    }
  }

  private void checkConnectionId(String connectionId) throws ValidationException {
    if (!StringUtils.hasText(connectionId)) {
      log.warn("ConnectionId was empty", connectionId);
      throw new ValidationException("connectionId", "0100", "Connection id is empty");
    }

    if (connectionRepo.findByConnectionId(connectionId) != null) {
      log.warn("ConnectionId {} was not unique", connectionId);
      throw new ValidationException("connectionId", "0100", "Connection id already exists");
    }
  }

  private void checkPort(String stpId, String attribute, RichUserDetails user) throws ValidationException {
    VirtualPort port = virtualPortService.findByNsiStpId(stpId);

    if (port == null) {
      throw new ValidationException(attribute, "0100", String.format("Unknown STP '%s'", stpId));
    }

    if (!user.getUserGroupIds().contains(port.getVirtualResourceGroup().getAdminGroup())) {
      throw new ValidationException(attribute, "0100", String.format("Unauthorized for STP '%s'", stpId));
    }
  }

  public void provision(Long connectionId, NsiRequestDetails requestDetails) {
    ConnectionV1 connection = connectionRepo.findOne(connectionId);
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
      connectionServiceRequester.provisionFailedDontUpdateState(connection, requestDetails);
    }
  }

  private boolean isProvisionPossible(ConnectionV1 connection) {
    return ImmutableList.of(ConnectionStateType.RESERVED, ConnectionStateType.SCHEDULED).contains(connection.getCurrentState());
  }

  @Async
  public void asyncTerminate(Long connectionId, String requesterNsa, NsiRequestDetails requestDetails, RichUserDetails user) {
    ConnectionV1 connection = connectionRepo.findOne(connectionId);

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

  private boolean isTerminatePossible(ConnectionV1 connection) {
    return !ImmutableList.of(
        ConnectionStateType.RESERVING,
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
    }
    else {
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

  public Connection find(Long id) {
    return connectionRepo.findOne(id);
  }

  public Collection<ConnectionV1> findAll() {
    return connectionRepo.findAll();
  }

  public List<Long> findIds(Optional<Sort> sort) {
    return connectionRepo.findIdsWithWhereClause(Optional.<Specification<ConnectionV1>>absent(), sort);
  }

  public List<ConnectionV1> findEntries(int firstResult, int maxResults, Sort sort) {
    return connectionRepo.findAll(new PageRequest(firstResult / maxResults, maxResults, sort)).getContent();
  }

  public long count() {
    return connectionRepo.count();
  }

  protected boolean hasValidState(ConnectionV1 connection) {
    if (connection.getReservation() == null) {
      return connection.getCurrentState() == ConnectionStateType.TERMINATED;
    }
    else {
        return STATE_MAPPING.get(connection.getReservation().getStatus()) == connection.getCurrentState();
    }
  }

  public List<ConnectionV1> findWithIllegalState(int firstResult, int maxResults, Sort sort) {
    List<ConnectionV1> connections = connectionRepo.findAll(sort);

    return FluentIterable.from(connections).filter(new Predicate<ConnectionV1>() {
      @Override
      public boolean apply(ConnectionV1 connection) {
        return !hasValidState(connection);
      }
    }).toList();
  }

  @Override
  protected EntityManager getEntityManager() {
    return entityManager;
  }
}