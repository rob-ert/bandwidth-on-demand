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

import nl.surfnet.bod.domain.ConnectionV2;
import nl.surfnet.bod.util.JaxbUserType;
import nl.surfnet.bod.util.NsiV2UserType;
import org.apache.commons.io.IOUtils;
import org.ogf.schemas.nsi._2013._12.connection.types.*;
import org.ogf.schemas.nsi._2013._12.framework.headers.CommonHeaderType;
import org.ogf.schemas.nsi._2013._12.framework.headers.ObjectFactory;
import org.ogf.schemas.nsi._2013._12.framework.types.ServiceExceptionType;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

class Converters {
  private static final org.ogf.schemas.nsi._2013._12.framework.headers.ObjectFactory HEADER_OF = new org.ogf.schemas.nsi._2013._12.framework.headers.ObjectFactory();
  private static final org.ogf.schemas.nsi._2013._12.connection.types.ObjectFactory BODY_OF = new org.ogf.schemas.nsi._2013._12.connection.types.ObjectFactory();

  public static final JaxbUserType<ReservationConfirmCriteriaType> RESERVATION_CONFIRM_CRITERIA_TYPE = new ConnectionV2.ReservationConfirmCriteriaTypeUserType();
  public static final JaxbUserType<CommonHeaderType> COMMON_HEADER_CONVERTER = new NsiV2UserType<>(HEADER_OF.createNsiHeader(null));
  public static final JaxbUserType<ReserveConfirmedType> RESERVE_CONFIRMED_CONVERTER = new NsiV2UserType<>(BODY_OF.createReserveConfirmed(null));
  public static final JaxbUserType<GenericFailedType> RESERVE_FAILED_CONVERTER = new NsiV2UserType<>(BODY_OF.createReserveFailed(null));
  public static final JaxbUserType<ReserveTimeoutRequestType> RESERVE_TIMEOUT_CONVERTER = new NsiV2UserType<>(BODY_OF.createReserveTimeout(null));
  public static final JaxbUserType<GenericConfirmedType> RESERVE_COMMIT_CONFIRMED_CONVERTER = new NsiV2UserType<>(BODY_OF.createReserveCommitConfirmed(null));
  public static final JaxbUserType<GenericConfirmedType> RESERVE_ABORT_CONFIRMED_CONVERTER = new NsiV2UserType<>(BODY_OF.createReserveAbortConfirmed(null));
  public static final JaxbUserType<GenericConfirmedType> TERMINATE_CONFIRMED_CONVERTER = new NsiV2UserType<>(BODY_OF.createTerminateConfirmed(null));
  public static final JaxbUserType<GenericConfirmedType> PROVISION_CONFIRMED_CONVERTER = new NsiV2UserType<>(BODY_OF.createProvisionConfirmed(null));
  public static final JaxbUserType<GenericConfirmedType> RELEASE_CONFIRMED_CONVERTER = new NsiV2UserType<>(BODY_OF.createReleaseConfirmed(null));
  public static final JaxbUserType<GenericErrorType> ERROR_CONVERTER = new NsiV2UserType<>(BODY_OF.createError(null));

  public static final JaxbUserType<DataPlaneStateChangeRequestType> DATA_PLANE_STATE_CHANGE_CONVERTER = new NsiV2UserType<>(BODY_OF.createDataPlaneStateChange(null));
  public static final JaxbUserType<ErrorEventType> ERROR_EVENT_CONVERTER = new NsiV2UserType<>(BODY_OF.createErrorEvent(null));
  public static final JaxbUserType<QuerySummaryConfirmedType> QUERY_SUMMARY_CONFIRMED_CONVERTER = new NsiV2UserType<>(BODY_OF.createQuerySummaryConfirmed(null));
  public static final JaxbUserType<QueryRecursiveConfirmedType> QUERY_RECURSIVE_CONFIRMED_CONVERTER = new NsiV2UserType<>(BODY_OF.createQueryRecursiveConfirmed(null));
  public static final JaxbUserType<QueryNotificationConfirmedType> QUERY_NOTIFICATION_CONFIRMED_CONVERTER = new NsiV2UserType<>(BODY_OF.createQueryNotificationConfirmed(null));
  public static final JaxbUserType<QueryResultConfirmedType> QUERY_RESULT_CONFIRMED_CONVERTER = new NsiV2UserType<>(BODY_OF.createQueryResultConfirmed(null));
  public static final JaxbUserType<ServiceExceptionType> SERVICE_EXCEPTION_CONVERTER = new NsiV2UserType<>(BODY_OF.createServiceException(null));

  public static CommonHeaderType parseNsiHeader(SOAPMessage message) throws SOAPException, JAXBException {
    Iterator<?> nsiHeaderIterator = message.getSOAPHeader().getChildElements(new ObjectFactory().createNsiHeader(null).getName());
    if (!nsiHeaderIterator.hasNext()) {
      throw new IllegalArgumentException("header not found");
    }
    Element nsiHeader = (Element) nsiHeaderIterator.next();
    return COMMON_HEADER_CONVERTER.fromDomElement(nsiHeader);
  }

  public static <T> T parseBody(JaxbUserType<T> converter, SOAPMessage message) throws SOAPException, JAXBException {
    Iterator<?> iterator = message.getSOAPBody().getChildElements(converter.getXmlRootElementName());
    if (!iterator.hasNext()) {
      throw new IllegalArgumentException("body element " + converter.getXmlRootElementName() + " not found");
    }
    Element bodyElement = (Element) iterator.next();
    return converter.fromDomElement(bodyElement);
  }

  public static SOAPMessage deserializeMessage(String message) throws IOException, SOAPException {
    return MessageFactory.newInstance().createMessage(new MimeHeaders(), IOUtils.toInputStream(message, "UTF-8"));
  }

  public static String serializeMessage(SOAPMessage message) throws IOException, SOAPException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    message.writeTo(baos);
    return baos.toString("UTF-8");
  }

  public static <T> SOAPMessage createSoapMessage(CommonHeaderType header, T body, JaxbUserType<T> bodyConverter) throws SOAPException, JAXBException {
    SOAPMessage message = MessageFactory.newInstance().createMessage();
    SOAPFactory factory = SOAPFactory.newInstance();
    message.getSOAPHeader().addChildElement(factory.createElement(COMMON_HEADER_CONVERTER.toDomElement(header)));
    message.getSOAPBody().addChildElement(factory.createElement(bodyConverter.toDomElement(body)));
    return message;
  }

  public static <T> SOAPMessage createSoapFault(CommonHeaderType header, String faultString, ServiceExceptionType exception) throws SOAPException, JAXBException, IOException {
    SOAPMessage message = MessageFactory.newInstance().createMessage();
    SOAPFactory factory = SOAPFactory.newInstance();
    message.getSOAPHeader().addChildElement(factory.createElement(COMMON_HEADER_CONVERTER.toDomElement(header)));
    SOAPFault fault = message.getSOAPBody().addFault(new QName(SOAPConstants.URI_NS_SOAP_ENVELOPE, "Server"), faultString);
    fault.addDetail().addChildElement(factory.createElement(SERVICE_EXCEPTION_CONVERTER.toDomElement(exception)));
    return message;
  }
}
