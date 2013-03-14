/**
 * Copyright (c) 2012, SURFnet BV
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

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebService;
import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.tmforum.mtop.fmw.wsdl.notc.v1_0.NotificationConsumer;
import org.tmforum.mtop.fmw.xsd.cei.v1.CommonEventInformationType;
import org.tmforum.mtop.fmw.xsd.hbt.v1.HeartbeatType;
import org.tmforum.mtop.fmw.xsd.hdr.v1.Header;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.Notify;
import org.tmforum.mtop.nra.xsd.alm.v1.AlarmType;

import com.google.common.collect.ImmutableList;

@Service("NotificationConsumerHttp")
@WebService(
    serviceName = "NotificationConsumerHttp", endpointInterface = "org.tmforum.mtop.fmw.wsdl.notc.v1_0.NotificationConsumer",
    portName = "NotificationConsumerSoapHttp", targetNamespace = "http://www.tmforum.org/mtop/fmw/wsdl/notc/v1-0")
public class NotificationConsumerHttp implements NotificationConsumer {

  private final Logger log = LoggerFactory.getLogger(NotificationConsumerHttp.class);

  // FIXME [AvD] concurrency....
  private final List<AlarmType> alarms = new ArrayList<>();
  private final List<HeartbeatType> heartbeats = new ArrayList<>();
//  private final List<AttributeValueChangeType> attributeValueChangeEvents = new ArrayList<>();
//  private final List<ObjectCreationType> objectCreationEvents = new ArrayList<>();
//  private final List<ObjectDeletionType> objectDeletionEvents = new ArrayList<>();
//  private final List<StateChangeType> stateChangeEvents = new ArrayList<>();

  @Override
  public void notify(final Header header, final Notify body) {
    log.info("Notification topic: {}", body.getTopic());

    List<JAXBElement<? extends CommonEventInformationType>> eventInformations = body.getMessage().getCommonEventInformation();

    for (JAXBElement<? extends CommonEventInformationType> jaxbElement : eventInformations) {
      log.info("Type of Notification: {}", jaxbElement.getDeclaredType().getSimpleName());
      if (jaxbElement.getDeclaredType().equals(HeartbeatType.class)) {
        heartbeats.add((HeartbeatType) jaxbElement.getValue());
      }
      else if (jaxbElement.getDeclaredType().equals(AlarmType.class)) {
        alarms.add((AlarmType) jaxbElement.getValue());
      }
      else {
        log.warn("Got an unspurred event " + jaxbElement.getDeclaredType());
      }
    }
  }

  public List<AlarmType> getAlarms() {
    return ImmutableList.copyOf(alarms);
  }

  public List<HeartbeatType> getHeartbeats() {
    return ImmutableList.copyOf(heartbeats);
  }

//  public List<AttributeValueChangeType> getAttributeValueChangeEvents() {
//    return ImmutableList.copyOf(attributeValueChangeEvents);
//  }
//
//  public List<ObjectCreationType> getObjectCreationEvents() {
//    return ImmutableList.copyOf(objectCreationEvents);
//  }
//
//  public List<ObjectDeletionType> getObjectDeletionEvents() {
//    return ImmutableList.copyOf(objectDeletionEvents);
//  }
//
//  public List<StateChangeType> getStateChangeEvents() {
//    return ImmutableList.copyOf(stateChangeEvents);
//  }

}