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
package nl.surfnet.bod.nsi.v2;

import static nl.surfnet.bod.nsi.ConnectionServiceProviderError.CONNECTION_EXISTS;
import static nl.surfnet.bod.nsi.ConnectionServiceProviderError.MISSING_PARAMETER;
import static nl.surfnet.bod.nsi.ConnectionServiceProviderError.PAYLOAD_ERROR;
import static nl.surfnet.bod.nsi.ConnectionServiceProviderError.TOPOLOGY_ERROR;
import static nl.surfnet.bod.nsi.ConnectionServiceProviderError.UNAUTHORIZED;
import static nl.surfnet.bod.nsi.ConnectionServiceProviderError.UNKNOWN_STP;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Resource;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import nl.surfnet.bod.domain.ConnectionV2;
import nl.surfnet.bod.domain.EnniPort;
import nl.surfnet.bod.domain.NsiV2RequestDetails;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationEndPoint;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.nsi.ConnectionServiceProviderError;
import nl.surfnet.bod.repo.ConnectionV2Repo;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.util.XmlUtils;
import nl.surfnet.bod.web.security.RichUserDetails;
import org.joda.time.DateTime;
import org.ogf.schemas.nsi._2013._12.connection.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class ConnectionServiceV2 {

  private final Logger log = LoggerFactory.getLogger(ConnectionServiceV2.class);

  @Resource private ConnectionV2Repo connectionRepo;
  @Resource private ReservationService reservationService;
  @Resource private VirtualPortService virtualPortService;
  @Resource private PhysicalPortService physicalPortService;
  @Resource private ConnectionServiceRequesterV2 connectionServiceRequester;
  @Resource private NsiV2MessageRepo nsiV2MessageRepo;

  public void reserve(ConnectionV2 connection, NsiV2RequestDetails requestDetails, RichUserDetails userDetails) throws ReservationCreationException {
    checkConnectionId(connection.getConnectionId());
    checkGlobalReservationId(connection.getGlobalReservationId());
    checkStartEndTime(connection.getStartTime(), connection.getEndTime());

    try {
      connection.setReservationState(ReservationStateEnumType.RESERVE_CHECKING);
      Reservation reservation = new Reservation();
      reservation.setConnectionV2(connection);
      reservation.setName(connection.getDescription());
      reservation.setStartDateTime(connection.getStartTime().orNull());
      reservation.setEndDateTime(connection.getEndTime().orNull());
      reservation.setSourcePort(findEndPoint(connection.getSourceStpId(), connection.getSourceVlanId(), userDetails));
      reservation.setDestinationPort(findEndPoint(connection.getDestinationStpId(),  connection.getDestinationVlanId(), userDetails));
      reservation.setBandwidth(connection.getDesiredBandwidth());
      reservation.setUserCreated(userDetails.getNameId());
      reservation.setProtectionType(connection.getProtectionType());
      connection.setReservation(reservation);
      connectionRepo.save(connection);

      reservationService.create(reservation, false);
    } catch (ReservationCreationException e) {
      connection.setLifecycleState(LifecycleStateEnumType.FAILED);
      connection.setReservationState(ReservationStateEnumType.RESERVE_START);
      connectionRepo.save(connection);

      throw e;
    }
  }

  @Async
  public void asyncReserveCommit(String connectionId, NsiV2RequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findByConnectionId(connectionId);

    connection.setLastReservationRequestDetails(requestDetails);
    connection.setReservationState(ReservationStateEnumType.RESERVE_COMMITTING);
    connectionRepo.save(connection);

    connectionServiceRequester.reserveCommitConfirmed(connection.getId(), requestDetails);
  }

  @Async
  public void asyncReserveAbort(String connectionId, NsiV2RequestDetails requestDetails, RichUserDetails user) {
    ConnectionV2 connection = connectionRepo.findByConnectionId(connectionId);

    connection.setLastReservationRequestDetails(requestDetails);
    switch (connection.getReservationState()) {
    case RESERVE_HELD:
      // Must terminate current reservation in NBI before confirming to requester.
      connection.setReservationState(ReservationStateEnumType.RESERVE_ABORTING);
      connectionRepo.save(connection);
      terminate(connection, requestDetails, user);
      break;
    case RESERVE_FAILED:
    case RESERVE_TIMEOUT:
      connection.setReservationState(ReservationStateEnumType.RESERVE_START);
      connectionRepo.save(connection);
      connectionServiceRequester.reserveAbortConfirmed(connection.getId(), requestDetails);
      break;
    default:
      throw new IllegalStateException("cannot abort reservation '" + connectionId + "' in " + connection.getReservationState() + " state");
    }
  }

  @Async
  public void asyncTerminate(String connectionId, NsiV2RequestDetails requestDetails, RichUserDetails user) {
    ConnectionV2 connection = connectionRepo.findByConnectionId(connectionId);

    connection.setLastLifecycleRequestDetails(requestDetails);
    connection.setLifecycleState(LifecycleStateEnumType.TERMINATING);
    connectionRepo.save(connection);

    terminate(connection, requestDetails, user);
  }

  private void terminate(ConnectionV2 connection, NsiV2RequestDetails requestDetails, RichUserDetails user) {
    reservationService.cancelWithReason(
        connection.getReservation(),
        "NSIv2 terminate by " + user.getNameId(),
        user);
  }

  @Async
  public void asyncProvision(String connectionId, NsiV2RequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findByConnectionId(connectionId);

    connection.setLastProvisionRequestDetails(requestDetails);
    connection.setProvisionState(ProvisionStateEnumType.PROVISIONING);
    connectionRepo.save(connection);

    reservationService.provision(connection.getReservation());
  }

  @Async
  public void asyncQuerySummary(List<String> connectionIds, List<String> globalReservationIds, NsiV2RequestDetails requestDetails) {
    connectionServiceRequester.querySummaryConfirmed(querySummarySync(connectionIds, globalReservationIds, requestDetails.getRequesterNsa()), requestDetails);
  }

  /**
   * Implement this just like querySummary, because BoD has no downstream agents to delegate to.
   */
  @Async
  public void asyncQueryRecursive(List<String> connectionIds, List<String> globalReservationIds, NsiV2RequestDetails requestDetails) {
    List<ConnectionV2> result = querySummarySync(connectionIds, globalReservationIds, requestDetails.getRequesterNsa());
    connectionServiceRequester.queryRecursiveConfirmed(result, requestDetails);
  }

  public List<ConnectionV2> querySummarySync(List<String> connectionIds, List<String> globalReservationIds, String requesterNsa) {
    List<ConnectionV2> connections;

    if (connectionIds.isEmpty() && globalReservationIds.isEmpty()) {
      connections = connectionRepo.findByRequesterNsa(requesterNsa);
    } else {
      connections = new ArrayList<>();
      for (String connectionId : connectionIds) {
        ConnectionV2 connection = connectionRepo.findByConnectionId(connectionId);
        if (connection != null) {
          connections.add(connection);
        }
      }
      for (String globalReservationId : globalReservationIds) {
        ConnectionV2 connection = connectionRepo.findByGlobalReservationId(globalReservationId);
        if (connection != null) {
          connections.add(connection);
        }
      }
    }

    return connections;
  }

  public List<NotificationBaseType> queryNotification(String connectionId, Optional<Long> startNotificationId, Optional<Long> endNotificationId, NsiV2RequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findByConnectionId(connectionId);

    if (connection == null) {
      return Collections.emptyList();
    }

    RangeSet<Long> notificationRange = TreeRangeSet.create();
    if (startNotificationId.isPresent() && endNotificationId.isPresent()) {
      notificationRange.add(Range.closed(startNotificationId.get(), endNotificationId.get()));
    } else if (startNotificationId.isPresent() && !endNotificationId.isPresent()) {
      notificationRange.add(Range.atLeast(startNotificationId.get()));
    } else if (endNotificationId.isPresent() && !startNotificationId.isPresent()) {
      notificationRange.add(Range.atMost(endNotificationId.get()));
    } else {
      notificationRange.add(Range.<Long>all());
    }

    List<NotificationBaseType> selectedNotifications = new ArrayList<>();
    for (NotificationBaseType notification : connection.getNotifications()) {
      if (notificationRange.contains(notification.getNotificationId())) {
        selectedNotifications.add(notification);
      }
    }

    return Lists.newArrayList(selectedNotifications);
  }

  @Async
  public void asyncQueryNotification(String connectionId, Optional<Long> startNotificationId, Optional<Long> endNotificationId, NsiV2RequestDetails requestDetails) {
    connectionServiceRequester.queryNotificationConfirmed(queryNotification(connectionId, startNotificationId, endNotificationId, requestDetails), requestDetails);
  }

  public List<QueryResultResponseType> syncQueryResult(String connectionId, Long startResultId, Long endResultId) {
    List<NsiV2Message> messages = nsiV2MessageRepo.findQueryResults(connectionId, startResultId, endResultId);

    List<QueryResultResponseType> responses = new ArrayList<>();
    for (NsiV2Message message: messages) {
      QueryResultResponseType response = new QueryResultResponseType().withResultId(message.getResultId())
              .withTimeStamp(XmlUtils.toGregorianCalendar(message.getCreatedAt()))
              .withCorrelationId(message.getCorrelationId());


      try {
        SOAPMessage soapMessage = Converters.deserializeMessage(message.getMessage());
        log.debug("message = {}", soapMessage.toString());
        // figure out which type of message it was. We care about:
        // reserveConfirmed, reserveFailed, reserveCommitConfirmed, reserveCommitFailed, reserveAbortConfirmed,
        // provisionConfirmed, releaseConfirmed, provisionConfirmed, terminateConfirmed
      } catch (SOAPException | IOException e) {
        log.error("Unable to de-serialize NsiV2Message.message, stored value was: {}", message.getMessage());
        throw new RuntimeException(e);
      }
      responses.add(response);
    }
    return responses;
  }

  @Async
  public void asyncReserveTimeout(DateTime timedOutAt, Long reservationId, Long connectionId) {
    reservationService.cancelDueToReserveTimeout(reservationId);
    connectionServiceRequester.reserveTimeout(connectionId, timedOutAt);
  }

  private void checkStartEndTime(Optional<DateTime> startTime, Optional<DateTime> endTime) throws ReservationCreationException {
    if (startTime.isPresent() && endTime.isPresent()) {
      if (startTime.get().isAfter(endTime.get())) {
        log.debug("Start time {} is after end time {}", startTime.get(), endTime.get());
        throw new ReservationCreationException(PAYLOAD_ERROR, "Start time is after end time");
      }
    }
  }

  private void checkGlobalReservationId(String globalReservationId) throws ReservationCreationException {
    if (connectionRepo.findByGlobalReservationId(globalReservationId) != null) {
      log.debug("GlobalReservationId {} was not unique", globalReservationId);
      throw new ReservationCreationException(PAYLOAD_ERROR, "GlobalReservationId already exists");
    }
  }

  private void checkConnectionId(String connectionId) throws ReservationCreationException {
    if (!StringUtils.hasText(connectionId)) {
      log.warn("ConnectionId was empty", connectionId);
      throw new ReservationCreationException(MISSING_PARAMETER, "Missing parameter");
    }

    if (connectionRepo.findByConnectionId(connectionId) != null) {
      log.warn("ConnectionId {} was not unique", connectionId);
      throw new ReservationCreationException(CONNECTION_EXISTS, "Connection id already exists");
    }
  }

  private ReservationEndPoint findEndPoint(String stpId, Optional<Integer> vlanId, RichUserDetails user) throws ReservationCreationException {
    ReservationEndPoint virtualPort = tryVirtualPortEndPoint(stpId, vlanId, user);
    if (virtualPort != null) {
      return virtualPort;
    }

    ReservationEndPoint enniPort = tryEnniPortAsEndPoint(stpId, vlanId, virtualPort);
    if (enniPort != null) {
      return enniPort;
    }

    throw new ReservationCreationException(UNKNOWN_STP, String.format("Unknown STP '%s'", stpId));
  }

  private ReservationEndPoint tryVirtualPortEndPoint(String stpId, Optional<Integer> vlanId, RichUserDetails user) throws ReservationCreationException {
    VirtualPort virtualPort = virtualPortService.findByNsiV2StpId(stpId);
    if (virtualPort == null) {
      return null;
    }

    if (!user.getUserGroupIds().contains(virtualPort.getVirtualResourceGroup().getAdminGroup())) {
      throw new ReservationCreationException(UNAUTHORIZED, String.format("Unauthorized for STP '%s'", stpId));
    }

    boolean vlanRequired = virtualPort.getVlanId() != null;
    if (vlanRequired != vlanId.isPresent()) {
      throw new ReservationCreationException(TOPOLOGY_ERROR, String.format("STP '%s' %s VLAN ID", stpId, vlanRequired ? "requires" : "does not allow"));
    } else if (vlanRequired && !virtualPort.getVlanId().equals(vlanId.get())) {
      throw new ReservationCreationException(TOPOLOGY_ERROR, String.format("requested VLAN '%d' does not match required VLAN '%d' for STP '%s'", vlanId.get(), virtualPort.getVlanId(), stpId));
    }

    return new ReservationEndPoint(virtualPort);
  }

  private ReservationEndPoint tryEnniPortAsEndPoint(String stpId, Optional<Integer> vlanId, ReservationEndPoint virtualPort) throws ReservationCreationException {
    EnniPort enniPort = physicalPortService.findByNsiV2StpId(stpId);
    if (enniPort == null) {
      return null;
    }

    if (enniPort.isVlanRequired() != vlanId.isPresent()) {
      throw new ReservationCreationException(TOPOLOGY_ERROR, String.format("STP '%s' %s VLAN ID", stpId, enniPort.isVlanRequired() ? "requires" : "does not allow"));
    } else if (enniPort.isVlanRequired() && !enniPort.isVlanIdAllowed(vlanId.get())) {
      throw new ReservationCreationException(TOPOLOGY_ERROR, String.format("requested VLAN '%d' is not allowed by E-NNI does not match required VLAN ranges '%s' for STP '%s'", vlanId.get(), enniPort.getVlanRanges(), stpId));
    }

    return new ReservationEndPoint(enniPort, vlanId);
  }

  @SuppressWarnings("serial")
  public static class ReservationCreationException extends Exception {
    private final ConnectionServiceProviderError errorCode;

    public ReservationCreationException(ConnectionServiceProviderError errorCode, String errorMessage) {
      super(errorMessage);
      this.errorCode = errorCode;
    }

    public ConnectionServiceProviderError getErrorCode() {
      return errorCode;
    }
  }
}
