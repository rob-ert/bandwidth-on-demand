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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import nl.surfnet.bod.nsi.v2.NsiV2Message.Type;
import nl.surfnet.bod.util.JaxbUserType;

import org.ogf.schemas.nsi._2013._04.connection.types.ConnectionStatesType;
import org.ogf.schemas.nsi._2013._04.connection.types.DataPlaneStateChangeRequestType;
import org.ogf.schemas.nsi._2013._04.connection.types.DataPlaneStatusType;
import org.ogf.schemas.nsi._2013._04.connection.types.ErrorEventType;
import org.ogf.schemas.nsi._2013._04.connection.types.GenericConfirmedType;
import org.ogf.schemas.nsi._2013._04.connection.types.GenericFailedType;
import org.ogf.schemas.nsi._2013._04.connection.types.QueryNotificationConfirmedType;
import org.ogf.schemas.nsi._2013._04.connection.types.QueryRecursiveConfirmedType;
import org.ogf.schemas.nsi._2013._04.connection.types.QueryRecursiveResultType;
import org.ogf.schemas.nsi._2013._04.connection.types.QuerySummaryConfirmedType;
import org.ogf.schemas.nsi._2013._04.connection.types.QuerySummaryResultType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReservationConfirmCriteriaType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReserveConfirmedType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReserveTimeoutRequestType;
import org.ogf.schemas.nsi._2013._04.framework.headers.CommonHeaderType;
import org.ogf.schemas.nsi._2013._04.framework.types.ServiceExceptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.DOMException;

@Component
public class ConnectionServiceRequesterClient {

  private final Logger log = LoggerFactory.getLogger(ConnectionServiceRequesterClient.class);

  @Resource private NsiV2MessageRepo messageRepo;

  @Resource private ConnectionServiceRequesterAsyncClient asyncClient;

  private static String soapAction(String action) {
    return "http://schemas.ogf.org/nsi/2013/04/connection/service/" + action;
  }

  public void replyReserveConfirmed(CommonHeaderType header, String connectionId, String globalReservationId, String description, ReservationConfirmCriteriaType criteria, URI replyTo) {
    ReserveConfirmedType body = new ReserveConfirmedType()
      .withConnectionId(connectionId)
      .withGlobalReservationId(globalReservationId)
      .withDescription(description)
      .withCriteria(criteria);
    sendMessage(NsiV2Message.Type.ASYNC_REPLY, replyTo, "reserveConfirmed", header, body, Converters.RESERVE_CONFIRMED_CONVERTER);
  }

  public void replyReserveFailed(CommonHeaderType header, String connectionId, ConnectionStatesType connectionStates, ServiceExceptionType exception, URI replyTo) {
    GenericFailedType body = new GenericFailedType()
      .withConnectionId(connectionId)
      .withConnectionStates(connectionStates)
      .withServiceException(exception);
    sendMessage(NsiV2Message.Type.ASYNC_REPLY, replyTo, "reserveFailed", header, body, Converters.RESERVE_FAILED_CONVERTER);
  }

  public void notifyReserveTimeout(CommonHeaderType header, String connectionId, final int notificationId, int timeoutValue, XMLGregorianCalendar timeStamp, URI replyTo) {
    ReserveTimeoutRequestType body = new ReserveTimeoutRequestType()
      .withConnectionId(connectionId)
      .withTimeoutValue(timeoutValue)
      .withTimeStamp(timeStamp)
      .withNotificationId(notificationId)
      .withOriginatingNSA(header.getProviderNSA())
      .withOriginatingConnectionId(connectionId);
    sendMessage(NsiV2Message.Type.NOTIFICATION, replyTo, "reserveTimeout", header, body, Converters.RESERVE_TIMEOUT_CONVERTER);
  }

  public void replyReserveCommitConfirmed(CommonHeaderType header, String connectionId, URI replyTo) {
    GenericConfirmedType body = new GenericConfirmedType().withConnectionId(connectionId);
    sendMessage(NsiV2Message.Type.ASYNC_REPLY, replyTo, "reserveCommitConfirmed", header, body, Converters.RESERVE_COMMIT_CONFIRMED_CONVERTER);
  }

  public void replyReserveAbortConfirmed(CommonHeaderType header, String connectionId, URI replyTo) {
    GenericConfirmedType body = new GenericConfirmedType().withConnectionId(connectionId);
    sendMessage(NsiV2Message.Type.ASYNC_REPLY, replyTo, "reserveAbortConfirmed", header, body, Converters.RESERVE_ABORT_CONFIRMED_CONVERTER);
  }

  public void replyTerminateConfirmed(CommonHeaderType header, String connectionId, URI replyTo) {
    GenericConfirmedType body = new GenericConfirmedType().withConnectionId(connectionId);
    sendMessage(NsiV2Message.Type.ASYNC_REPLY, replyTo, "terminateConfirmed", header, body, Converters.TERMINATE_CONFIRMED_CONVERTER);
  }

