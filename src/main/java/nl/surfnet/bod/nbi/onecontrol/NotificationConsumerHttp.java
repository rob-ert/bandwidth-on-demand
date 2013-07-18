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
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.tmforum.mtop.fmw.wsdl.notc.v1_0.NotificationConsumer;
import org.tmforum.mtop.fmw.xsd.cei.v1.CommonEventInformationType;
import org.tmforum.mtop.fmw.xsd.hbt.v1.HeartbeatType;
import org.tmforum.mtop.fmw.xsd.hdr.v1.Header;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.Notify;
import org.tmforum.mtop.nra.xsd.alm.v1.AlarmType;
import org.tmforum.mtop.sb.xsd.savc.v1.ServiceAttributeValueChangeType;
import org.tmforum.mtop.sb.xsd.soc.v1.ServiceObjectCreationType;
import org.tmforum.mtop.sb.xsd.sodel.v1.ServiceObjectDeletionType;

@Profile("onecontrol")
@Component
@WebService(
    serviceName = "NotificationConsumerHttp", endpointInterface = "org.tmforum.mtop.fmw.wsdl.notc.v1_0.NotificationConsumer",
    portName = "NotificationConsumerSoapHttp", targetNamespace = "http://www.tmforum.org/mtop/fmw/wsdl/notc/v1-0")
public class NotificationConsumerHttp implements NotificationConsumer {

  private final Logger log = LoggerFactory.getLogger(NotificationConsumerHttp.class);

  private final List<AlarmType> alarms = Collections.synchronizedList(new ArrayList<AlarmType>());
  private final List<CommonEventInformationType> events = Collections.synchronizedList(new ArrayList<CommonEventInformationType>());
  private final List<ServiceObjectCreationType> serviceObjectCreations = Collections.synchronizedList(new ArrayList<ServiceObjectCreationType>());
  private final List<ServiceObjectDeletionType> serviceObjectDeletions = Collections.synchronizedList(new ArrayList<ServiceObjectDeletionType>());

  @Resource
  private ReservationsAligner reservationsAligner;

  private DateTime lastHeartbeat = DateTime.now();

  @Override
  public void notify(Header header, Notify body) {
    try {
      log.info("Received a notification: {}, {}", body.getTopic(), body.getMessage());

      List<JAXBElement<? extends CommonEventInformationType>> eventInformations = body.getMessage().getCommonEventInformation();

      for (JAXBElement<? extends CommonEventInformationType> jaxbElement : eventInformations) {
        CommonEventInformationType event = jaxbElement.getValue();
        if (event instanceof HeartbeatType) {
          lastHeartbeat = DateTime.now();
          log.debug("Received heartbeat");
        } else if (event instanceof AlarmType) {
          alarms.add((AlarmType) jaxbElement.getValue());
        } else if (event instanceof ServiceObjectCreationType) {
          handleServiceObjectCreation((ServiceObjectCreationType) event);
        } else if (event instanceof ServiceAttributeValueChangeType) { // connection state changes: active/inactive
          handleServiceAttributeValueChange((ServiceAttributeValueChangeType) event);
        } else if (event instanceof ServiceObjectDeletionType) {
          handleServiceObjectDeletion((ServiceObjectDeletionType) event);
        } else {
          events.add(event);
          log.warn("Got an unsupported event type: " + event.getClass().getSimpleName());
        }
      }
    } catch (Exception e) {
      log.warn("Error processing notification: {}", e, e);
    }
  }

  public DateTime getTimeOfLastHeartbeat() {
    return lastHeartbeat;
  }


  private void handleServiceObjectCreation(ServiceObjectCreationType event) {
    serviceObjectCreations.add(event);
    Optional<String> reservationId = MtosiUtils.findRdnValue("RFS", event.getObjectName());
    scheduleUpdate(reservationId);
  }

  private void handleServiceAttributeValueChange(ServiceAttributeValueChangeType serviceAttributeValueChange) throws JAXBException {
    Optional<String> reservationId = MtosiUtils.findRdnValue("RFS", serviceAttributeValueChange.getObjectName());
    scheduleUpdate(reservationId);
  }

  private void handleServiceObjectDeletion(ServiceObjectDeletionType deletionEvent) {
    serviceObjectDeletions.add(deletionEvent);
    Optional<String> reservationId = MtosiUtils.findRdnValue("RFS", deletionEvent.getObjectName());
    scheduleUpdate(reservationId);
  }

  private void scheduleUpdate(Optional<String> reservationId) {
    if (reservationId.isPresent()) {
      reservationsAligner.add(reservationId.get());
    }
  }

  public List<AlarmType> getAlarms() {
    return ImmutableList.copyOf(alarms);
  }

  public List<CommonEventInformationType> getEvents() {
    return ImmutableList.copyOf(events);
  }

  public List<ServiceObjectCreationType> getServiceObjectCreations() {
    return ImmutableList.copyOf(serviceObjectCreations);
  }
}