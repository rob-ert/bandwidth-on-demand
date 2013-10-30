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

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.net.InetAddresses;

import nl.surfnet.bod.nbi.onecontrol.NotificationProducerClient.NotificationTopic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tmforum.mtop.fmw.wsdl.notp.v1_0.UnsubscribeException;

@Profile({ "onecontrol", "onecontrol-offline" })
@Component
public class NotificationSubscriber {

  private Logger logger = LoggerFactory.getLogger(NotificationSubscriber.class);

  private String serviceTopicSubscribeId;
  private String faultTopicSubscribeId;

  private int flatlineTolerance; // in seconds

  private NotificationProducerClient notificationClient;

  private NotificationConsumerHttp notificationConsumerHttp;

  private String endPointAddress;

  @Autowired
  public NotificationSubscriber(@Value("${nbi.onecontrol.notification.flatline.tolerance}") int flatlineTolerance,
                                @Value("${nbi.onecontrol.notification.consumer.endpoint}") String endPointAddress,
                                NotificationProducerClient notificationClient, NotificationConsumerHttp notificationConsumerHttp) {
    this.flatlineTolerance = flatlineTolerance;
    this.endPointAddress = endPointAddress;
    this.notificationClient = notificationClient;
    this.notificationConsumerHttp = notificationConsumerHttp;
  }

  @PostConstruct
  public void subscribe() {
    try {
      String endPoint = getEndPoint();

      logger.info("Using '{}' as our callback endpoint", endPoint);

      serviceTopicSubscribeId = notificationClient.subscribe(NotificationTopic.SERVICE, endPoint);
      faultTopicSubscribeId = notificationClient.subscribe(NotificationTopic.FAULT, endPoint);
    } catch (Exception e) {
      throw new IllegalArgumentException("Could not subscribe to MTOSI/OneControl notifications", e);
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

  public boolean isHealthy() {
    // the last time a notification was received may not be more than {tolerance} seconds ago
    return notificationConsumerHttp.getTimeOfLastHeartbeat().plusSeconds(flatlineTolerance).isAfterNow();
  }

  /**
   * Reconnects when the heartbeat is lost
   */
  @Scheduled(initialDelay = 30000, fixedDelayString = "${nbi.onecontrol.notification.monitor.interval}")
  public void monitor() {
    if (isHealthy()) {
      logger.debug("Notification subscription OK, nothing to do.");
    } else {
      logger.info("Subscriptions lost, try to re-subscribe");
      unsubscribe();
      subscribe();
    }
  }

  private String getEndPoint() {
    if (Strings.isNullOrEmpty(endPointAddress)) {
      Optional<InetAddress> address = getOneControlInetAddress();
      if (!address.isPresent()) {
        logger.warn("Could not determine MTOSI/OneControl consumer end point");
        return getEndPoint(InetAddresses.forString("127.0.0.1"));
      }
      return getEndPoint(address.get());
    }

    return endPointAddress;
  }

  private String getEndPoint(InetAddress address) {
    return String.format("http://%s:8082/bod/mtosi/fmw/NotificationConsumer", address.getHostAddress());
  }

  private Optional<InetAddress> getOneControlInetAddress() {
    try {
      Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
      while (networkInterfaces.hasMoreElements()) {
        NetworkInterface networkInterface = networkInterfaces.nextElement();
        Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
        while (inetAddresses.hasMoreElements()) {
          InetAddress inetAddress = inetAddresses.nextElement();
          if (inetAddress.getHostAddress().startsWith("145.145.73")) {
            return Optional.of(inetAddress);
          }
        }
      }
    } catch (IOException e) {
      logger.warn("Could not determine OneControl InetAddress", e);
    }

    return Optional.absent();
  }

  private void unsubscribe(NotificationTopic topic, String subscriptionId) {
    try {
      notificationClient.unsubscribe(topic, subscriptionId);
      logger.info("Successfully un-subscribed from {} topic with id {}", topic, subscriptionId);
    } catch (UnsubscribeException e) {
      logger.warn("Un-subscribe to {} topic with id {} has failed: {}", topic, subscriptionId, e);
    }
  }

}
