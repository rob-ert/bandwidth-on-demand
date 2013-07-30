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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import nl.surfnet.bod.nsi.v2.NsiV2Message.Type;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ogf.schemas.nsi._2013._04.framework.headers.CommonHeaderType;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionServiceProviderIdempotentMessageHandlerTest {

  @Mock
  private NsiV2MessageRepo messageRepo;

  @Mock
  private ConnectionServiceRequesterAsyncClient client;

  @InjectMocks
  private ConnectionServiceProviderIdempotentMessageHandler subject = new ConnectionServiceProviderIdempotentMessageHandler();

  private SOAPMessage reserveMessage;
  private NsiV2Message reserveEntity;

  private SOAPMessage reserveResponseMessage;
  private NsiV2Message reserveResponseEntity;

  private SOAPMessage reserveConfirmedMessage;
  private NsiV2Message reserveConfirmedEntity;

  @Before
  public void setUp() throws Exception {
    reserveMessage = parseSoapMessage("/web/services/nsiv2/reserve.xml");
    reserveEntity = NsiV2Message.fromSoapMessage(Type.REQUEST, reserveMessage);
    reserveResponseMessage = parseSoapMessage("/web/services/nsiv2/reserveResponse.xml");
    reserveResponseEntity = NsiV2Message.fromSoapMessage(Type.SYNC_ACK, reserveResponseMessage);
    reserveConfirmedMessage = parseSoapMessage("/web/services/nsiv2/reserveConfirmed.xml");
    reserveConfirmedEntity = NsiV2Message.fromSoapMessage(Type.SYNC_ACK, reserveConfirmedMessage);
  }

  @Test
  public void shouldSaveAcknowledgement() throws Exception {
    boolean proceed = subject.handleAcknowledgment(reserveResponseMessage);

    ArgumentCaptor<NsiV2Message> persistedMessage = ArgumentCaptor.forClass(NsiV2Message.class);
    verify(messageRepo, times(1)).save(persistedMessage.capture());
    assertThat("proceed with further handling", proceed, is(true));
    assertThat(persistedMessage.getValue().getType(), is(Type.SYNC_ACK));
  }

  @Test
  public void shouldSaveRequestIfNew() throws Exception {
    NsiV2Message reserveEntity = NsiV2Message.fromSoapMessage(Type.REQUEST, reserveMessage);
    when(messageRepo.findByRequesterNsaAndCorrelationIdAndType(reserveEntity.getRequesterNsa(), reserveEntity.getCorrelationId(), Type.REQUEST)).thenReturn(null);

    SOAPMessage originalAcknowledgment = subject.handleRequest(reserveMessage);

    assertThat(originalAcknowledgment, is(nullValue()));
    ArgumentCaptor<NsiV2Message> persistedMessage = ArgumentCaptor.forClass(NsiV2Message.class);
    verify(messageRepo, times(1)).save(persistedMessage.capture());
  }

  @Test
  public void shouldReturnOriginalAcknowledgmentIfCorrelationIdKnown() throws Exception {
    when(messageRepo.findByRequesterNsaAndCorrelationIdAndType(reserveEntity.getRequesterNsa(), reserveEntity.getCorrelationId(), Type.REQUEST)).thenReturn(reserveEntity);
    when(messageRepo.findByRequesterNsaAndCorrelationIdAndType(reserveEntity.getRequesterNsa(), reserveEntity.getCorrelationId(), Type.SYNC_ACK)).thenReturn(reserveResponseEntity);

    SOAPMessage originalAcknowledgment = subject.handleRequest(reserveMessage);

    assertThat(originalAcknowledgment, is(notNullValue()));
    verify(messageRepo, never()).save(any(NsiV2Message.class));
    verify(client, never()).asyncSend(any(URI.class), any(String.class), any(SOAPMessage.class));
  }

  @Test
  public void shouldSendOriginalAsyncReplyIfCorrelationIdKnown() throws Exception {
    CommonHeaderType header = Converters.parseNsiHeader(reserveMessage);
    when(messageRepo.findByRequesterNsaAndCorrelationIdAndType(reserveEntity.getRequesterNsa(), reserveEntity.getCorrelationId(), Type.REQUEST)).thenReturn(reserveEntity);
    when(messageRepo.findByRequesterNsaAndCorrelationIdAndType(reserveEntity.getRequesterNsa(), reserveEntity.getCorrelationId(), Type.SYNC_ACK)).thenReturn(reserveResponseEntity);
    when(messageRepo.findByRequesterNsaAndCorrelationIdAndType(reserveEntity.getRequesterNsa(), reserveEntity.getCorrelationId(), Type.ASYNC_REPLY)).thenReturn(reserveConfirmedEntity);

    SOAPMessage originalAcknowledgment = subject.handleRequest(reserveMessage);

    assertThat(originalAcknowledgment, is(notNullValue()));
    verify(messageRepo, never()).save(any(NsiV2Message.class));
    verify(client, times(1)).asyncSend(eq(URI.create(header.getReplyTo())), eq(reserveConfirmedEntity.getSoapAction()), any(SOAPMessage.class));
  }

  @Test
  public void shouldSendErrorIfOriginalMessageIsDifferent() throws Exception {
    when(messageRepo.findByRequesterNsaAndCorrelationIdAndType(reserveEntity.getRequesterNsa(), reserveEntity.getCorrelationId(), Type.REQUEST)).thenReturn(reserveResponseEntity);

    SOAPMessage response = subject.handleRequest(reserveMessage);

    assertThat(response, is(notNullValue()));
    assertThat(response.getSOAPBody().getFault(), is(notNullValue()));
    String message = Converters.serializeMessage(response);
    assertThat(message, containsString("request with existing correlation id does not match the original request"));
  }

  private SOAPMessage parseSoapMessage(String filename) throws IOException, SOAPException {
    String reserve = IOUtils.toString(getClass().getResourceAsStream(filename));
    SOAPMessage message = Converters.deserializeMessage(reserve);
    return message;
  }
}
