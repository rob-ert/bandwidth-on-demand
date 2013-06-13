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
package nl.surfnet.bod.nbi.mtosi;

import static nl.surfnet.bod.nbi.mtosi.MtosiNotificationLiveClient.NotificationTopic.FAULT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;

import nl.surfnet.bod.nbi.mtosi.MtosiNotificationLiveClient.NotificationTopic;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.tmforum.mtop.fmw.wsdl.notc.v1_0.NotificationConsumer;
import org.tmforum.mtop.fmw.wsdl.notp.v1_0.SubscribeException;
import org.tmforum.mtop.fmw.xsd.hdr.v1.Header;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.Notify;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.Notify.Message;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.UnsubscribeResponse;
import org.tmforum.mtop.nra.xsd.alm.v1.AlarmType;

public class MtosiNotificationLiveClientTestIntegration {

  private MtosiNotificationLiveClient mtosiNotificationLiveClient;

  @Before
  public void setup() throws IOException {
    Endpoint.publish("http://localhost:9999/ws/hello", new NotificationConsumerHttp());
  }

  @Test
  @Ignore("not ready yet..")
  public void subscribeAndUnsubscribe() throws Exception {
    String subscriberId = mtosiNotificationLiveClient.subscribe(NotificationTopic.FAULT,
        "http://localhost:9999/ws/hello");

    assertThat(subscriberId, notNullValue());

    UnsubscribeResponse unsubscribeResponse = mtosiNotificationLiveClient.unsubscribe(NotificationTopic.FAULT,
        subscriberId);

    assertThat(unsubscribeResponse, notNullValue());
  }

  @Test
  @Ignore("not ready yet because we don't receive any heartbeats")
  public void retreiveNotificationsHeartbeat() throws SubscribeException, InterruptedException {
    // String subscriberId = mtosiNotificationLiveClient.subscribe(FAULT,
    // "https://bod.test.dlp.surfnet.nl/mtosi/fmw/NotificationConsumer");
    String subscriberId = mtosiNotificationLiveClient.subscribe(FAULT, "http://localhost:9999/ws/hello");
    System.err.println("Got a " + subscriberId);
  }

  @Test
  @Ignore("Made it locally runnable again")
  public void sendNotification() throws Exception {

    // Create and configure notification client
    final URL url = new ClassPathResource(
        "/mtosi/2.1/DDPs/Framework/IIS/wsdl/NotificationConsumer/NotificationConsumerHttp.wsdl").getURL();
    final NotificationConsumer port = new org.tmforum.mtop.fmw.wsdl.notc.v1_0.NotificationConsumerHttp(url, new QName(
        "http://www.tmforum.org/mtop/fmw/wsdl/notc/v1-0", "NotificationConsumerHttp"))
        .getNotificationConsumerSoapHttp();

    final Map<String, Object> requestContext = ((BindingProvider) port).getRequestContext();
    requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:9999/ws/hello");

    // Create notification header
    final Header header = new Header();
    final String activity = "notify";
    header.setActivityName(activity);

    // Create notification body
    final Notify body = new Notify();
    body.setTopic("fault");

    // Create an alarm notification message which is part of the body
    AlarmType alarm = new org.tmforum.mtop.nra.xsd.alm.v1.ObjectFactory().createAlarmType();
    alarm.setAdditionalText("Some extra info");
    alarm.setRootCauseAlarmIndication(true);
    alarm.setNotificationId("0123456789");

    Message message = new org.tmforum.mtop.fmw.xsd.notmsg.v1.ObjectFactory().createNotifyMessage();

//    message.getCommonEventInformation().add(alarm);
    body.setMessage(message);

    // send notification
    port.notify(header, body);

  }

  static {
    System.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, "true");
    System.setProperty("com.sun.xml.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace", "false");
    System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
    System.setProperty("com.sun.xml.ws.util.pipe.StandaloneTubeAssembler.dump", "true");
    System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
  }
}
