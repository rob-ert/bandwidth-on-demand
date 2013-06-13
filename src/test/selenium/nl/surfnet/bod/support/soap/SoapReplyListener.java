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
package nl.surfnet.bod.support.soap;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.XMLConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.ws.Holder;

import com.sun.xml.ws.developer.SchemaValidation;
import org.apache.commons.lang.NotImplementedException;
import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.util.EntityUtils;
import org.ogf.schemas.nsi._2013._04.connection.requester.ConnectionRequesterPort;
import org.ogf.schemas.nsi._2013._04.connection.requester.ServiceException;
import org.ogf.schemas.nsi._2013._04.connection.types.ConnectionStatesType;
import org.ogf.schemas.nsi._2013._04.connection.types.DataPlaneStatusType;
import org.ogf.schemas.nsi._2013._04.connection.types.EventEnumType;
import org.ogf.schemas.nsi._2013._04.connection.types.QueryRecursiveResultType;
import org.ogf.schemas.nsi._2013._04.connection.types.QuerySummaryResultType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReservationConfirmCriteriaType;
import org.ogf.schemas.nsi._2013._04.framework.headers.CommonHeaderType;
import org.ogf.schemas.nsi._2013._04.framework.types.ServiceExceptionType;
import org.ogf.schemas.nsi._2013._04.framework.types.TypeValuePairListType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Simple server that always replies "200 OK" and exposes a stack of response objects,
 * which you obviously should only use pop() on.
 *
 */
