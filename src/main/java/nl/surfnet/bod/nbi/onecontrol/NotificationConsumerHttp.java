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

import java.util.Optional;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Queues;
import java.util.List;
import java.util.Queue;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.domain.UpdatedReservationStatus;
import nl.surfnet.bod.service.ReservationService;
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
import org.tmforum.mtop.sb.xsd.svc.v1.ResourceFacingServiceType;

@Profile({ "onecontrol", "onecontrol-offline" })
@Component
@WebService(
    serviceName = "NotificationConsumerHttp", endpointInterface = "org.tmforum.mtop.fmw.wsdl.notc.v1_0.NotificationConsumer",
    portName = "NotificationConsumerSoapHttp", targetNamespace = "http://www.tmforum.org/mtop/fmw/wsdl/notc/v1-0")
//@SchemaValidation // OneControl notifications are not valid against the MTOSI schemas
public class NotificationConsumerHttp implements NotificationConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(NotificationConsumerHttp.class);

  public static final int NOTIFICATION_LIMIT = 20;

  private final Queue<AlarmType> alarms = Queues.synchronizedQueue(EvictingQueue.<AlarmType>create(NOTIFICATION_LIMIT));
  private final Queue<CommonEventInformationType> events = Queues.synchronizedQueue(EvictingQueue.<CommonEventInformationType>create(NOTIFICATION_LIMIT));
  private final Queue<ServiceObjectCreationType> serviceObjectCreations = Queues.synchronizedQueue(EvictingQueue.<ServiceObjectCreationType>create(NOTIFICATION_LIMIT));
  private final Queue<ServiceObjectDeletionType> serviceObjectDeletions = Queues.synchronizedQueue(EvictingQueue.<ServiceObjectDeletionType>create(NOTIFICATION_LIMIT));
  private final Queue<ServiceAttributeValueChangeType> serviceAttributeValueChanges = Queues.synchronizedQueue(EvictingQueue.<ServiceAttributeValueChangeType>create(NOTIFICATION_LIMIT));

  @Resource private ReservationsAligner reservationsAligner;
  @Resource private ReservationService reservationService;

  private volatile DateTime lastHeartbeat = DateTime.now();

  @Override
  public void notify(Header header, Notify body) {
    try {

      List<JAXBElement<? extends CommonEventInformationType>> eventInformations = body.getMessage().getCommonEventInformation();

      for (JAXBElement<? extends CommonEventInformationType> jaxbElement : eventInformations) {
        CommonEventInformationType event = jaxbElement.getValue();
        if (event instanceof HeartbeatType) {
          lastHeartbeat = DateTime.now();
          LOGGER.debug("Received heartbeat");
        } else if (event instanceof AlarmType) {
          LOGGER.debug("ALARM  : {}, {}", body.getTopic(), body.getMessage());
          alarms.add((AlarmType) jaxbElement.getValue());
        } else if (event instanceof ServiceObjectCreationType) {
          LOGGER.info("CREATION: {}, {}", body.getTopic(), body.getMessage());
          handleServiceObjectCreation((ServiceObjectCreationType) event);
        } else if (event instanceof ServiceAttributeValueChangeType) {
          LOGGER.info("CHANGE  : {}, {}", body.getTopic(), body.getMessage());
          handleServiceAttributeValueChange((ServiceAttributeValueChangeType) event);
        } else if (event instanceof ServiceObjectDeletionType) {
          LOGGER.info("DELETION: {}, {}", body.getTopic(), body.getMessage());
          handleServiceObjectDeletion((ServiceObjectDeletionType) event);
        } else {
          events.add(event);
          LOGGER.warn("Got an unsupported event type {}", event.getClass().getSimpleName());
        }
      }
    } catch (Exception e) {
      LOGGER.warn("Error processing notification: {}", e, e);
    }
  }

  public DateTime getTimeOfLastHeartbeat() {
    return lastHeartbeat;
  }


  private void handleServiceObjectCreation(ServiceObjectCreationType event) {
    serviceObjectCreations.add(event);
  }

  private Optional<Reservation> maybeFindReservation(Optional<String> reservationId) {
    if (reservationId.isPresent()) {
      return Optional.ofNullable(reservationService.findByReservationId(reservationId.get()));
    } else {
      return Optional.empty();
    }
  }

  private void handleServiceAttributeValueChange(ServiceAttributeValueChangeType valueChange) throws JAXBException {
    serviceAttributeValueChanges.add(valueChange);
    Optional<String> reservationId = MtosiUtils.findRdnValue("RFS", valueChange.getObjectName());
    Optional<ResourceFacingServiceType> rfs = MtosiUtils.findRfs(valueChange);
    Optional<Reservation> reservation = maybeFindReservation(reservationId);

    if (reservation.isPresent() && !rfs.isPresent()) {
      LOGGER.warn("RFS not found in value change event {}", valueChange);
    }
    if (reservation.isPresent() && rfs.isPresent()) {
      Optional<ReservationStatus> newStatus = MtosiUtils.mapToReservationState(rfs.get());
      if (newStatus.isPresent()) {
        UpdatedReservationStatus updatedReservationStatus = UpdatedReservationStatus.forNewStatus(newStatus.get());
        scheduleUpdate(reservationId.get(), updatedReservationStatus);
      }
    }
  }

  private void handleServiceObjectDeletion(ServiceObjectDeletionType deletionEvent) {
    serviceObjectDeletions.add(deletionEvent);
    Optional<String> reservationId = MtosiUtils.findRdnValue("RFS", deletionEvent.getObjectName());
    Optional<Reservation> reservation = maybeFindReservation(reservationId);

    if (reservation.isPresent()) {
      if (reservation.get().getStatus() == ReservationStatus.REQUESTED) {
        scheduleUpdate(reservation.get().getReservationId(), UpdatedReservationStatus.notAccepted("reserve request not accepted by network"));
      } else {
        scheduleUpdate(reservation.get().getReservationId(), UpdatedReservationStatus.forNewStatus(ReservationStatus.SUCCEEDED));
      }
    }
  }

  private void scheduleUpdate(String reservationId, UpdatedReservationStatus updatedReservationStatus) {
    reservationsAligner.add(reservationId, Optional.of(updatedReservationStatus));
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

  public List<ServiceObjectDeletionType> getServiceObjectDeletions() {
    return ImmutableList.copyOf(serviceObjectDeletions);
  }

  public List<ServiceAttributeValueChangeType> getServiceAttributeValueChanges() {
    return ImmutableList.copyOf(serviceAttributeValueChanges);
  }

}