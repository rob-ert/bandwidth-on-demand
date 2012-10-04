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

import static nl.surfnet.bod.mtosi.MtosiNotificationCenterWs.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.tmforum.mtop.fmw.wsdl.notc.v1_0.NotificationConsumer;
import org.tmforum.mtop.fmw.wsdl.notc.v1_0.NotificationConsumerHttp;
import org.tmforum.mtop.fmw.xsd.hdr.v1.Header;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.Notify;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.UnsubscribeResponse;

import nl.surfnet.bod.domain.MtosiNotificationHolder;

public class MtosiNotificationLiveClientTestIntegration {

  private final Properties properties = new Properties();

  private MtosiNotificationLiveClient mtosiNotificationLiveClient;

  @Before
  public void setup() throws IOException {
    properties.load(ClassLoader.class.getResourceAsStream("/bod-default.properties"));
    mtosiNotificationLiveClient = new MtosiNotificationLiveClient(properties.get(
        "mtosi.notification.retrieval.endpoint").toString(), properties.get("mtosi.notification.sender.uri").toString());

  }

  @Ignore
  @Test
  public void subscribeAndUnsubscribe() throws Exception {
    final String topic = "fault";
    final String subscriberId = mtosiNotificationLiveClient.subscribe(topic, DEFAULT_ADDRESS);
    assertThat(subscriberId, notNullValue());
    final UnsubscribeResponse unsubscribeResponse = mtosiNotificationLiveClient.unsubscribe(subscriberId, topic);
    assertThat(unsubscribeResponse, notNullValue());
  }

  @Test
  public void sendNotification() throws Exception {

    // Our notification consumer
    final MtosiNotificationCenterWs mtosiNotificationCenterWs = new MtosiNotificationCenterWs();

    // start it
    Endpoint.publish(DEFAULT_ADDRESS, mtosiNotificationCenterWs);

    final URL url = new ClassPathResource(
        "/mtosi/2.1/DDPs/Framework/IIS/wsdl/NotificationConsumer/NotificationConsumerHttp.wsdl").getURL();
    final NotificationConsumer port = new NotificationConsumerHttp(url, new QName(
        "http://www.tmforum.org/mtop/fmw/wsdl/notc/v1-0", "NotificationConsumerHttp"))
        .getNotificationConsumerSoapHttp();
    final Map<String, Object> requestContext = ((BindingProvider) port).getRequestContext();
    requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, DEFAULT_ADDRESS);
    final Header header = new Header();
    final String activity = "notify";

    header.setActivityName(activity);
    final Notify body = new Notify();
    body.setTopic("fault");

    // make sure that there are no old messages
    assertThat(mtosiNotificationCenterWs.getLastMessage(), nullValue());

    // send notification
    port.notify(header, body);

    final MtosiNotificationHolder notification = mtosiNotificationCenterWs.getLastMessage(2, TimeUnit.SECONDS);
    assertThat(notification, notNullValue());
    assertThat(notification.getHeader().getActivityName(), containsString(activity));
    assertThat(mtosiNotificationCenterWs.getLastMessage(), nullValue());
  }
}