@WebService(serviceName = "ConnectionServiceRequester", portName = "ConnectionServiceRequesterPort", endpointInterface = "org.ogf.schemas.nsi._2013._04.connection.requester.ConnectionRequesterPort", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/request")
@SchemaValidation
public class SoapReplyListener implements ConnectionRequesterPort {

  private static final Logger LOG = LoggerFactory.getLogger(SoapReplyListener.class);

  private Stack<Map<String, Object>> responses = new Stack<>();

  private static int LISTEN_TIMEOUT = 5000;

  public static final String HEADER_KEY = "header";
  public static final String MESSAGE_TYPE_KEY = "MESSAGE_TYPE";

  public static final String CONNECTION_ID_KEY = "connectionId";
  public static final String GLOBAL_RESERVATION_ID_KEY = "globalReservationId";
  public static final String RESERVATION_CONFIRM_CRITERIA = "reservationConfirmCriteria";

  public Map<String, Object> waitForReply(){
    try {
      Thread.sleep(LISTEN_TIMEOUT);
    } catch (InterruptedException e) {
      throw new IllegalStateException("No reply received in time");
    }
    if (responses.size() > 1){
      throw new IllegalStateException("there are more than one responses left on the queue, don't know which to pop");
    }
    return responses.pop();
  }


  @Override
  public void reserveConfirmed(String connectionId, String globalReservationId, String description, List<ReservationConfirmCriteriaType> reservationConfirmCriteria, Holder<CommonHeaderType> header) throws ServiceException {

    LOG.debug("Received reserveConfirmed for globalResId: " + globalReservationId);

    Map<String, Object> result = new HashMap<>();
    result.put(MESSAGE_TYPE_KEY, "reserveConfirmed");
    result.put(HEADER_KEY, header);

    result.put(CONNECTION_ID_KEY, connectionId);
    result.put(GLOBAL_RESERVATION_ID_KEY, globalReservationId);
    result.put(RESERVATION_CONFIRM_CRITERIA, reservationConfirmCriteria);
    responses.push(result);
  }

  @Override
  public void reserveFailed(@WebParam(name = "connectionId", targetNamespace = "") String s, @WebParam(name = "connectionStates", targetNamespace = "") ConnectionStatesType connectionStatesType, @WebParam(name = "serviceException", targetNamespace = "") ServiceExceptionType serviceExceptionType, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "header") Holder<CommonHeaderType> commonHeaderTypeHolder) throws ServiceException {

  }

  @Override
  public void reserveCommitConfirmed(@WebParam(name = "connectionId", targetNamespace = "") String s, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "header") Holder<CommonHeaderType> commonHeaderTypeHolder) throws ServiceException {

  }

  @Override
  public void reserveCommitFailed(@WebParam(name = "connectionId", targetNamespace = "") String s, @WebParam(name = "connectionStates", targetNamespace = "") ConnectionStatesType connectionStatesType, @WebParam(name = "serviceException", targetNamespace = "") ServiceExceptionType serviceExceptionType, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "header") Holder<CommonHeaderType> commonHeaderTypeHolder) throws ServiceException {

  }

  @Override
  public void reserveAbortConfirmed(@WebParam(name = "connectionId", targetNamespace = "") String s, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "header") Holder<CommonHeaderType> commonHeaderTypeHolder) throws ServiceException {

  }

  @Override
  public void provisionConfirmed(@WebParam(name = "connectionId", targetNamespace = "") String s, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "header") Holder<CommonHeaderType> commonHeaderTypeHolder) throws ServiceException {

  }

  @Override
  public void releaseConfirmed(@WebParam(name = "connectionId", targetNamespace = "") String s, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "header") Holder<CommonHeaderType> commonHeaderTypeHolder) throws ServiceException {

  }

  @Override
  public void terminateConfirmed(@WebParam(name = "connectionId", targetNamespace = "") String s, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "header") Holder<CommonHeaderType> commonHeaderTypeHolder) throws ServiceException {

  }

  @Override
  public void querySummaryConfirmed(@WebParam(name = "reservation", targetNamespace = "") List<QuerySummaryResultType> querySummaryResultTypes, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "header") Holder<CommonHeaderType> commonHeaderTypeHolder) throws ServiceException {

  }

  @Override
  public void querySummaryFailed(@WebParam(name = "serviceException", targetNamespace = "") ServiceExceptionType serviceExceptionType, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "header") Holder<CommonHeaderType> commonHeaderTypeHolder) throws ServiceException {

  }

  @Override
  public void queryRecursiveConfirmed(@WebParam(name = "reservation", targetNamespace = "") List<QueryRecursiveResultType> queryRecursiveResultTypes, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "header") Holder<CommonHeaderType> commonHeaderTypeHolder) throws ServiceException {

  }

  @Override
  public void queryRecursiveFailed(@WebParam(name = "serviceException", targetNamespace = "") ServiceExceptionType serviceExceptionType, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "header") Holder<CommonHeaderType> commonHeaderTypeHolder) throws ServiceException {

  }

  @Override
  public void errorEvent(@WebParam(name = "connectionId", targetNamespace = "") String s, @WebParam(name = "event", targetNamespace = "") EventEnumType eventEnumType, @WebParam(name = "timeStamp", targetNamespace = "") XMLGregorianCalendar xmlGregorianCalendar, @WebParam(name = "additionalInfo", targetNamespace = "") TypeValuePairListType typeValuePairListType, @WebParam(name = "serviceException", targetNamespace = "") ServiceExceptionType serviceExceptionType, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "header") Holder<CommonHeaderType> commonHeaderTypeHolder) throws ServiceException {

  }

  @Override
  public void dataPlaneStateChange(@WebParam(name = "connectionId", targetNamespace = "") String s, @WebParam(name = "dataPlaneStatus", targetNamespace = "") DataPlaneStatusType dataPlaneStatusType, @WebParam(name = "timeStamp", targetNamespace = "") XMLGregorianCalendar xmlGregorianCalendar, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "header") Holder<CommonHeaderType> commonHeaderTypeHolder) throws ServiceException {

  }

  @Override
  public void reserveTimeout(@WebParam(name = "connectionId", targetNamespace = "") String s, @WebParam(name = "timeoutValue", targetNamespace = "") int i, @WebParam(name = "timeStamp", targetNamespace = "") XMLGregorianCalendar xmlGregorianCalendar, @WebParam(name = "originatingConnectionId", targetNamespace = "") String s2, @WebParam(name = "originatingNSA", targetNamespace = "") String s3, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "header") Holder<CommonHeaderType> commonHeaderTypeHolder) throws ServiceException {

  }

  @Override
  public void messageDeliveryTimeout(@WebParam(name = "correlationId", targetNamespace = "") String s, @WebParam(name = "timeStamp", targetNamespace = "") XMLGregorianCalendar xmlGregorianCalendar, @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = WebParam.Mode.INOUT, partName = "header") Holder<CommonHeaderType> commonHeaderTypeHolder) throws ServiceException {

  }
}
