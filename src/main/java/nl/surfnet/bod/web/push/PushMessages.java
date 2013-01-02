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

    if (ReservationStatus.ERROR_STATES.contains(reservation.getStatus()) && reservation.getFailedReason() != null) {
      message += String.format(" Failed because '%s'.", reservation.getFailedReason());
    }

    final boolean deleteAllowed = reservation.getStatus().isDeleteAllowed();
    String deleteTooltip = null;
    if (!deleteAllowed) {
      deleteTooltip = messageSource.getMessage(
        "reservation_state_transition_not_allowed", new Object[] {}, LocaleContextHolder.getLocale());
    }

    return new JsonMessageEvent(reservation.getVirtualResourceGroup().getAdminGroup(), new JsonEvent(message,
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
