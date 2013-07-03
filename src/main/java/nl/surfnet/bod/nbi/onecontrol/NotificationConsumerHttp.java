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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.bind.JAXBElement;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import nl.surfnet.bod.nbi.onecontrol.MtosiNotificationClient.NotificationTopic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.tmforum.mtop.fmw.wsdl.notc.v1_0.NotificationConsumer;
import org.tmforum.mtop.fmw.wsdl.notp.v1_0.SubscribeException;
import org.tmforum.mtop.fmw.wsdl.notp.v1_0.UnsubscribeException;
import org.tmforum.mtop.fmw.xsd.cei.v1.CommonEventInformationType;
import org.tmforum.mtop.fmw.xsd.hbt.v1.HeartbeatType;
import org.tmforum.mtop.fmw.xsd.hdr.v1.Header;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.Notify;
import org.tmforum.mtop.nra.xsd.alm.v1.AlarmType;
import org.tmforum.mtop.sb.xsd.soc.v1.ServiceObjectCreationType;
import org.tmforum.mtop.sb.xsd.sodel.v1.ServiceObjectDeletionType;

@Profile("onecontrol")
@Component
@WebService(
    serviceName = "NotificationConsumerHttp", endpointInterface = "org.tmforum.mtop.fmw.wsdl.notc.v1_0.NotificationConsumer",
    portName = "NotificationConsumerSoapHttp", targetNamespace = "http://www.tmforum.org/mtop/fmw/wsdl/notc/v1-0")
public class NotificationConsumerHttp implements NotificationConsumer {

  private final Logger log = LoggerFactory.getLogger(NotificationConsumerHttp.class);

  private final List<AlarmType> alarms = new ArrayList<>();
  private final List<HeartbeatType> heartbeats = new ArrayList<>();
  private final List<CommonEventInformationType> events = new ArrayList<>();
  private final List<ServiceObjectCreationType> serviceObjectCreations = new ArrayList<>();
  private final List<ServiceObjectDeletionType> serviceObjectDeletions = new ArrayList<>();

  private String serviceTopicSubscribeId;
  private String faultTopicSubscribeId;

  @Resource private MtosiNotificationClient notificationClient;
  @Resource private Environment environment;

  @PostConstruct
  public void subscribe() {
    // FIXME hard coded endpoint
    try {
      serviceTopicSubscribeId = notificationClient.subscribe(NotificationTopic.SERVICE, "http://145.145.73.17:8082/bod/onecontrol/fmw/NotificationConsumer");
      faultTopicSubscribeId = notificationClient.subscribe(NotificationTopic.FAULT, "http://145.145.73.17:8082/bod/onecontrol/fmw/NotificationConsumer");
    } catch (SubscribeException e) {
      throw new AssertionError("Could not subscribe to MTOSI/OneControl notifications");
    }
  }

  @PreDestroy
  public void unsubscribe() {
    if (!Strings.isNullOrEmpty(serviceTopicSubscribeId)) {
      unsubscribe(NotificationTopic.SERVICE, serviceTopicSubscribeId);
    }
    if (!Strings.isNullOrEmpty(faultTopicSubscribeId)) {
      unsubscribe(NotificationTopic.FAULT, faultTopicSubscribeId);
    }
  }

  private void unsubscribe(NotificationTopic topic, String subscriptionId) {
    try {
      notificationClient.unsubscribe(topic, subscriptionId);
      log.info("Succesfully unsubscribed from {} topic with id {}", topic, subscriptionId);
    } catch (UnsubscribeException e) {
      log.warn("Unsubscribe to {} topic with id {} has failed: {}", topic, subscriptionId, e);
    }
  }

  @Override
  public void notify(Header header, Notify body) {
    log.info("Received a notification: {}, {}", body.getTopic(), body.getMessage());

    List<JAXBElement<? extends CommonEventInformationType>> eventInformations = body.getMessage().getCommonEventInformation();

    for (JAXBElement<? extends CommonEventInformationType> jaxbElement : eventInformations) {
      CommonEventInformationType event = jaxbElement.getValue();
      if (event instanceof HeartbeatType) {
        heartbeats.add((HeartbeatType) jaxbElement.getValue());
      } else if (event instanceof AlarmType) {
        alarms.add((AlarmType) jaxbElement.getValue());
      } else if (event instanceof ServiceObjectCreationType) {
        serviceObjectCreations.add((ServiceObjectCreationType) event);
      } else if (event instanceof ServiceObjectDeletionType) {
        serviceObjectDeletions.add((ServiceObjectDeletionType) event);
      } else {
        events.add(event);
        log.warn("Got an unsupported event type: " + event.getClass().getSimpleName());
      }
    }
  }

  public List<AlarmType> getAlarms() {
    return ImmutableList.copyOf(alarms);
  }

  public List<HeartbeatType> getHeartbeats() {
    return ImmutableList.copyOf(heartbeats);
  }

  public List<CommonEventInformationType> getEvents() {
    return ImmutableList.copyOf(events);
  }

  public List<ServiceObjectCreationType> getServiceObjectCreations() {
    return ImmutableList.copyOf(serviceObjectCreations);
  }
}