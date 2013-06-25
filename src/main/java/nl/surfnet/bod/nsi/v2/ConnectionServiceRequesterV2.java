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

import static com.google.common.collect.Lists.transform;
import static nl.surfnet.bod.nsi.v2.ConnectionsV2.toQueryRecursiveResultType;
import static nl.surfnet.bod.nsi.v2.ConnectionsV2.toQuerySummaryResultType;

import java.util.List;

import javax.annotation.Resource;
import javax.xml.datatype.XMLGregorianCalendar;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import nl.surfnet.bod.domain.ConnectionV2;
import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.nsi.ConnectionServiceProviderErrorCodes;
import nl.surfnet.bod.nsi.NsiHelper;
import nl.surfnet.bod.repo.ConnectionV2Repo;
import nl.surfnet.bod.util.XmlUtils;
import org.joda.time.DateTime;
import org.ogf.schemas.nsi._2013._04.connection.types.DataPlaneStateChangeRequestType;
import org.ogf.schemas.nsi._2013._04.connection.types.DataPlaneStatusType;
import org.ogf.schemas.nsi._2013._04.connection.types.ErrorEventType;
import org.ogf.schemas.nsi._2013._04.connection.types.EventEnumType;
import org.ogf.schemas.nsi._2013._04.connection.types.LifecycleStateEnumType;
import org.ogf.schemas.nsi._2013._04.connection.types.NotificationBaseType;
import org.ogf.schemas.nsi._2013._04.connection.types.ProvisionStateEnumType;
import org.ogf.schemas.nsi._2013._04.connection.types.QueryNotificationConfirmedType;
import org.ogf.schemas.nsi._2013._04.connection.types.QueryRecursiveResultType;
import org.ogf.schemas.nsi._2013._04.connection.types.QuerySummaryResultType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReservationConfirmCriteriaType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReservationStateEnumType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReserveTimeoutRequestType;
import org.ogf.schemas.nsi._2013._04.framework.headers.CommonHeaderType;
import org.ogf.schemas.nsi._2013._04.framework.types.ServiceExceptionType;
import org.ogf.schemas.nsi._2013._04.framework.types.TypeValuePairListType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class ConnectionServiceRequesterV2 {

  private static final String PROTOCOL_VERSION = "application/vdn.ogf.nsi.cs.v2.requester+soap";

  @Resource private ConnectionV2Repo connectionRepo;
  @Resource private ConnectionServiceRequesterClient client;

  public void reserveConfirmed(Long id, NsiRequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(id);
    connection.setReservationState(ReservationStateEnumType.RESERVE_HELD);
    connection.setReserveHeldTimeout(Optional.of(new DateTime().plusSeconds(connection.getReserveHeldTimeoutValue())));

    ReservationConfirmCriteriaType criteria = new ReservationConfirmCriteriaType()
      .withBandwidth(connection.getDesiredBandwidth())
      .withPath(connection.getPath())
      .withSchedule(connection.getSchedule())
      .withServiceAttributes(new TypeValuePairListType())
      .withVersion(0);

    client.asyncSendReserveConfirmed(
        requestDetails.getCommonHeaderType(PROTOCOL_VERSION),
        connection.getConnectionId(),
        connection.getGlobalReservationId(),
        connection.getDescription(),
        ImmutableList.of(criteria),
        requestDetails.getReplyTo());
  }

  public void reserveFailed(Long id, NsiRequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(id);
    connection.setReservationState(ReservationStateEnumType.RESERVE_FAILED);

    ServiceExceptionType exception = new ServiceExceptionType()
      .withConnectionId(connection.getConnectionId())
      .withErrorId(ConnectionServiceProviderErrorCodes.CONNECTION.CONNECTION_ERROR.getId())
      .withNsaId(requestDetails.getProviderNsa())
      .withText(connection.getReservation().getFailedReason());

    client.asyncSendReserveFailed(requestDetails.getCommonHeaderType(PROTOCOL_VERSION), connection.getConnectionId(), connection.getConnectionStates(), exception, requestDetails.getReplyTo());
  }

  public void reserveTimeout(Long id, DateTime when) {
    ConnectionV2 connection = connectionRepo.findOne(id);
    connection.setReservationState(ReservationStateEnumType.RESERVE_TIMEOUT);
    final XMLGregorianCalendar timeStamp = XmlUtils.toGregorianCalendar(when);

    NsiRequestDetails requestDetails = connection.getReserveRequestDetails();
    ReserveTimeoutRequestType notification = new ReserveTimeoutRequestType();
    notification.setTimeoutValue(connection.getReserveHeldTimeoutValue());
    populateNotification(notification, connection, timeStamp);
    notification.setOriginatingNSA(requestDetails.getRequesterNsa());
    notification.setOriginatingConnectionId(connection.getConnectionId());

    client.asyncSendReserveTimeout(requestDetails.getCommonHeaderType(PROTOCOL_VERSION), connection.getConnectionId(), connection.getReserveHeldTimeoutValue(), timeStamp, requestDetails.getReplyTo());
  }

  public void abortConfirmed(Long id, NsiRequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(id);
    connection.setReservationState(ReservationStateEnumType.RESERVE_START);

    client.asyncSendAbortConfirmed(requestDetails.getCommonHeaderType(PROTOCOL_VERSION), connection.getConnectionId(), requestDetails.getReplyTo());
  }

  public void terminateConfirmed(Long id, NsiRequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(id);
    connection.setLifecycleState(LifecycleStateEnumType.TERMINATED);

    //client.asy
    client.asyncSendTerminateConfirmed(requestDetails.getCommonHeaderType(PROTOCOL_VERSION), connection.getConnectionId(), requestDetails.getReplyTo());
  }

  public void reserveAbortConfirmed(Long connectionId, NsiRequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(connectionId);
    connection.setReservationState(ReservationStateEnumType.RESERVE_START);

    client.asyncSendReserveAbortConfirmed(requestDetails.getCommonHeaderType(PROTOCOL_VERSION), connection.getConnectionId(), requestDetails.getReplyTo());
  }

  public void reserveCommitConfirmed(Long connectionId, NsiRequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(connectionId);
    connection.setReservationState(ReservationStateEnumType.RESERVE_START);
    connection.setProvisionState(ProvisionStateEnumType.RELEASED);
    connection.setLifecycleState(LifecycleStateEnumType.CREATED);
    connection.setDataPlaneActive(false);

    client.asyncSendReserveCommitConfirmed(requestDetails.getCommonHeaderType(PROTOCOL_VERSION), connection.getConnectionId(), requestDetails.getReplyTo());
  }

  public void provisionConfirmed(Long connectionId, NsiRequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(connectionId);
    connection.setProvisionState(ProvisionStateEnumType.PROVISIONED);

    client.asyncSendProvisionConfirmed(requestDetails.getCommonHeaderType(PROTOCOL_VERSION), connection.getConnectionId(), requestDetails.getReplyTo());
  }

  public void reservePassedEndTime(Long connectionId) {
    ConnectionV2 connection = connectionRepo.findOne(connectionId);
    connection.setLifecycleState(LifecycleStateEnumType.PASSED_END_TIME);
  }

  public void dataPlaneActivated(Long id, NsiRequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(id);
    connection.setDataPlaneActive(true);
    sendDataPlaneStatus(requestDetails, connection, DateTime.now());
  }

  public void dataPlaneDeactivated(Long id, NsiRequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(id);
    connection.setDataPlaneActive(false);

    sendDataPlaneStatus(requestDetails, connection, DateTime.now());
  }

  public void dataPlaneError(Long id, NsiRequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(id);
    connection.setDataPlaneActive(false);
    connection.setLifecycleState(LifecycleStateEnumType.FAILED);

    XMLGregorianCalendar timeStamp = XmlUtils.toGregorianCalendar(DateTime.now());

    ErrorEventType notification = new ErrorEventType();
    notification.setEvent(EventEnumType.DATAPLANE_ERROR);
    populateNotification(notification, connection, timeStamp);

    client.asyncSendDataPlaneError(notification, requestDetails.getCommonHeaderType(PROTOCOL_VERSION), connection.getConnectionId(), timeStamp, requestDetails.getReplyTo());
  }

  public void deactivateFailed(Long id, NsiRequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(id);

    XMLGregorianCalendar timeStamp = XmlUtils.toGregorianCalendar(DateTime.now());
    ErrorEventType notification = new ErrorEventType();
    notification.setEvent(EventEnumType.DEACTIVATE_FAILED);
    populateNotification(notification, connection, timeStamp);
    client.asyncSendDeactivateFailed(notification, requestDetails.getCommonHeaderType(PROTOCOL_VERSION), connection.getConnectionId(), timeStamp, requestDetails.getReplyTo());
  }

  private void sendDataPlaneStatus(NsiRequestDetails requestDetails, ConnectionV2 connection, DateTime when) {
    DataPlaneStatusType dataPlaneStatus = new DataPlaneStatusType().withActive(connection.getDataPlaneActive()).withVersion(0).withVersionConsistent(true);
    XMLGregorianCalendar timeStamp = XmlUtils.toGregorianCalendar(when);

    CommonHeaderType header = requestDetails.getCommonHeaderType(PROTOCOL_VERSION).withCorrelationId(NsiHelper.generateCorrelationId());

    DataPlaneStateChangeRequestType notification = new DataPlaneStateChangeRequestType();
    populateNotification(notification, connection, timeStamp);
    notification.setDataPlaneStatus(dataPlaneStatus);

    client.asyncSendDataPlaneStatus(header, connection.getConnectionId(), dataPlaneStatus, timeStamp, requestDetails.getReplyTo());
  }

  public void querySummaryConfirmed(List<ConnectionV2> connections, NsiRequestDetails requestDetails) {
    List<QuerySummaryResultType> results = transform(connections, toQuerySummaryResultType);

    client.asyncSendQuerySummaryConfirmed(requestDetails.getCommonHeaderType(PROTOCOL_VERSION), results, requestDetails.getReplyTo());
  }

  public void queryRecursiveConfirmed(List<ConnectionV2> connections, NsiRequestDetails requestDetails){
    List<QueryRecursiveResultType> result = transform(connections, toQueryRecursiveResultType);

    client.asyncSendQueryRecursiveConfirmed(requestDetails.getCommonHeaderType(PROTOCOL_VERSION), result, requestDetails.getReplyTo());
  }

  public void queryNotificationConfirmed(List<NotificationBaseType> notifications, NsiRequestDetails requestDetails) {
    QueryNotificationConfirmedType result = new QueryNotificationConfirmedType().withErrorEventOrReserveTimeoutOrMessageDeliveryTimeout(notifications);

    client.asyncSendQueryNotificationConfirmed(requestDetails.getCommonHeaderType(PROTOCOL_VERSION), result, requestDetails.getReplyTo());
  }

  private void populateNotification(final NotificationBaseType notification, final ConnectionV2 connection, final XMLGregorianCalendar timeStamp){
    notification.setConnectionId(connection.getConnectionId());
    notification.setTimeStamp(timeStamp);
    notification.setNotificationId(connection.nextNotificationId());
    connection.addNotification(notification);
  }
}