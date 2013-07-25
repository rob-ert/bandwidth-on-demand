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
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.Resource;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import nl.surfnet.bod.nsi.v2.NsiV2Message.Type;

import org.apache.commons.io.IOUtils;
import org.ogf.schemas.nsi._2013._04.framework.headers.CommonHeaderType;
import org.ogf.schemas.nsi._2013._04.framework.headers.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

@Component
public class ConnectionServiceProviderIdempotentMessageHandler implements SOAPHandler<SOAPMessageContext> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionServiceProviderIdempotentMessageHandler.class);

  @Resource private NsiV2MessageRepo messageRepo;
  @Resource private ConnectionServiceRequesterAsyncClient client;


  public Set<QName> getHeaders() {
    return Collections.emptySet();
  }

  public boolean handleMessage(SOAPMessageContext context) {
    try {
      // Parse header
      SOAPMessage message = context.getMessage();
      CommonHeaderType header = parseNsiHeader(message);
      boolean outbound = (boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
      if (outbound) {
        storeMessage(NsiV2Message.Type.SYNC_ACK, header, message);
      } else {
        NsiV2Message originalMessage = messageRepo.findByRequesterNsaAndCorrelationIdAndType(header.getRequesterNSA(), header.getCorrelationId(), NsiV2Message.Type.REQUEST);
        if (originalMessage == null) {
          storeMessage(NsiV2Message.Type.REQUEST, header, message);
          return true;
        }
        NsiV2Message originalAck = messageRepo.findByRequesterNsaAndCorrelationIdAndType(header.getRequesterNSA(), header.getCorrelationId(), NsiV2Message.Type.SYNC_ACK);

        NsiV2Message asyncReply = messageRepo.findByRequesterNsaAndCorrelationIdAndType(header.getRequesterNSA(), header.getCorrelationId(), NsiV2Message.Type.ASYNC_REPLY);
        if (asyncReply != null) {
          SOAPMessage asyncReplySoap = deserializeMessage(asyncReply.getMessage());
          client.asyncSend(new URI(header.getReplyTo()), asyncReply.getSoapAction(), asyncReplySoap);
        }

        SOAPMessage originalAckSoap = deserializeMessage(originalAck.getMessage());
        context.setMessage(originalAckSoap);
        return false;
      }
      return true;
    } catch (SOAPException | JAXBException | IOException | URISyntaxException e) {
      // FIXME
      LOGGER.info("Failed to parse incoming NSIv2 SOAP request", e);
      return false;
    }
  }

  private void storeMessage(Type messageType, CommonHeaderType header, SOAPMessage message) throws IOException, SOAPException {
    String[] soapActionValues = message.getMimeHeaders().getHeader("SOAPAction");
    String soapAction = (soapActionValues != null && soapActionValues.length > 0) ? soapActionValues[0] : null;
    String serializedMessage = serializeMessage(message);
    messageRepo.save(new NsiV2Message(header.getRequesterNSA(), header.getCorrelationId(), messageType, soapAction, serializedMessage));
  }

  private SOAPMessage deserializeMessage(String message) throws IOException, SOAPException {
    return MessageFactory.newInstance().createMessage(new MimeHeaders(), IOUtils.toInputStream(message, "UTF-8"));
  }

  private String serializeMessage(SOAPMessage message) throws IOException, SOAPException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    message.writeTo(baos);
    return baos.toString("UTF-8");
  }

  private CommonHeaderType parseNsiHeader(SOAPMessage message) throws SOAPException, JAXBException {
    Iterator<?> nsiHeaderIterator = message.getSOAPHeader().getChildElements(new ObjectFactory().createNsiHeader(null).getName());
    if (!nsiHeaderIterator.hasNext()) {
      throw new IllegalArgumentException("header not found");
    }
    Element nsiHeader = (Element) nsiHeaderIterator.next();
    return Converters.COMMON_HEADER_CONVERTER.fromDomNode(nsiHeader);
  }

  public boolean handleFault(SOAPMessageContext context) {
//  ???   return handleMessage(context);
    return true;
  }

  public void close(MessageContext context) {
  }
}
