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
package nl.surfnet.bod.web.push;

import java.io.IOException;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.service.ReservationStatusChangeEvent;
import nl.surfnet.bod.web.base.MessageRetriever;

import org.codehaus.jackson.map.ObjectMapper;

public final class PushMessages {

  private PushMessages() {
  }

  public static PushMessage createMessage(MessageRetriever messageRetriever,
      ReservationStatusChangeEvent reservationStatusChangeEvent) {

    Reservation reservation = reservationStatusChangeEvent.getReservation();

    String message = messageRetriever.getMessageWithBoldArguments("info_reservation_statuschanged", reservation
        .getName(), reservationStatusChangeEvent.getOldStatus().name(), reservationStatusChangeEvent.getNewStatus().name());

    if (reservationStatusChangeEvent.getNewStatus().isErrorState() && reservation.getFailedReason() != null) {
      message += String.format(" Failed because '%s'.", reservation.getFailedReason());
    }

    boolean cancelAllowed = reservationStatusChangeEvent.getNewStatus().isCancelAllowed();
    String cancelTooltip;
    if (!cancelAllowed) {
      cancelTooltip = messageRetriever.getMessage("reservation_state_transition_not_allowed", new String[] {});
    } else {
      cancelTooltip = messageRetriever.getMessage("label_cancel", new String[] {"Reservation"});
    }

    return new JsonMessageEvent(reservation.getVirtualResourceGroup().get().getAdminGroup(), new JsonEvent(message,
        reservation.getId(), reservationStatusChangeEvent.getNewStatus().name(), cancelAllowed, cancelTooltip));
  }

  private static class JsonEvent {
    private final String message;
    private final Long reservationId;
    private final String status;
    private final Boolean deletable;
    private final String deleteTooltip;

    public JsonEvent(String message, Long reservationId, String status, boolean deletable, String deleteTooltip) {
      this.message = message;
      this.reservationId = reservationId;
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
    public Long getReservationId() {
      return reservationId;
    }

    @SuppressWarnings("unused")
    public String getStatus() {
      return status;
    }
  }

  private static final class JsonMessageEvent implements PushMessage {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private final String groupId;
    private final Object payload;

    public JsonMessageEvent(String groupId, Object toJsonObject) {
      this.groupId = groupId;
      this.payload = toJsonObject;
    }

    @Override
    public String getGroupId() {
      return groupId;
    }

    @Override
    public String toJson() {
      try {
        return JSON_MAPPER.writeValueAsString(payload);
      }
      catch (IOException e) {
        return "{}";
      }
    }

  }
}
