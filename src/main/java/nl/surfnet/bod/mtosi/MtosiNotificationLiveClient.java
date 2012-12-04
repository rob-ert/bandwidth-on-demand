/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.mtosi;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tmforum.mtop.fmw.wsdl.notp.v1_0.NotificationProducer;
import org.tmforum.mtop.fmw.wsdl.notp.v1_0.NotificationProducerHttp;
import org.tmforum.mtop.fmw.wsdl.notp.v1_0.SubscribeException;
import org.tmforum.mtop.fmw.wsdl.notp.v1_0.UnsubscribeException;
import org.tmforum.mtop.fmw.xsd.hdr.v1.CommunicationPatternType;
import org.tmforum.mtop.fmw.xsd.hdr.v1.CommunicationStyleType;
import org.tmforum.mtop.fmw.xsd.hdr.v1.Header;
import org.tmforum.mtop.fmw.xsd.hdr.v1.MessageTypeType;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.SubscribeRequest;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.SubscribeResponse;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.UnsubscribeRequest;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.UnsubscribeResponse;

@Service("mtosiNotificationLiveClient")
public class MtosiNotificationLiveClient {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private NotificationProducerHttp notificationProducerHttp;

  private final String notificationRetrievalUrl;

  private boolean isInited = false;

  private final String senderUri;

  @Autowired
  public MtosiNotificationLiveClient(@Value("${mtosi.notification.retrieval.endpoint}") String eventRetrievalUrl,
      @Value("${mtosi.notification.sender.uri}") String senderUri) {
    this.notificationRetrievalUrl = eventRetrievalUrl;
    this.senderUri = senderUri;
  }

  // If we do this using a postconstruct then spring will try to initialise this
  // bean (using it's annotation scan path thingie), fails and therefore the
  // complete context will fail during (junit) testing when there is no
  // connection with the mtosi server.
  private void init() {
    if (isInited) {
      return;
    }
    else {
      try {
        notificationProducerHttp = new NotificationProducerHttp(new URL(notificationRetrievalUrl), new QName(
            "http://www.tmforum.org/mtop/fmw/wsdl/notp/v1-0", "NotificationProducerHttp"));

        final Map<String, Object> requestContext = ((BindingProvider) notificationProducerHttp
            .getPort(NotificationProducer.class)).getRequestContext();

        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, notificationRetrievalUrl);
        isInited = true;
      }
      catch (MalformedURLException e) {
        log.error("Error: ", e);
      }
    }

  }

  public String subscribe(final String topic, final String consumerErp) throws SubscribeException {
    init();
    final SubscribeRequest subscribeRequest = new org.tmforum.mtop.fmw.xsd.notmsg.v1.ObjectFactory()
        .createSubscribeRequest();
    subscribeRequest.setConsumerEpr(consumerErp);
    subscribeRequest.setTopic(topic);
    final SubscribeResponse subscribe = notificationProducerHttp.getNotificationProducerSoapHttp().subscribe(
        getRequestHeaders(), subscribeRequest);
    log.info("Subscription id {}", subscribe.getSubscriptionID());
    return subscribe.getSubscriptionID();
  }

  public UnsubscribeResponse unsubscribe(final String id, final String topic) throws UnsubscribeException {
    init();
    final UnsubscribeRequest unsubscribeRequest = new org.tmforum.mtop.fmw.xsd.notmsg.v1.ObjectFactory()
        .createUnsubscribeRequest();
    unsubscribeRequest.setSubscriptionID(id);
    unsubscribeRequest.setTopic(topic);
    final UnsubscribeResponse unsubscribeResponse = notificationProducerHttp.getNotificationProducerSoapHttp()
        .unsubscribe(getRequestHeaders(), unsubscribeRequest);
    return unsubscribeResponse;

  }

  private Holder<Header> getRequestHeaders() {
    final Header header = new Header();
    header.setDestinationURI(notificationRetrievalUrl);
    header.setCommunicationPattern(CommunicationPatternType.SIMPLE_RESPONSE);
    header.setCommunicationStyle(CommunicationStyleType.RPC);
    header.setActivityName("subscribe");
    header.setMsgName("subscribeRequest");
    header.setSenderURI(senderUri);
    header.setMsgType(MessageTypeType.REQUEST);
    return new Holder<Header>(header);
  }
}
