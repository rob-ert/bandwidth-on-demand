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

import static nl.surfnet.bod.nsi.v2.Converters.COMMON_HEADER_CONVERTER;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.ogf.schemas.nsi._2013._07.framework.headers.CommonHeaderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Wraps a transaction around incoming SOAP requests. The transaction is rolled
 * back in case of a fault.
 */
@Component
public class UpdateNsiHeadersForAckHandler implements SOAPHandler<SOAPMessageContext> {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateNsiHeadersForAckHandler.class);

  static final String SAVED_HEADERS_PROPERTY = UpdateNsiHeadersForAckHandler.class.getName() + ".HEADERS";

  @Override
  public boolean handleMessage(SOAPMessageContext context) {
    return handleNsiHeaders(context);
  }

  @Override
  public boolean handleFault(SOAPMessageContext context) {
    return handleNsiHeaders(context);
  }

  private boolean handleNsiHeaders(SOAPMessageContext context) {
    try {
      boolean outbound = (boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
      if (outbound) {
        return updateAcknowledgmentHeaders(context);
      } else {
        return saveRequestHeaders(context);
      }
    } catch (Exception e) {
      LOGGER.warn("Processing of NSI header failed in context '" + context + "': " + e, e);
      return true;
    }
  }

  private boolean saveRequestHeaders(SOAPMessageContext context) {
    Object[] headers = context.getHeaders(COMMON_HEADER_CONVERTER.getXmlRootElementName(), COMMON_HEADER_CONVERTER.getJaxbContext(), false);
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Found " + headers.length + " headers: " + Arrays.toString(headers));
    }

    if (headers.length == 1 && headers[0] instanceof JAXBElement<?> && ((JAXBElement<?>) headers[0]).getValue() instanceof CommonHeaderType) {
      CommonHeaderType nsiHeaders = (CommonHeaderType) ((JAXBElement<?>) headers[0]).getValue();
      context.put(SAVED_HEADERS_PROPERTY, nsiHeaders);
      context.setScope(SAVED_HEADERS_PROPERTY, Scope.HANDLER);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Saving headers " + nsiHeaders + " to context");
      }
    } else {
      LOGGER.info("No NSI headers present or headers did not match type: " + Arrays.toString(headers));
    }
    return true;
  }

  private boolean updateAcknowledgmentHeaders(SOAPMessageContext context) throws SOAPException {
    CommonHeaderType headers = (CommonHeaderType) context.get(SAVED_HEADERS_PROPERTY);
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Header retrieved from context: " + headers);
    }

    if (headers == null) {
      return true;
    }

    headers.setReplyTo(null);
    updateSoapNsiHeaders(context.getMessage().getSOAPHeader(), headers);

    return true;
  }

  private void updateSoapNsiHeaders(SOAPHeader soapHeader, CommonHeaderType headers) {
    for (Iterator<?> it = soapHeader.getChildElements(COMMON_HEADER_CONVERTER.getXmlRootElementName()); it.hasNext();) {
      Node child = (Node) it.next();
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Removing headers " + child);
      }
      soapHeader.removeChild(child);
    }
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Appending headers " + headers);
    }
    Element headersElement = COMMON_HEADER_CONVERTER.toDomElement(headers);
    soapHeader.appendChild(soapHeader.getOwnerDocument().adoptNode(headersElement));
  }

  @Override
  public void close(MessageContext context) {
  }

  @Override
  public Set<QName> getHeaders() {
    return Collections.singleton(COMMON_HEADER_CONVERTER.getXmlRootElementName());
  }
}
