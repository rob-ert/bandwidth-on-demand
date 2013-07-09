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
package nl.surfnet.bod.support.soap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;

import com.sun.xml.ws.developer.SchemaValidation;

import org.ogf.schemas.nsi._2013._04.connection.requester.ConnectionRequesterPort;
import org.ogf.schemas.nsi._2013._04.connection.requester.ServiceException;
import org.ogf.schemas.nsi._2013._04.connection.types.ConnectionStatesType;
import org.ogf.schemas.nsi._2013._04.connection.types.DataPlaneStatusType;
import org.ogf.schemas.nsi._2013._04.connection.types.EventEnumType;
import org.ogf.schemas.nsi._2013._04.connection.types.GenericAcknowledgmentType;
import org.ogf.schemas.nsi._2013._04.connection.types.QueryNotificationConfirmedType;
import org.ogf.schemas.nsi._2013._04.connection.types.QueryRecursiveResultType;
import org.ogf.schemas.nsi._2013._04.connection.types.QuerySummaryResultType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReservationConfirmCriteriaType;
import org.ogf.schemas.nsi._2013._04.framework.headers.CommonHeaderType;
import org.ogf.schemas.nsi._2013._04.framework.types.ServiceExceptionType;
import org.ogf.schemas.nsi._2013._04.framework.types.TypeValuePairListType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple server that always replies "200 OK" and exposes a stack of response objects,
 * which you obviously should only use pop() on.
 *
 */
