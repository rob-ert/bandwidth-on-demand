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
import org.springframework.context.i18n.LocaleContextHolder;

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

    final boolean deleteAllowed = reservation.getStatus().isDeleteAllowed();
    String deleteTooltip = null;
    if (!deleteAllowed) {
      deleteTooltip = messageSource.getMessage("reservation_state_transition_not_allowed", new Object[] {}, LocaleContextHolder.getLocale());
    }

    return new JsonMessageEvent(reservation.getVirtualResourceGroup().getSurfconextGroupId(), new JsonEvent(message,
        reservation.getId(), reservation.getStatus().name(), deleteAllowed, deleteTooltip));
  }

  private static class JsonEvent {
    private final String message;
    private final Long id;
    private final String status;
    private final Boolean deletable;
    private final String deleteTooltip;

    public JsonEvent(String message, Long id, String status, boolean deletable, String deleteTooltip) {
      this.message = message;
      this.id = id;
      this.status = status;
      this.deletable = deletable;
      this.deleteTooltip = deleteTooltip;
    }

    @SuppressWarnings("unused")
    public String getDeleteTooltip() {
      return deleteTooltip;
    }

    @SuppressWarnings("unused")
    public Boolean getDeletable() {
      return deletable;
    }

    @SuppressWarnings("unused")
    public String getMessage() {
      return message;
    }

    @SuppressWarnings("unused")
    public Long getId() {
      return id;
    }

    @SuppressWarnings("unused")
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
