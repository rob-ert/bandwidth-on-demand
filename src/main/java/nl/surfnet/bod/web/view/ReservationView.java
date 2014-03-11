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
package nl.surfnet.bod.web.view;

import com.google.common.base.Function;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.ConnectionV1;
import nl.surfnet.bod.domain.ConnectionV2;
import nl.surfnet.bod.domain.ProtectionType;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.domain.VirtualResourceGroup;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.DateTime;

public class ReservationView {
  private final Long id;
  private final String name;
  private final String virtualResourceGroup;
  private final ReservationStatus status;
  private final PortView sourcePort;
  private final PortView destinationPort;
  private final String failedReason;
  private final String cancelReason;
  @JsonSerialize(using = JsonDateTimeSerializer.class)
  private final DateTime startDateTime;
  @JsonSerialize(using = JsonDateTimeSerializer.class)
  private final DateTime endDateTime;
  private final Long bandwidth;
  private final String userCreated;
  private final String reservationId;
  private final String connectionId;
  private final DateTime creationDateTime;
  private final ElementActionView deleteActionView, editActionView;
  private final ProtectionType protectionType;

  private final String connectionState; // v1 only

  private final String reservationState;
  private final String provisionState;
  private final String lifeCycleState;
  private final String dataPlaneActive;


  public ReservationView(Reservation reservation, ElementActionView deleteActionView, ElementActionView editActionView) {
    this.id = reservation.getId();
    this.virtualResourceGroup = reservation.getVirtualResourceGroup()
        .transform(new Function<VirtualResourceGroup, String>() {
          public String apply(VirtualResourceGroup group) {
            return group.getName();
          }
        }).or("-");
    this.sourcePort = new PortView(reservation.getSourcePort());
    this.destinationPort = new PortView(reservation.getDestinationPort());
    this.status = reservation.getStatus();
    this.failedReason = reservation.getFailedReason();
    this.cancelReason = reservation.getCancelReason();
    this.startDateTime = reservation.getStartDateTime();
    this.endDateTime = reservation.getEndDateTime().orNull();
    this.bandwidth = reservation.getBandwidth();
    this.userCreated = reservation.getUserCreated();
    this.reservationId = reservation.getReservationId();
    this.creationDateTime = reservation.getCreationDateTime();
    this.protectionType = reservation.getProtectionType();
    this.name = reservation.getName();
    this.deleteActionView = deleteActionView;
    this.editActionView = editActionView;
    Connection connection = reservation.getConnection().orNull();
    this.connectionId = connection == null ? null : connection.getConnectionId();

    if (!reservation.isNSICreated()) {
      connectionState = null;
      reservationState = null;
      provisionState = null;
      lifeCycleState = null;
      dataPlaneActive = null;
    } else if (reservation.getConnectionV1().isPresent()) {
      ConnectionV1 connectionV1 = reservation.getConnectionV1().get();
      reservationState = null;
      provisionState = null;
      lifeCycleState = null;
      dataPlaneActive = null;
      connectionState = connectionV1.getCurrentState().toString();
    } else if (reservation.getConnectionV2().isPresent()) {
      ConnectionV2 connectionV2 = reservation.getConnectionV2().get();
      connectionState = null;
      reservationState = connectionV2.getReservationState().value();
      provisionState = connectionV2.getProvisionState().value();
      lifeCycleState = connectionV2.getLifecycleState().value();
      dataPlaneActive = connectionV2.getDataPlaneActive() ? "Active" : "Inactive";
    } else {
      throw new IllegalStateException("Reservation is NSI created but has no connection");
    }
  }

  public String getConnectionState() {
    return connectionState;
  }
  public String getVirtualResourceGroup() {
    return virtualResourceGroup;
  }

  public PortView getSourcePort() {
    return sourcePort;
  }

  public PortView getDestinationPort() {
    return destinationPort;
  }

  public ReservationStatus getStatus() {
    return status;
  }

  public String getFailedReason() {
    return failedReason;
  }

  public DateTime getStartDateTime() {
    return startDateTime;
  }

  public DateTime getEndDateTime() {
    return endDateTime;
  }

  public Long getBandwidth() {
    return bandwidth;
  }

  public Long getId() {
    return id;
  }

  public String getUserCreated() {
    return userCreated;
  }

  public String getReservationId() {
    return reservationId;
  }

  public DateTime getCreationDateTime() {
    return creationDateTime;
  }

  public String getName() {
    return name;
  }

  public boolean isDeleteAllowedForSelectedRole() {
    return deleteActionView.isAllowed();
  }

