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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Resource;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Listens for switches that occur behind the haproxy that hides the onecontrol backend
 * When a switch occurs, we must attempt to unsubscribe from our previous server, and re-subscribe to
 * the new one so that we receive notifications from the same server as we are sending inventory/reservation requests to
 */
@Component
@Profile("onecontrol")
public class BackendSwitchListenerHandler implements SOAPHandler<SOAPMessageContext> {

  private static final Logger LOG = LoggerFactory.getLogger(BackendSwitchListenerHandler.class);
  public static final String BACKEND_SERVER_ID_HEADER = "X-Backend-Server";

  private AtomicReference<String> lastKnownServer = new AtomicReference<>();

  @Resource
  private NotificationSubscriber notificationSubscriber;

  @Override
  public Set<QName> getHeaders() {
    return Collections.emptySet();
  }

  @Override
  public boolean handleMessage(SOAPMessageContext context) {

    boolean outbound = (boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

    if (!outbound) { // only look at responses to queries made by our clients

      @SuppressWarnings("unchecked") // the documentation states that we are returned this type
      Map<String, List<String>> headers = (Map<String, List<String>>) context.get(MessageContext.HTTP_RESPONSE_HEADERS);

      if (headers.containsKey(BACKEND_SERVER_ID_HEADER)) {
        final List<String> backendServerValues = headers.get(BACKEND_SERVER_ID_HEADER);
        final String currentBackendServer = backendServerValues.get(0);
        if (lastKnownServer.get() == null) {
          lastKnownServer.set(currentBackendServer); // it is the first request
        } else if (!lastKnownServer.get().equals(currentBackendServer)){
          // a switch has occured
          LOG.info("Switch detected, switching notification subscription from {} to {}", lastKnownServer.get(), currentBackendServer);
          performSwitchTo(currentBackendServer);
        }
        LOG.debug("Received reply from backend server: {}", currentBackendServer);
      }

    }

    return true;
  }

  private synchronized void performSwitchTo(String currentBackendServer) {
    notificationSubscriber.unsubscribe(); // will it just work if we send our unsubscription to the new server?
    notificationSubscriber.subscribe(); // this will go to the new backendserver automatically
    lastKnownServer.set(currentBackendServer);
  }

  @Override
  public boolean handleFault(SOAPMessageContext context) {
    return true;
  }

  @Override
  public void close(MessageContext context) {

  }
}
