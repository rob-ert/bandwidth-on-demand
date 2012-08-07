/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
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
import org.springframework.stereotype.Service;
import org.tmforum.mtop.fmw.wsdl.notp.v1_0.NotificationProducer;
import org.tmforum.mtop.fmw.wsdl.notp.v1_0.NotificationProducerHttp;
import org.tmforum.mtop.fmw.wsdl.notp.v1_0.SubscribeException;
import org.tmforum.mtop.fmw.xsd.hdr.v1.CommunicationPatternType;
import org.tmforum.mtop.fmw.xsd.hdr.v1.CommunicationStyleType;
import org.tmforum.mtop.fmw.xsd.hdr.v1.Header;
import org.tmforum.mtop.fmw.xsd.hdr.v1.MessageTypeType;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.SubscribeRequest;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.SubscribeResponse;

//@Service("mtosiEventLiveClient")
public class MtosiEventLiveClient {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private NotificationProducerHttp notificationConsumerHttp;

  private final String eventRetrievalUrl = "http://localhost:9006/mtosi/fmw/NotificationProducer";

  private boolean isInited = false;

  public MtosiEventLiveClient() {
    init();
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
        notificationConsumerHttp = new NotificationProducerHttp(new URL(eventRetrievalUrl), new QName(
            "http://www.tmforum.org/mtop/fmw/wsdl/notp/v1-0", "NotificationProducerHttp"));

        final Map<String, Object> requestContext = ((BindingProvider) notificationConsumerHttp
            .getPort(NotificationProducer.class)).getRequestContext();

        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, eventRetrievalUrl);
        isInited = true;
      }
      catch (MalformedURLException e) {
        log.error("Error: ", e);
      }
    }

  }

  public void subscribe() {
    final SubscribeRequest subscribeRequest = new org.tmforum.mtop.fmw.xsd.notmsg.v1.ObjectFactory()
        .createSubscribeRequest();
    
    subscribeRequest.setConsumerEpr("http://145.96.0.119:9009/mtosi/fmw/NotificationConsumer");
    subscribeRequest.setTopic("fault");
    
    try {
      final SubscribeResponse subscribe = notificationConsumerHttp.getNotificationProducerSoapHttp().subscribe(getRequestHeaders(), subscribeRequest);
      log.info("Subscription id {}", subscribe.getSubscriptionID());
    }
    catch (SubscribeException e) {
      log.error("Error: ", e);
    }
  }

  private Holder<Header> getRequestHeaders() {
    final Header header = new Header();
    header.setDestinationURI(eventRetrievalUrl);
    header.setCommunicationPattern(CommunicationPatternType.SIMPLE_RESPONSE);
    header.setCommunicationStyle(CommunicationStyleType.RPC);
    header.setActivityName("subscribe");
    header.setMsgName("subscribeRequest");
     header.setSenderURI("http://localhost:9009");
    header.setMsgType(MessageTypeType.REQUEST);
    return new Holder<Header>(header);
  }
}
