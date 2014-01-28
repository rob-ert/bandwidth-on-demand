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
package nl.surfnet.bod.nbi.onecontrol;

import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BackendSwitchListenerHandlerTest {

  @Mock
  NotificationSubscriber notificationSubscriber;

  @InjectMocks
  private BackendSwitchListenerHandler subject = new BackendSwitchListenerHandler();

  @Mock
  private SOAPMessageContext soapMessageContext;

  private Map<String, List<String>> headers;

  @Before
  public void before(){
    when(soapMessageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).thenReturn(false);
    // default to primary
    headers = new HashMap<>();
    headers.put(BackendSwitchListenerHandler.BACKEND_SERVER_ID_HEADER, Arrays.asList("primary"));
    when(soapMessageContext.get(MessageContext.HTTP_RESPONSE_HEADERS)).thenReturn(headers);
  }

  @Test
  public void outboundMessagesHaveNoEffect() throws Exception {
    when(soapMessageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).thenReturn(true);
    subject.handleMessage(soapMessageContext);
    verifyZeroInteractions(notificationSubscriber);
  }

  @Test
  public void firstInvocationHasNoEffectOnSubscription(){
    subject.handleMessage(soapMessageContext);
    verifyZeroInteractions(notificationSubscriber);
  }

  @Test
  public void changeInConfiguredHeaderCausesSwitch(){
    subject.handleMessage(soapMessageContext);
    verifyZeroInteractions(notificationSubscriber);
    headers.put(BackendSwitchListenerHandler.BACKEND_SERVER_ID_HEADER, Arrays.asList("secondary"));
    subject.handleMessage(soapMessageContext);
    verify(notificationSubscriber).unsubscribe();
    verify(notificationSubscriber).subscribe();
  }

}
