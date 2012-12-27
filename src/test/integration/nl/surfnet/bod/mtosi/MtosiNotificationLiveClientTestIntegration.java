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

import static nl.surfnet.bod.mtosi.MtosiNotificationCenterWs.DEFAULT_ADDRESS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;

import nl.surfnet.bod.nbi.mtosi.MtosiNotificationHolder;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.tmforum.mtop.fmw.wsdl.notc.v1_0.NotificationConsumer;
import org.tmforum.mtop.fmw.wsdl.notc.v1_0.NotificationConsumerHttp;
import org.tmforum.mtop.fmw.xsd.hdr.v1.Header;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.Notify;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.Notify.Message;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.UnsubscribeResponse;
import org.tmforum.mtop.nra.xsd.alm.v1.AlarmType;
import org.tmforum.mtop.sb.xsd.soc.v1.ServiceObjectCreationType;

public class MtosiNotificationLiveClientTestIntegration {

  private final Properties properties = new Properties();

  private MtosiNotificationLiveClient mtosiNotificationLiveClient;

  @Before
  public void setup() throws IOException {
    try {
    properties.load(new FileInputStream("src/main/resources/env-properties/bod-test.properties"));
    mtosiNotificationLiveClient = new MtosiNotificationLiveClient(
        properties.getProperty("nbi.mtosi.notification.retrieval.endpoint"),
        properties.getProperty("nbi.mtosi.notification.sender.uri"));
    } catch (IOException e) {
      System.err.println("Ignoring test because 'env-properties/bod-test.properties' not found.");
      Assume.assumeNoException(e);
    }
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

    // Create and configure notification client
    final URL url = new ClassPathResource(
        "/mtosi/2.1/DDPs/Framework/IIS/wsdl/NotificationConsumer/NotificationConsumerHttp.wsdl").getURL();
    final NotificationConsumer port = new NotificationConsumerHttp(url, new QName(
        "http://www.tmforum.org/mtop/fmw/wsdl/notc/v1-0", "NotificationConsumerHttp"))
        .getNotificationConsumerSoapHttp();
    final Map<String, Object> requestContext = ((BindingProvider) port).getRequestContext();
    requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, DEFAULT_ADDRESS);


    // Create notification header
    final Header header = new Header();
    final String activity = "notify";
    header.setActivityName(activity);

    // Create notification body
    final Notify body = new Notify();
    body.setTopic("fault");

    // Create an alarm notification message which is part of the body
    final AlarmType alarm = new org.tmforum.mtop.nra.xsd.alm.v1.ObjectFactory().createAlarmType();
    alarm.setNotificationId(UUID.randomUUID().toString());

    final ServiceObjectCreationType serviceObjectCreation = new org.tmforum.mtop.sb.xsd.soc.v1.ObjectFactory().createServiceObjectCreationType();
    serviceObjectCreation.setNotificationId(UUID.randomUUID().toString());

    final Message message = new org.tmforum.mtop.fmw.xsd.notmsg.v1.ObjectFactory().createNotifyMessage();

    message.getCommonEventInformation().add(serviceObjectCreation);
//    message.getCommonEventInformation().add(alarm);
    body.setMessage(message);


    // make sure that there are no old messages
    assertThat(mtosiNotificationCenterWs.getLastMessage(), nullValue());

    // send notification
    port.notify(header, body);

    final MtosiNotificationHolder notification = mtosiNotificationCenterWs.getLastMessage(2, TimeUnit.SECONDS);
    assertThat(notification, notNullValue());
    assertThat(notification.getHeader().getActivityName(), containsString(activity));
//    final CommonEventInformationType commonEventInformationType = notification.getBody().getMessage().getCommonEventInformation().get(0);
    assertThat(mtosiNotificationCenterWs.getLastMessage(), nullValue());
  }
}
