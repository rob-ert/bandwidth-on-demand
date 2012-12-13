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

import nl.surfnet.bod.nbi.mtosi.MtosiNotificationHolder;

@Service("mtosiNotificationCenterWs")
@WebService(endpointInterface = "org.tmforum.mtop.fmw.wsdl.notc.v1_0.NotificationConsumer")
public class MtosiNotificationCenterWs implements NotificationConsumer {

  @VisibleForTesting
  public static final String DEFAULT_ADDRESS = "http://localhost:8089/bod/mtosi/fmw/notificationconsumer";
  
  private final Logger log = LoggerFactory.getLogger(MtosiNotificationCenterWs.class);
  private final LinkedBlockingDeque<MtosiNotificationHolder> queue = new LinkedBlockingDeque<>();

  @Override
  public void notify(final Header header, final Notify body) {
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