  public void replyProvisionConfirmed(CommonHeaderType header, String connectionId, URI replyTo) {
    GenericConfirmedType body = new GenericConfirmedType().withConnectionId(connectionId);
    sendMessage(NsiV2Message.Type.ASYNC_REPLY, replyTo, "provisionConfirmed", header, body, Converters.PROVISION_CONFIRMED_CONVERTER);
  }

  public void notifyDataPlaneStateChange(CommonHeaderType header, String connectionId, final int notificationId, DataPlaneStatusType dataPlaneStatus, XMLGregorianCalendar timeStamp, URI replyTo) {
    DataPlaneStateChangeRequestType body = new DataPlaneStateChangeRequestType()
      .withConnectionId(connectionId)
      .withNotificationId(notificationId)
      .withDataPlaneStatus(dataPlaneStatus)
      .withTimeStamp(timeStamp);
    sendMessage(NsiV2Message.Type.NOTIFICATION, replyTo, "dataPlaneStateChange", header, body, Converters.DATA_PLANE_STATE_CHANGE_CONVERTER);
  }

  public void notifyDataPlaneError(final ErrorEventType notification, CommonHeaderType header, String connectionId, XMLGregorianCalendar timeStamp, URI replyTo) {
    notifyErrorEvent(notification, header, connectionId, timeStamp, replyTo);
  }

  public void notifyDeactivateFailed(final ErrorEventType notification, CommonHeaderType header, String connectionId, XMLGregorianCalendar timeStamp, URI replyTo) {
    notifyErrorEvent(notification, header, connectionId, timeStamp, replyTo);
  }

  private void notifyErrorEvent(ErrorEventType notification, CommonHeaderType header, String connectionId, XMLGregorianCalendar timeStamp, URI replyTo) {
    ErrorEventType body = new ErrorEventType()
      .withConnectionId(connectionId)
      .withEvent(notification.getEvent())
      .withNotificationId(notification.getNotificationId())
      .withTimeStamp(timeStamp)
      .withAdditionalInfo(notification.getAdditionalInfo())
      .withServiceException(notification.getServiceException());
    sendMessage(NsiV2Message.Type.NOTIFICATION, replyTo, "errorEvent", header, body, Converters.ERROR_EVENT_CONVERTER);
  }

  public void replyQuerySummaryConfirmed(CommonHeaderType header, List<QuerySummaryResultType> results, URI replyTo) {
    QuerySummaryConfirmedType body = new QuerySummaryConfirmedType().withReservation(results);
    sendMessage(NsiV2Message.Type.ASYNC_REPLY, replyTo, "querySummaryConfirmed", header, body, Converters.QUERY_SUMMARY_CONFIRMED_CONVERTER);
  }

  public void replyQueryRecursiveConfirmed(CommonHeaderType header, List<QueryRecursiveResultType> result, URI replyTo) {
    QueryRecursiveConfirmedType body = new QueryRecursiveConfirmedType().withReservation(result);
    sendMessage(NsiV2Message.Type.ASYNC_REPLY, replyTo, "queryRecursiveConfirmed", header, body, Converters.QUERY_RECURSIVE_CONFIRMED_CONVERTER);
  }

  public void replyQueryNotificationConfirmed(CommonHeaderType header, QueryNotificationConfirmedType queryNotificationConfirmed, URI replyTo) {
    sendMessage(NsiV2Message.Type.ASYNC_REPLY, replyTo, "queryNotificationConfirmed", header, queryNotificationConfirmed, Converters.QUERY_NOTIFICATION_CONFIRMED_CONVERTER);
  }

  private <T> void sendMessage(Type type, URI replyTo, String action, CommonHeaderType header, T body, JaxbUserType<T> bodyConverter) {
    log.info("sending {} {} message to {} for requester {} and correlation {}", type, action, replyTo, header.getRequesterNSA(), header.getCorrelationId());
    try {
      SOAPMessage message = Converters.createSoapMessage(header, body, bodyConverter);
      saveMessage(type, soapAction(action), header, message);
      asyncClient.asyncSend(replyTo, soapAction(action), message);
    } catch (SOAPException | DOMException | JAXBException | IOException e) {
      throw new RuntimeException("failed to send " + action + " message to " + header.getRequesterNSA() + " (" + replyTo + ")");
    }
  }

  private void saveMessage(Type type, String soapAction, CommonHeaderType header, SOAPMessage message) throws SOAPException, IOException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      message.writeTo(baos);
      messageRepo.save(new NsiV2Message(header.getRequesterNSA(), header.getCorrelationId(), type, soapAction, baos.toString("UTF-8")));
    }
  }
}
