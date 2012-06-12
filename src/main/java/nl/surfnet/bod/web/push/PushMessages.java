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
package nl.surfnet.bod.web.push;

import java.io.IOException;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.service.ReservationStatusChangeEvent;
import nl.surfnet.bod.web.WebUtils;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.context.MessageSource;

public final class PushMessages {

  private PushMessages() {
  }

  public static PushMessage createMessage(ReservationStatusChangeEvent reservationStatusChangeEvent,
      MessageSource messageSource) {

    Reservation reservation = reservationStatusChangeEvent.getReservation();

    String message = WebUtils.getMessageWithBoldArguments(messageSource, "info_reservation_statuschanged",
        reservation.getName(), reservationStatusChangeEvent.getOldStatus().name(), reservation.getStatus().name());

    if (reservation.getStatus().equals(ReservationStatus.FAILED) && reservation.getFailedReason() != null) {
      message += String.format(" Failed because '%s'.", reservation.getFailedReason());
    }

    return new JsonMessageEvent(reservation.getVirtualResourceGroup().getSurfconextGroupId(), new JsonEvent(message,
        reservation.getId(), reservation.getStatus().name()));
  }

  private static class JsonEvent {
    private final String message;
    private final Long id;
    private final String status;

    public JsonEvent(String message, Long id, String status) {
      this.message = message;
      this.id = id;
      this.status = status;
    }

    public String getMessage() {
      return message;
    }

    public Long getId() {
      return id;
    }

    public String getStatus() {
      return status;
    }
  }

  private static final class JsonMessageEvent implements PushMessage {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private final String groupId;
    private final Object toJsonObject;

    public JsonMessageEvent(String groupId, Object toJsonObject) {
      this.groupId = groupId;
      this.toJsonObject = toJsonObject;
    }

    @Override
    public String getGroupId() {
      return groupId;
    }

    @Override
    public String getMessage() {
      try {
        return JSON_MAPPER.writeValueAsString(toJsonObject);
      }
      catch (IOException e) {
        return "{}";
      }
    }

  }
}
