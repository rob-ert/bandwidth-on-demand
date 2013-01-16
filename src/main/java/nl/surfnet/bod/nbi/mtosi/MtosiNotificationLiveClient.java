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
package nl.surfnet.bod.nbi.mtosi;

import static nl.surfnet.bod.nbi.mtosi.HeaderBuilder.buildNotificationHeader;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tmforum.mtop.fmw.wsdl.notp.v1_0.NotificationProducer;
import org.tmforum.mtop.fmw.wsdl.notp.v1_0.NotificationProducerHttp;
import org.tmforum.mtop.fmw.wsdl.notp.v1_0.SubscribeException;
import org.tmforum.mtop.fmw.wsdl.notp.v1_0.UnsubscribeException;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.*;

@Service("mtosiNotificationLiveClient")
public class MtosiNotificationLiveClient {

  private static final String WSDL_LOCATION =
    "/mtosi/2.1/DDPs/Framework/IIS/wsdl/NotificationProducer/NotificationProducerHttp.wsdl";

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final NotificationProducerHttp client;

  private final String endPoint;

  @Autowired
  public MtosiNotificationLiveClient(@Value("${nbi.mtosi.notification.retrieval.endpoint}") String endPoint) {
    this.endPoint = endPoint;
    URL wsdlUrl = this.getClass().getResource(WSDL_LOCATION);
    this.client = new NotificationProducerHttp(wsdlUrl,
        new QName("http://www.tmforum.org/mtop/fmw/wsdl/notp/v1-0", "NotificationProducerHttp"));
  }

  public String subscribe(final NotificationTopic topic, final String consumerErp) throws SubscribeException {
    SubscribeRequest subscribeRequest = createSubscribeRequest(topic, consumerErp);

    NotificationProducer proxy = createNotificationProducer();

    SubscribeResponse response = proxy.subscribe(buildNotificationHeader(endPoint), subscribeRequest);

    log.info("Subscription id {}", response.getSubscriptionID());

    return response.getSubscriptionID();
  }

  private SubscribeRequest createSubscribeRequest(final NotificationTopic topic, final String consumerErp) {
    SubscribeRequest subscribeRequest = new ObjectFactory().createSubscribeRequest();
    subscribeRequest.setConsumerEpr(consumerErp);
    subscribeRequest.setTopic(topic.name().toLowerCase());

    return subscribeRequest;
  }

  private NotificationProducer createNotificationProducer() {
    NotificationProducer proxy = client.getNotificationProducerSoapHttp();
    ((BindingProvider) proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPoint);

    return proxy;
  }

  public UnsubscribeResponse unsubscribe(final NotificationTopic topic, final String id) throws UnsubscribeException {
    UnsubscribeRequest unsubscribeRequest = createUnsubscribeRequest(topic, id);

    NotificationProducer proxy = createNotificationProducer();

    return proxy.unsubscribe(buildNotificationHeader(endPoint), unsubscribeRequest);
  }

  private UnsubscribeRequest createUnsubscribeRequest(final NotificationTopic topic, final String id) {
    UnsubscribeRequest unsubscribeRequest = new ObjectFactory().createUnsubscribeRequest();
    unsubscribeRequest.setSubscriptionID(id);
    unsubscribeRequest.setTopic(topic.name().toLowerCase());

    return unsubscribeRequest;
  }

  public enum NotificationTopic {
    FAULT
  }
}