  public boolean isUpdateAllowedForSelectedRole() {
    return editActionView.isAllowed();
  }

  public String getDeleteReasonKey() {
    return deleteActionView.getReasonKey();
  }

  public String getUpdateReasonKey() {
    return editActionView.getReasonKey();
  }


  public String getConnectionId() {
    return connectionId;
  }

  public String getCancelReason() {
    return cancelReason;
  }

  public ProtectionType getProtectionType() {
    return protectionType;
  }

  public String getReservationState() {
    return reservationState;
  }

  public String getProvisionState() {
    return provisionState;
  }

  public String getLifeCycleState() {
    return lifeCycleState;
  }

  public String getDataPlaneActive() {
    return dataPlaneActive;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((bandwidth == null) ? 0 : bandwidth.hashCode());
    result = prime * result + ((cancelReason == null) ? 0 : cancelReason.hashCode());
    result = prime * result + ((connectionId == null) ? 0 : connectionId.hashCode());
    result = prime * result + ((creationDateTime == null) ? 0 : creationDateTime.hashCode());
    result = prime * result + ((deleteActionView == null) ? 0 : deleteActionView.hashCode());
    result = prime * result + ((destinationPort == null) ? 0 : destinationPort.hashCode());
    result = prime * result + ((endDateTime == null) ? 0 : endDateTime.hashCode());
    result = prime * result + ((failedReason == null) ? 0 : failedReason.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((reservationId == null) ? 0 : reservationId.hashCode());
    result = prime * result + ((sourcePort == null) ? 0 : sourcePort.hashCode());
    result = prime * result + ((startDateTime == null) ? 0 : startDateTime.hashCode());
    result = prime * result + ((status == null) ? 0 : status.hashCode());
    result = prime * result + ((userCreated == null) ? 0 : userCreated.hashCode());
    result = prime * result + ((virtualResourceGroup == null) ? 0 : virtualResourceGroup.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ReservationView other = (ReservationView) obj;
    if (bandwidth == null) {
      if (other.bandwidth != null) {
        return false;
      }
    }
    else if (!bandwidth.equals(other.bandwidth)) {
      return false;
    }
    if (cancelReason == null) {
      if (other.cancelReason != null) {
        return false;
      }
    }
    else if (!cancelReason.equals(other.cancelReason)) {
      return false;
    }
    if (connectionId == null) {
      if (other.connectionId != null) {
        return false;
      }
    }
    else if (!connectionId.equals(other.connectionId)) {
      return false;
    }
    if (creationDateTime == null) {
      if (other.creationDateTime != null) {
        return false;
      }
    }
    else if (!creationDateTime.equals(other.creationDateTime)) {
      return false;
    }
    if (deleteActionView == null) {
      if (other.deleteActionView != null) {
        return false;
      }
    }
    else if (!deleteActionView.equals(other.deleteActionView)) {
      return false;
    }
    if (destinationPort == null) {
      if (other.destinationPort != null) {
        return false;
      }
    }
    else if (!destinationPort.equals(other.destinationPort)) {
      return false;
    }
    if (endDateTime == null) {
      if (other.endDateTime != null) {
        return false;
      }
    }
    else if (!endDateTime.equals(other.endDateTime)) {
      return false;
    }
    if (failedReason == null) {
      if (other.failedReason != null) {
        return false;
      }
    }
    else if (!failedReason.equals(other.failedReason)) {
      return false;
    }
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    }
    else if (!id.equals(other.id)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    }
    else if (!name.equals(other.name)) {
      return false;
    }
    if (reservationId == null) {
      if (other.reservationId != null) {
        return false;
      }
    }
    else if (!reservationId.equals(other.reservationId)) {
      return false;
    }
    if (sourcePort == null) {
      if (other.sourcePort != null) {
        return false;
      }
    }
    else if (!sourcePort.equals(other.sourcePort)) {
      return false;
    }
    if (startDateTime == null) {
      if (other.startDateTime != null) {
        return false;
      }
    }
    else if (!startDateTime.equals(other.startDateTime)) {
      return false;
    }
    if (status != other.status) {
      return false;
    }
    if (userCreated == null) {
      if (other.userCreated != null) {
        return false;
      }
    }
    else if (!userCreated.equals(other.userCreated)) {
      return false;
    }
    if (virtualResourceGroup == null) {
      if (other.virtualResourceGroup != null) {
        return false;
      }
    }
    else if (!virtualResourceGroup.equals(other.virtualResourceGroup)) {
      return false;
    }
    return true;
  }

}
