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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Set;

import javax.annotation.Resource;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import com.google.common.annotations.VisibleForTesting;

import nl.surfnet.bod.nsi.v2.NsiV2Message.Type;

import org.ogf.schemas.nsi._2013._04.connection.requester.ServiceException;
import org.ogf.schemas.nsi._2013._04.framework.headers.CommonHeaderType;
import org.ogf.schemas.nsi._2013._04.framework.types.ServiceExceptionType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Component
public class ConnectionServiceProviderIdempotentMessageHandler implements SOAPHandler<SOAPMessageContext> {

  @Resource private NsiV2MessageRepo messageRepo;
  @Resource private ConnectionServiceRequesterAsyncClient client;
  @Resource private PlatformTransactionManager transactionManager;

  public Set<QName> getHeaders() {
    return Collections.emptySet();
  }

  @Override
  public boolean handleMessage(SOAPMessageContext context) {
    ensureMandatoryTransaction();
    try {
      SOAPMessage message = context.getMessage();
      boolean outbound = (boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
      if (outbound) {
        return handleAcknowledgment(message);
      } else {
        SOAPMessage faultOrOriginalAcknowledgment = handleRequest(message);
        if (faultOrOriginalAcknowledgment == null) {
          // Proceed with new request.
          return true;
        } else {
          // Block further processing and just return the fault or acknowledgment of original request.
          context.setMessage(faultOrOriginalAcknowledgment);
          return false;
        }
      }
    } catch (SOAPException | JAXBException | IOException | URISyntaxException e) {
      throw new RuntimeException("NSIv2 message handler error: " + e, e);
    }
  }

  @Override
  public boolean handleFault(SOAPMessageContext context) {
    return true;
  }

  @Override
  public void close(MessageContext context) {
  }

  @VisibleForTesting
  SOAPMessage handleRequest(SOAPMessage message) throws IOException, SOAPException, URISyntaxException, JAXBException {
    CommonHeaderType header = Converters.parseNsiHeader(message);

    NsiV2Message originalMessage = messageRepo.findByRequesterNsaAndCorrelationIdAndType(header.getRequesterNSA(), header.getCorrelationId(), NsiV2Message.Type.REQUEST);
    if (originalMessage == null) {
      storeMessage(NsiV2Message.Type.REQUEST, message);
      return null;
    } else {
      String request = Converters.serializeMessage(message);
      if (!request.equals(originalMessage.getMessage())) {
        ServiceExceptionType detail = new ServiceExceptionType().withErrorId("100").withText("PAYLOAD_ERROR").withNsaId(header.getProviderNSA());
        return Converters.createSoapFault(header.withReplyTo(null), "request with existing correlation id does not match the original request", detail);
      }
    }

    if (header.getReplyTo() != null) {
      NsiV2Message asyncReply = messageRepo.findByRequesterNsaAndCorrelationIdAndType(header.getRequesterNSA(), header.getCorrelationId(), NsiV2Message.Type.ASYNC_REPLY);
      if (asyncReply != null) {
        SOAPMessage asyncReplySoap = Converters.deserializeMessage(asyncReply.getMessage());
        client.asyncSend(new URI(header.getReplyTo()), asyncReply.getSoapAction(), asyncReplySoap);
      }
    }

    NsiV2Message originalAck = messageRepo.findByRequesterNsaAndCorrelationIdAndType(header.getRequesterNSA(), header.getCorrelationId(), NsiV2Message.Type.SYNC_ACK);
    SOAPMessage originalAckSoap = Converters.deserializeMessage(originalAck.getMessage());
    return originalAckSoap;
  }

  @VisibleForTesting
  boolean handleAcknowledgment(SOAPMessage message) throws IOException, SOAPException, JAXBException {
    storeMessage(NsiV2Message.Type.SYNC_ACK, message);
    return true;
  }

  private void ensureMandatoryTransaction() {
    transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_MANDATORY));
  }

  private void storeMessage(Type messageType, SOAPMessage message) throws IOException, SOAPException, JAXBException {
    messageRepo.save(NsiV2Message.fromSoapMessage(messageType, message));
  }
}
