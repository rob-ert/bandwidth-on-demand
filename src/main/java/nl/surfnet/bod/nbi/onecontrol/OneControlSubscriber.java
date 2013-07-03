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
import javax.annotation.Resource;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

import nl.surfnet.bod.nbi.onecontrol.MtosiNotificationClient.NotificationTopic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.tmforum.mtop.fmw.wsdl.notp.v1_0.UnsubscribeException;

@Profile("onecontrol")
@Component
public class OneControlSubscriber {

  private Logger logger = LoggerFactory.getLogger(OneControlSubscriber.class);

  private String serviceTopicSubscribeId;
  private String faultTopicSubscribeId;

  @Resource private MtosiNotificationClient notificationClient;

  @PostConstruct
  public void subscribe() {
    try {
      Optional<InetAddress> address = getOneControlIntAddress();

      if (!address.isPresent()) {
        throw new AssertionError("Could not subscribe to MTOSI/OneControl notifications");
      }

      String endpoint = getEndPoint(address.get());

      logger.info("Using {} as our callback endpoint", endpoint);

      serviceTopicSubscribeId = notificationClient.subscribe(NotificationTopic.SERVICE, endpoint);
      faultTopicSubscribeId = notificationClient.subscribe(NotificationTopic.FAULT, endpoint);
    } catch (Exception e) {
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

  private String getEndPoint(InetAddress address) {
    return String.format("http://%s:8082/bod/onecontrol/fmw/NotificationConsumer", address.getHostAddress());
  }

  private Optional<InetAddress> getOneControlIntAddress() {
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
      logger.info("Could not determine OneControl Inet Address", e);
    }

    return Optional.absent();

  }

  private void unsubscribe(NotificationTopic topic, String subscriptionId) {
    try {
      notificationClient.unsubscribe(topic, subscriptionId);
      logger.info("Succesfully unsubscribed from {} topic with id {}", topic, subscriptionId);
    } catch (UnsubscribeException e) {
      logger.warn("Unsubscribe to {} topic with id {} has failed: {}", topic, subscriptionId, e);
    }
  }

}
