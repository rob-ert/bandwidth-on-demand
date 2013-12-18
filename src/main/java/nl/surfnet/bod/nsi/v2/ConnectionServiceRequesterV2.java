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
import static nl.surfnet.bod.nsi.ConnectionServiceProviderError.RESOURCE_UNAVAILABLE;
import static nl.surfnet.bod.nsi.v2.ConnectionsV2.toQueryRecursiveResultType;
import static nl.surfnet.bod.nsi.v2.ConnectionsV2.toQuerySummaryResultType;

import com.google.common.base.Preconditions;

import java.util.List;
import javax.annotation.Resource;
import javax.xml.datatype.XMLGregorianCalendar;
import com.google.common.base.Optional;
import nl.surfnet.bod.domain.ConnectionV2;
import nl.surfnet.bod.domain.NsiV2RequestDetails;
import nl.surfnet.bod.repo.ConnectionV2Repo;
import nl.surfnet.bod.util.XmlUtils;
import org.joda.time.DateTime;
import org.ogf.schemas.nsi._2013._12.connection.types.DataPlaneStateChangeRequestType;
import org.ogf.schemas.nsi._2013._12.connection.types.DataPlaneStatusType;
import org.ogf.schemas.nsi._2013._12.connection.types.ErrorEventType;
import org.ogf.schemas.nsi._2013._12.connection.types.EventEnumType;
import org.ogf.schemas.nsi._2013._12.connection.types.LifecycleStateEnumType;
import org.ogf.schemas.nsi._2013._12.connection.types.NotificationBaseType;
import org.ogf.schemas.nsi._2013._12.connection.types.ProvisionStateEnumType;
import org.ogf.schemas.nsi._2013._12.connection.types.QueryNotificationConfirmedType;
import org.ogf.schemas.nsi._2013._12.connection.types.QueryRecursiveResultType;
import org.ogf.schemas.nsi._2013._12.connection.types.QuerySummaryResultType;
import org.ogf.schemas.nsi._2013._12.connection.types.ReservationConfirmCriteriaType;
import org.ogf.schemas.nsi._2013._12.connection.types.ReservationStateEnumType;
import org.ogf.schemas.nsi._2013._12.connection.types.ReserveTimeoutRequestType;
import org.ogf.schemas.nsi._2013._12.framework.types.ServiceExceptionType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
class ConnectionServiceRequesterV2 {

  @Resource private ConnectionV2Repo connectionRepo;
  @Resource private ConnectionServiceRequesterClient client;

  public void reserveConfirmed(Long id, NsiV2RequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(id);
    connection.setReservationState(ReservationStateEnumType.RESERVE_HELD);
    connection.setReserveHeldTimeout(Optional.of(new DateTime().plusSeconds(connection.getReserveHeldTimeoutValue())));

    ReservationConfirmCriteriaType criteria = connection.getCriteria();

    client.replyReserveConfirmed(
        requestDetails.createRequesterReplyHeaders(),
        connection.getConnectionId(),
        connection.getGlobalReservationId(),
        connection.getDescription(),
        criteria,
        requestDetails.getReplyTo());
  }

  public void reserveFailed(Long id, NsiV2RequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(id);
    connection.setReservationState(ReservationStateEnumType.RESERVE_FAILED);

    ServiceExceptionType exception = new ServiceExceptionType()
      .withConnectionId(connection.getConnectionId())
      // when nbi failed always assume the reason was resource unavailable
      .withErrorId(RESOURCE_UNAVAILABLE.getErrorId())
      .withNsaId(requestDetails.getProviderNsa())
      .withText(connection.getReservation().getFailedReason());

    client.replyReserveFailed(requestDetails.createRequesterReplyHeaders(), connection.getConnectionId(), connection.getConnectionStates(), exception, requestDetails.getReplyTo());
  }

  public void reserveTimeout(Long id, DateTime timedOutAt) {
    ConnectionV2 connection = connectionRepo.findOne(id);
    connection.setReservationState(ReservationStateEnumType.RESERVE_TIMEOUT);
    final XMLGregorianCalendar timeStamp = XmlUtils.toGregorianCalendar(timedOutAt);

    NsiV2RequestDetails requestDetails = connection.getLastReservationRequestDetails();
    ReserveTimeoutRequestType notification = new ReserveTimeoutRequestType();
    notification.setTimeoutValue(connection.getReserveHeldTimeoutValue());
    populateNotification(notification, connection, timeStamp);
    notification.setOriginatingNSA(requestDetails.getRequesterNsa());
    notification.setOriginatingConnectionId(connection.getConnectionId());

    client.notifyReserveTimeout(requestDetails.createRequesterNotificationHeaders(), connection.getConnectionId(), notification.getNotificationId(),  connection.getReserveHeldTimeoutValue(), timeStamp, requestDetails.getReplyTo());
  }

  public void reserveAbortConfirmed(Long id, NsiV2RequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(id);
    connection.setReservationState(ReservationStateEnumType.RESERVE_START);

    client.replyReserveAbortConfirmed(requestDetails.createRequesterReplyHeaders(), connection.getConnectionId(), requestDetails.getReplyTo());
  }

  public void terminateConfirmed(Long id, NsiV2RequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(id);
    connection.setLifecycleState(LifecycleStateEnumType.TERMINATED);

    client.replyTerminateConfirmed(requestDetails.createRequesterReplyHeaders(), connection.getConnectionId(), requestDetails.getReplyTo());
  }

