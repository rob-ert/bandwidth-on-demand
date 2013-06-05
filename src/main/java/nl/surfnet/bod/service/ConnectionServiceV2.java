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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.ConnectionV2;
import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.domain.ProtectionType;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.nsi.v2.ConnectionServiceRequesterV2;
import nl.surfnet.bod.repo.ConnectionV2Repo;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.ogf.schemas.nsi._2013._04.connection.types.LifecycleStateEnumType;
import org.ogf.schemas.nsi._2013._04.connection.types.ProvisionStateEnumType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReservationStateEnumType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.base.Optional;

@Service
public class ConnectionServiceV2 extends AbstractFullTextSearchService<ConnectionV2> {

  private final Logger log = LoggerFactory.getLogger(ConnectionServiceV2.class);

  @Resource private Environment bodEnvironment;
  @Resource private ConnectionV2Repo connectionRepo;
  @Resource private ReservationService reservationService;
  @Resource private VirtualPortService virtualPortService;
  @Resource private ConnectionServiceRequesterV2 connectionServiceRequester;

  @PersistenceContext private EntityManager entityManager;

  public void reserve(ConnectionV2 connection, NsiRequestDetails requestDetails, RichUserDetails userDetails) throws ValidationException {
    checkConnection(connection, userDetails);

    connection.setReservationState(ReservationStateEnumType.RESERVE_CHECKING);
    connectionRepo.save(connection);

    VirtualPort sourcePort = virtualPortService.findByNsiStpId(connection.getSourceStpId());
    VirtualPort destinationPort = virtualPortService.findByNsiStpId(connection.getDestinationStpId());

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

    reservationService.create(reservation, false, Optional.of(requestDetails));
  }

  @Async
  public void asyncReserveCommit(String connectionId, NsiRequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findByConnectionId(connectionId);

    connection.setReservationState(ReservationStateEnumType.RESERVE_COMMITTING);
    connectionRepo.save(connection);

    connectionServiceRequester.reserveCommitConfirmed(connection.getId(), requestDetails);
  }

  @Async
  public void asyncReserveAbort(String connectionId, NsiRequestDetails requestDetails, RichUserDetails user) {
    ConnectionV2 connection = connectionRepo.findByConnectionId(connectionId);

    connection.setReservationState(ReservationStateEnumType.RESERVE_ABORTING);
    connectionRepo.save(connection);

    terminate(connection, requestDetails, user);
  }

  private void terminate(ConnectionV2 connection, NsiRequestDetails requestDetails, RichUserDetails user) {
    reservationService.cancelWithReason(
      connection.getReservation(),
      "NSIv2 terminate by " + user.getNameId(),
      user,
      Optional.of(requestDetails));
  }

  @Async
  public void asyncProvision(String connectionId, NsiRequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findByConnectionId(connectionId);

    connection.setProvisionState(ProvisionStateEnumType.PROVISIONING);
    connectionRepo.save(connection);

    reservationService.provision(connection.getReservation(), Optional.of(requestDetails));
  }

  @Async
  public void asyncQuerySummary(List<String> connectionIds, List<String> globalReservationIds, NsiRequestDetails requestDetails, String requesterNsa) {
    connectionServiceRequester.querySummaryConfirmed(querySummarySync(connectionIds, globalReservationIds, requesterNsa), requestDetails);
  }

  public List<ConnectionV2> querySummarySync(List<String> connectionIds, List<String> globalReservationIds, String requesterNsa) {
    List<ConnectionV2> connections;

    if (connectionIds.isEmpty() && globalReservationIds.isEmpty()) {
      connections = connectionRepo.findByRequesterNsa(requesterNsa);
    } else {
      connections = new ArrayList<>();
      for (String connectionId : connectionIds) {
        connections.add(connectionRepo.findByConnectionId(connectionId));
      }
    }

    return connections;
  }

  public void asyncTerminate(String connectionId, NsiRequestDetails requestDetails, RichUserDetails user) {
    ConnectionV2 connection = connectionRepo.findByConnectionId(connectionId);

    connection.setLifecycleState(LifecycleStateEnumType.TERMINATING);
    connectionRepo.save(connection);

    terminate(connection, requestDetails, user);
  }

  private void checkConnection(ConnectionV2 connection, RichUserDetails richUserDetails) throws ValidationException {
    try {
      checkProviderNsa(connection.getProviderNsa());
      checkConnectionId(connection.getConnectionId());
      checkPort(connection.getSourceStpId(), "sourceSTP", richUserDetails);
      checkPort(connection.getDestinationStpId(), "destSTP", richUserDetails);
    }
    catch (ValidationException e) {
      // TODO should go to terminated in Life-cycle state machine?
      //connection.setLifecycleState(TERMINATED);
      connection.setReservationState(ReservationStateEnumType.RESERVE_START);
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

  public Connection find(Long id) {
    return connectionRepo.findOne(id);
  }

  public Collection<ConnectionV2> findAll() {
    return connectionRepo.findAll();
  }

  public List<Long> findIds(Optional<Sort> sort) {
    return connectionRepo.findIdsWithWhereClause(Optional.<Specification<ConnectionV2>>absent(), sort);
  }

  public List<ConnectionV2> findEntries(int firstResult, int maxResults, Sort sort) {
    return connectionRepo.findAll(new PageRequest(firstResult / maxResults, maxResults, sort)).getContent();
  }

  public long count() {
    return connectionRepo.count();
  }

  @Override
  protected EntityManager getEntityManager() {
    return entityManager;
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

}
