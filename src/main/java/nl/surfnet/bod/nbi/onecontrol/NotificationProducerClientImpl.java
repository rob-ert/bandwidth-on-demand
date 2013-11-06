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

import static nl.surfnet.bod.nbi.onecontrol.HeaderBuilder.buildSubscribeHeader;
import static nl.surfnet.bod.nbi.onecontrol.HeaderBuilder.buildUnsubscribeHeader;

import javax.annotation.Resource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import com.sun.xml.ws.developer.JAXWSProperties;

import nl.surfnet.bod.nbi.onecontrol.OneControlInstance.OneControlConfiguration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.tmforum.mtop.fmw.wsdl.notp.v1_0.NotificationProducer;
import org.tmforum.mtop.fmw.wsdl.notp.v1_0.NotificationProducerHttp;
import org.tmforum.mtop.fmw.wsdl.notp.v1_0.SubscribeException;
import org.tmforum.mtop.fmw.wsdl.notp.v1_0.UnsubscribeException;
import org.tmforum.mtop.fmw.xsd.hdr.v1.Header;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.ObjectFactory;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.SubscribeRequest;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.SubscribeResponse;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.UnsubscribeRequest;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.UnsubscribeResponse;

@Profile("onecontrol")
@Service
public class NotificationProducerClientImpl implements NotificationProducerClient {

  private static final String WSDL_LOCATION = "/mtosi/2.1/DDPs/Framework/IIS/wsdl/NotificationProducer/NotificationProducerHttp.wsdl";

  @Resource private OneControlInstance oneControlInstance;

  @Value("${onecontrol.notification.producer.connect.timeout}")
  private int connectTimeout;

  @Value("${onecontrol.notification.producer.request.timeout}")
  private int requestTimeout;

  @Override
  public String subscribe(NotificationTopic topic, String consumerErp) throws SubscribeException {
    return subscribe(topic, consumerErp, oneControlInstance.getCurrentConfiguration());
  }

  private String subscribe(NotificationTopic topic, String consumerErp, OneControlConfiguration configuration) throws SubscribeException {
    NotificationProducer port = createPort(configuration);

    Holder<Header> header = buildSubscribeHeader(configuration);
    SubscribeRequest body = createSubscribeRequest(topic, consumerErp);

    SubscribeResponse response = port.subscribe(header, body);

    return response.getSubscriptionID();
  }

  @Override
  public UnsubscribeResponse unsubscribe(NotificationTopic topic, String id) throws UnsubscribeException {
    return unsubscribe(topic, id, oneControlInstance.getCurrentConfiguration());
  }

  private UnsubscribeResponse unsubscribe(NotificationTopic topic, String id, OneControlConfiguration configuration) throws UnsubscribeException {
    NotificationProducer port = createPort(configuration);

    Holder<Header> header = buildUnsubscribeHeader(configuration);
    UnsubscribeRequest body = createUnsubscribeRequest(topic, id);

    return port.unsubscribe(header, body);
  }

  private UnsubscribeRequest createUnsubscribeRequest(NotificationTopic topic, String id) {
    return new ObjectFactory().createUnsubscribeRequest()
      .withSubscriptionID(id)
      .withTopic(topic.name().toLowerCase());
  }

  private SubscribeRequest createSubscribeRequest(NotificationTopic topic, String consumerErp) {
    return new ObjectFactory().createSubscribeRequest()
      .withConsumerEpr(consumerErp)
      .withTopic(topic.name().toLowerCase());
  }

  private NotificationProducer createPort(OneControlConfiguration configuration) {
    NotificationProducer port = new NotificationProducerHttp(this.getClass().getResource(WSDL_LOCATION)).getNotificationProducerSoapHttp();
    BindingProvider bindingProvider = (BindingProvider) port;
    bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, configuration.getNotificationProducerEndpoint());
    bindingProvider.getRequestContext().put(JAXWSProperties.CONNECT_TIMEOUT, connectTimeout);
    bindingProvider.getRequestContext().put(JAXWSProperties.REQUEST_TIMEOUT, requestTimeout);

    return port;
  }

}