  public void reserveCommitConfirmed(Long connectionId, NsiV2RequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(connectionId);
    connection.setReservationState(ReservationStateEnumType.RESERVE_START);
    connection.setProvisionState(ProvisionStateEnumType.RELEASED);
    connection.setLifecycleState(LifecycleStateEnumType.CREATED);
    connection.setDataPlaneActive(false);

    client.replyReserveCommitConfirmed(requestDetails.createRequesterReplyHeaders(), connection.getConnectionId(), requestDetails.getReplyTo());
  }

  public void provisionConfirmed(Long connectionId, NsiV2RequestDetails requestDetails) {
    Preconditions.checkNotNull(requestDetails, "cannot provision without provision request details");

    ConnectionV2 connection = connectionRepo.findOne(connectionId);
    connection.setProvisionState(ProvisionStateEnumType.PROVISIONED);

    client.replyProvisionConfirmed(requestDetails.createRequesterReplyHeaders(), connection.getConnectionId(), requestDetails.getReplyTo());
  }

  public void reservePassedEndTime(Long connectionId) {
    ConnectionV2 connection = connectionRepo.findOne(connectionId);
    connection.setLifecycleState(LifecycleStateEnumType.PASSED_END_TIME);
  }

  public void forcedEnd(final Long connectionId, final NsiV2RequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(connectionId);
    connection.setLifecycleState(LifecycleStateEnumType.FAILED);

    XMLGregorianCalendar timeStamp = XmlUtils.toGregorianCalendar(DateTime.now());
    ErrorEventType notification = new ErrorEventType();
    notification.setEvent(EventEnumType.FORCED_END);
    populateNotification(notification, connection, timeStamp);

    client.notifyForcedEnd(notification, requestDetails.createRequesterNotificationHeaders(), connection.getConnectionId(), timeStamp, requestDetails.getReplyTo());

  }

  public void dataPlaneActivated(Long id, NsiV2RequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(id);
    connection.setDataPlaneActive(true);
    sendDataPlaneStatus(requestDetails, connection, DateTime.now());
  }

  public void dataPlaneDeactivated(Long id, NsiV2RequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(id);
    connection.setDataPlaneActive(false);

    sendDataPlaneStatus(requestDetails, connection, DateTime.now());
  }

  public void dataPlaneError(Long id, NsiV2RequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(id);
    connection.setDataPlaneActive(false);
    connection.setLifecycleState(LifecycleStateEnumType.FAILED);

    XMLGregorianCalendar timeStamp = XmlUtils.toGregorianCalendar(DateTime.now());

    ErrorEventType notification = new ErrorEventType();
    notification.setEvent(EventEnumType.DATAPLANE_ERROR);
    populateNotification(notification, connection, timeStamp);

    client.notifyDataPlaneError(notification, requestDetails.createRequesterNotificationHeaders(), connection.getConnectionId(), timeStamp, requestDetails.getReplyTo());
  }

  public void deactivateFailed(Long id, NsiV2RequestDetails requestDetails) {
    ConnectionV2 connection = connectionRepo.findOne(id);

    XMLGregorianCalendar timeStamp = XmlUtils.toGregorianCalendar(DateTime.now());
    ErrorEventType notification = new ErrorEventType();
    notification.setEvent(EventEnumType.DEACTIVATE_FAILED);
    populateNotification(notification, connection, timeStamp);
    client.notifyDeactivateFailed(notification, requestDetails.createRequesterNotificationHeaders(), connection.getConnectionId(), timeStamp, requestDetails.getReplyTo());
  }

  private void sendDataPlaneStatus(NsiV2RequestDetails requestDetails, ConnectionV2 connection, DateTime when) {
    DataPlaneStatusType dataPlaneStatus = new DataPlaneStatusType().withActive(connection.getDataPlaneActive()).withVersion(0).withVersionConsistent(true);
    XMLGregorianCalendar timeStamp = XmlUtils.toGregorianCalendar(when);

    DataPlaneStateChangeRequestType notification = new DataPlaneStateChangeRequestType();
    populateNotification(notification, connection, timeStamp);
    notification.setDataPlaneStatus(dataPlaneStatus);

    client.notifyDataPlaneStateChange(requestDetails.createRequesterNotificationHeaders(), connection.getConnectionId(), notification.getNotificationId(), dataPlaneStatus, timeStamp, requestDetails.getReplyTo());
  }

  public void querySummaryConfirmed(List<ConnectionV2> connections, NsiV2RequestDetails requestDetails) {
    List<QuerySummaryResultType> results = transform(connections, toQuerySummaryResultType);

    client.replyQuerySummaryConfirmed(requestDetails.createRequesterReplyHeaders(), results, requestDetails.getReplyTo());
  }

  public void queryRecursiveConfirmed(List<ConnectionV2> connections, NsiV2RequestDetails requestDetails){
    List<QueryRecursiveResultType> result = transform(connections, toQueryRecursiveResultType);

    client.replyQueryRecursiveConfirmed(requestDetails.createRequesterReplyHeaders(), result, requestDetails.getReplyTo());
  }

  public void queryNotificationConfirmed(List<NotificationBaseType> notifications, NsiV2RequestDetails requestDetails) {
    QueryNotificationConfirmedType result = new QueryNotificationConfirmedType().withErrorEventOrReserveTimeoutOrDataPlaneStateChange(notifications);

    client.replyQueryNotificationConfirmed(requestDetails.createRequesterReplyHeaders(), result, requestDetails.getReplyTo());
  }

  private void populateNotification(final NotificationBaseType notification, final ConnectionV2 connection, final XMLGregorianCalendar timeStamp){
    notification.setConnectionId(connection.getConnectionId());
    notification.setTimeStamp(timeStamp);
    notification.setNotificationId(connection.nextNotificationId());
    connection.addNotification(notification);
  }


}
