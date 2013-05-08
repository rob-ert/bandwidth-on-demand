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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.ConnectionV2;
import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.domain.ProtectionType;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.nsi.v2.ConnectionServiceRequesterVersionTwoCallback;
import nl.surfnet.bod.repo.ConnectionV2Repo;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.ogf.schemas.nsi._2013._04.connection.types.ReservationStateEnumType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

@Service
public class ConnectionServiceV2 extends AbstractFullTextSearchService<ConnectionV2> {

  protected static final Map<ReservationStatus, ReservationStateEnumType> STATE_MAPPING =
    new ImmutableMap.Builder<ReservationStatus, ReservationStateEnumType>()
//      .put(AUTO_START, ConnectionStateType.AUTO_PROVISION)
//      .put(CANCEL_FAILED, ConnectionStateType.TERMINATED)
//      .put(CANCELLED, ConnectionStateType.TERMINATED)
//      .put(FAILED, ConnectionStateType.TERMINATED)
//      .put(REQUESTED, ConnectionStateType.INITIAL)
//      .put(NOT_ACCEPTED, ConnectionStateType.TERMINATED)
//      .put(RESERVED, ConnectionStateType.RESERVED)
//      .put(RUNNING, ConnectionStateType.PROVISIONED)
//      .put(SCHEDULED, ConnectionStateType.SCHEDULED)
//      .put(SUCCEEDED, ConnectionStateType.TERMINATED)
//      .put(TIMED_OUT, ConnectionStateType.TERMINATED)
      .build();

  private final Logger log = LoggerFactory.getLogger(ConnectionServiceV2.class);

  @Resource
  private Environment bodEnvironment;

  @Resource
  private ConnectionV2Repo connectionRepo;

  @Resource
  private ReservationService reservationService;

  @Resource
  private VirtualPortService virtualPortService;

  @Resource
  private ConnectionServiceRequesterVersionTwoCallback connectionServiceRequester;

  @PersistenceContext
  private EntityManager entityManager;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void reserve(ConnectionV2 connection, NsiRequestDetails requestDetails, boolean autoProvision, RichUserDetails userDetails) throws ValidationException {
    checkConnection(connection, userDetails);

    connection.setCurrentState(ReservationStateEnumType.RESERVE_CHECKING);
    connection = connectionRepo.saveAndFlush(connection);

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

  private void checkConnection(ConnectionV2 connection, RichUserDetails richUserDetails) throws ValidationException {
    try {
      checkProviderNsa(connection.getProviderNsa());
      checkConnectionId(connection.getConnectionId());
      checkPort(connection.getSourceStpId(), "sourceSTP", richUserDetails);
      checkPort(connection.getDestinationStpId(), "destSTP", richUserDetails);
    }
    catch (ValidationException e) {
      // TODO should go to terminated in Life-cycle state machine?
      connection.setCurrentState(ReservationStateEnumType.RESERVED);
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

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void provision(Long connectionId, NsiRequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(connectionId);
    checkNotNull(connection);

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
}