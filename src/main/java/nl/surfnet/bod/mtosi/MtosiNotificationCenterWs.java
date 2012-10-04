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

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.tmforum.mtop.fmw.wsdl.notc.v1_0.NotificationConsumer;
import org.tmforum.mtop.fmw.xsd.hdr.v1.Header;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.Notify;

import com.google.common.annotations.VisibleForTesting;

import nl.surfnet.bod.domain.MtosiNotificationHolder;

@Service("mtosiNotificationCenterWs")
@WebService(endpointInterface = "org.tmforum.mtop.fmw.wsdl.notc.v1_0.NotificationConsumer")
public class MtosiNotificationCenterWs implements NotificationConsumer {

  @VisibleForTesting
  public static final String DEFAULT_ADDRESS = "http://localhost:8089/bod/mtosi/fmw/notificationconsumer";
  
  private final Logger log = LoggerFactory.getLogger(MtosiNotificationCenterWs.class);
  private final LinkedBlockingDeque<MtosiNotificationHolder> queue = new LinkedBlockingDeque<>();

  @Override
  public void notify(final Header header, final Notify body) {
    log.info("Received: {}, {}", header, body);
    log.info("Activity name: {}", header.getActivityName());
    log.info("Topic: {}", body.getTopic());
    queue.add(new MtosiNotificationHolder(header, body));
  }

  public MtosiNotificationHolder getFirstMessage(long timeout, final TimeUnit timeUnit) {
    try {
      return queue.pollFirst(timeout, timeUnit);
    }
    catch (InterruptedException e) {
      return null;
    }
  }

  public MtosiNotificationHolder getLastMessage(long timeout, final TimeUnit timeUnit) {
    try {
      return queue.pollLast(timeout, timeUnit);
    }
    catch (InterruptedException e) {
      return null;
    }
  }

  public MtosiNotificationHolder getFirstMessage() {
    try {
      return queue.pollFirst(0, TimeUnit.SECONDS);
    }
    catch (InterruptedException e) {
      return null;
    }
  }

  public MtosiNotificationHolder getLastMessage() {
    try {
      return queue.pollLast(0, TimeUnit.SECONDS);
    }
    catch (InterruptedException e) {
      return null;
    }
  }

  static {
//    System.setProperty("com.sun.xml.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace", "false");
//    System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
//    System.setProperty("com.sun.xml.ws.util.pipe.StandaloneTubeAssembler.dump", "true");
//    System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
  }

  public static void main(String[] args) {
    Endpoint.publish(DEFAULT_ADDRESS, new MtosiNotificationCenterWs());
  }

}