@WebService(serviceName = "ConnectionServiceRequester", portName = "ConnectionServiceRequesterPort", endpointInterface = "org.ogf.schemas.nsi._2013._04.connection.requester.ConnectionRequesterPort", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/request")
@SchemaValidation
public class SoapReplyListener implements ConnectionRequesterPort {

  private static final Logger LOG = LoggerFactory.getLogger(SoapReplyListener.class);


  private BlockingQueue<Map<String, Object>> responses = new ArrayBlockingQueue<>(1); // we can only deal with one response at a time

  public static final String HEADER_KEY = "headerHolder";
  public static final String MESSAGE_TYPE_KEY = "MESSAGE_TYPE";

  public static final String CONNECTION_ID_KEY = "connectionId";
  public static final String GLOBAL_RESERVATION_ID_KEY = "globalReservationId";
  public static final String RESERVATION_CONFIRM_CRITERIA = "reservationConfirmCriteria";

  public static final String QUERY_RESULT_KEY = "QUERY_RESULT";

  public Map<String, Object> getLastReply(){
    try {
      return responses.poll(5000, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException("response did not arrive in time", e);
    }
  }

  @Override
  public void reserveConfirmed(String connectionId, String globalReservationId, String description, ReservationConfirmCriteriaType reservationConfirmCriteria, Holder<CommonHeaderType> header) throws ServiceException {
    LOG.debug("Received reserveConfirmed for globalResId: " + globalReservationId);

    Map<String, Object> result = new HashMap<>();
    result.put(MESSAGE_TYPE_KEY, "reserveConfirmed");
    result.put(HEADER_KEY, header.value);

    result.put(CONNECTION_ID_KEY, connectionId);
    result.put(GLOBAL_RESERVATION_ID_KEY, globalReservationId);
    result.put(RESERVATION_CONFIRM_CRITERIA, reservationConfirmCriteria);
    responses.add(result);
  }

  @Override
  public void reserveFailed(@WebParam(name = "connectionId", targetNamespace = "") String s, @WebParam(name = "connectionStates", targetNamespace = "") ConnectionStatesType connectionStatesType, @WebParam(name = "serviceException", targetNamespace = "") ServiceExceptionType serviceExceptionType, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "headerHolder") Holder<CommonHeaderType> commonHeaderTypeHolder) throws ServiceException {
    throw new RuntimeException("Not implemented (or not required)");
  }

  @Override
  public void reserveCommitConfirmed(String connectionId, Holder<CommonHeaderType> header) throws ServiceException {
    LOG.debug("Received reserveCommitConfirmed for connectionId: " + connectionId);

    Map<String, Object> result = new HashMap<>();
    result.put(MESSAGE_TYPE_KEY, "reserveCommitConfirmed");
    result.put(HEADER_KEY, header.value);

    result.put(CONNECTION_ID_KEY, connectionId);
    responses.add(result);

  }

  @Override
  public void reserveCommitFailed(@WebParam(name = "connectionId", targetNamespace = "") String s, @WebParam(name = "connectionStates", targetNamespace = "") ConnectionStatesType connectionStatesType, @WebParam(name = "serviceException", targetNamespace = "") ServiceExceptionType serviceExceptionType, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "headerHolder") Holder<CommonHeaderType> commonHeaderTypeHolder) throws ServiceException {
    throw new RuntimeException("Not implemented (or not required)");
  }

  @Override
  public void reserveAbortConfirmed(@WebParam(name = "connectionId", targetNamespace = "") String s, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "headerHolder") Holder<CommonHeaderType> commonHeaderTypeHolder) throws ServiceException {
    throw new RuntimeException("Not implemented (or not required)");
  }

  @Override
  public void provisionConfirmed(String connectionId, Holder<CommonHeaderType> headerHolder) throws ServiceException {
    LOG.debug("Received provisionConfirmed for connectionId: " + connectionId);

    Map<String, Object> result = new HashMap<>();
    result.put(MESSAGE_TYPE_KEY, "provisionConfirmed");
    result.put(HEADER_KEY, headerHolder.value);
    result.put(CONNECTION_ID_KEY, connectionId);
    responses.add(result);
  }

  @Override
  public void releaseConfirmed(@WebParam(name = "connectionId", targetNamespace = "") String s, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "headerHolder") Holder<CommonHeaderType> commonHeaderTypeHolder) throws ServiceException {
    throw new RuntimeException("Not implemented (or not required)");
  }

  @Override
  public void terminateConfirmed(@WebParam(name = "connectionId", targetNamespace = "") String s, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "headerHolder") Holder<CommonHeaderType> commonHeaderTypeHolder) throws ServiceException {
    throw new RuntimeException("Not implemented (or not required)");
  }

  @Override
  public void querySummaryConfirmed(List<QuerySummaryResultType> querySummaryResult, Holder<CommonHeaderType> header) throws ServiceException {
    LOG.debug("Received querySummaryConfirmed");

    Map<String, Object> result = new HashMap<>();
    result.put(MESSAGE_TYPE_KEY, "querySummaryConfirmed");
    result.put(HEADER_KEY, header.value);

    result.put(QUERY_RESULT_KEY, querySummaryResult);
    responses.add(result);
  }

  @Override
  public void querySummaryFailed(@WebParam(name = "serviceException", targetNamespace = "") ServiceExceptionType serviceExceptionType, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "headerHolder") Holder<CommonHeaderType> commonHeaderTypeHolder) throws ServiceException {
    throw new RuntimeException("Not implemented (or not required)");
  }

  @Override
  public void queryRecursiveConfirmed(@WebParam(name = "reservation", targetNamespace = "") List<QueryRecursiveResultType> queryRecursiveResultTypes, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "headerHolder") Holder<CommonHeaderType> commonHeaderTypeHolder) throws ServiceException {
    throw new RuntimeException("Not implemented (or not required)");
  }

  @Override
  public GenericAcknowledgmentType queryNotificationConfirmed(QueryNotificationConfirmedType queryNotificationConfirmed, Holder<CommonHeaderType> header) throws ServiceException {
    throw new RuntimeException("Not implemented (or not required)");
  }

  @Override
  public void queryNotificationFailed(ServiceExceptionType serviceException, Holder<CommonHeaderType> header)
      throws ServiceException {
    throw new RuntimeException("Not implemented (or not required)");
  }

  @Override
  public void errorEvent(String connectionId, int notificationId, XMLGregorianCalendar timeStamp, EventEnumType event, TypeValuePairListType additionalInfo, ServiceExceptionType serviceException, Holder<CommonHeaderType> header) throws ServiceException {
    throw new RuntimeException("Not implemented (or not required)");
  }

  @Override
  public void dataPlaneStateChange(String connectionId, int notificationId, XMLGregorianCalendar timeStamp, DataPlaneStatusType dataPlaneStatus, Holder<CommonHeaderType> header)
      throws ServiceException {
    throw new RuntimeException("Not implemented (or not required)");
  }

  @Override
  public void reserveTimeout(String connectionId, int notificationId, XMLGregorianCalendar timeStamp, int timeoutValue, String originatingConnectionId, String originatingNSA, Holder<CommonHeaderType> header)
      throws ServiceException {
    throw new RuntimeException("Not implemented (or not required)");
  }

  @Override
  public void messageDeliveryTimeout(String connectionId, int notificationId, XMLGregorianCalendar timeStamp, String correlationId, Holder<CommonHeaderType> header) throws ServiceException {
    throw new RuntimeException("Not implemented (or not required)");
  }

  @Override
  public void queryRecursiveFailed(ServiceExceptionType serviceException, Holder<CommonHeaderType> header) throws ServiceException {
    throw new RuntimeException("Not implemented (or not required)");
  }

}
