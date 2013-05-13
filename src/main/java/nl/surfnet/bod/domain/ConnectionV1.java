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
package nl.surfnet.bod.domain;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.ogf.schemas.nsi._2011._10.connection.types.PathType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceParametersType;

@Entity
@DiscriminatorValue("V1")
@Indexed
@Analyzer(definition = "customanalyzer")
public class ConnectionV1 extends AbstractConnection {

  @Column(unique = true, nullable = false)
  @Field
  private String globalReservationId;

  @Enumerated(EnumType.STRING)
  @Column(length = 50)
  @Field
  private ConnectionStateType currentState = ConnectionStateType.INITIAL;

  @Column(nullable = false)
  @Field
  private String sourceStpId;

  @Column(nullable = false)
  @Field
  private String destinationStpId;

  @Type(type = "nl.surfnet.bod.util.PathTypeUserType")
  @Column(nullable = false, length = 4096)
  private PathType path;

  @Type(type = "nl.surfnet.bod.util.ServiceParametersTypeUserType")
  @Column(nullable = false, length = 4096)
  private ServiceParametersType serviceParameters;

  @Column(nullable = false)
  @Field
  private String protectionType;

  public ConnectionV1() {
    super(NsiVersion.ONE);
  }

  public String getGlobalReservationId() {
    return globalReservationId;
  }

  public void setGlobalReservationId(String globalReservationId) {
    this.globalReservationId = globalReservationId;
  }

  @Override
  public String getConnectionStatus() {
    return currentState.toString();
  }

  public ConnectionStateType getCurrentState() {
    return currentState;
  }

  public void setCurrentState(ConnectionStateType currentState) {
    this.currentState = currentState;
  }

  public String getSourceStpId() {
    return sourceStpId;
  }

  public void setSourceStpId(String sourceStpId) {
    this.sourceStpId = sourceStpId;
  }

  public String getDestinationStpId() {
    return destinationStpId;
  }

  public void setDestinationStpId(String destitnationStpId) {
    this.destinationStpId = destitnationStpId;
  }

  public PathType getPath() {
    return path;
  }

  public void setPath(PathType path) {
    this.path = path;
  }

  public ServiceParametersType getServiceParameters() {
    return serviceParameters;
  }

  public void setServiceParameters(ServiceParametersType serviceParameters) {
    this.serviceParameters = serviceParameters;
  }

  public String getProtectionType() {
    return protectionType;
  }

  public void setProtectionType(String protectionType) {
    this.protectionType = protectionType;
  }

  public NsiVersion getNsiVersion() {
    return nsiVersion;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Connection [");
    if (id != null) {
      builder.append("id=");
      builder.append(id);
      builder.append(", ");
    }
    if (connectionId != null) {
      builder.append("connectionId=");
      builder.append(connectionId);
      builder.append(", ");
    }
    if (version != null) {
      builder.append("version=");
      builder.append(version);
      builder.append(", ");
    }
    if (requesterNsa != null) {
      builder.append("requesterNsa=");
      builder.append(requesterNsa);
      builder.append(", ");
    }
    if (providerNsa != null) {
      builder.append("providerNsa=");
      builder.append(providerNsa);
      builder.append(", ");
    }
    if (globalReservationId != null) {
      builder.append("globalReservationId=");
      builder.append(globalReservationId);
      builder.append(", ");
    }
    if (description != null) {
      builder.append("description=");
      builder.append(description);
      builder.append(", ");
    }
    if (currentState != null) {
      builder.append("currentState=");
      builder.append(currentState);
      builder.append(", ");
    }
    if (startTime != null) {
      builder.append("startTime=");
      builder.append(startTime);
      builder.append(", ");
    }
    if (endTime != null) {
      builder.append("endTime=");
      builder.append(endTime);
      builder.append(", ");
    }
    builder.append("desiredBandwidth=");
    builder.append(desiredBandwidth);
    builder.append(", ");
    if (sourceStpId != null) {
      builder.append("sourceStpId=");
      builder.append(sourceStpId);
      builder.append(", ");
    }
    if (destinationStpId != null) {
      builder.append("destinationStpId=");
      builder.append(destinationStpId);
      builder.append(", ");
    }
    if (path != null) {
      builder.append("path=");
      builder.append(path);
      builder.append(", ");
    }
    if (serviceParameters != null) {
      builder.append("serviceParameters=");
      builder.append(serviceParameters);
      builder.append(", ");
    }
    if (reservation != null) {
      builder.append("reservation=");
      builder.append(reservation.getId()).append(" ").append(reservation.getName());
      builder.append(", ");
    }
    if (provisionRequestDetails != null) {
      builder.append("provisionRequestDetails=");
      builder.append(provisionRequestDetails);
    }
    builder.append("]");
    return builder.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
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
    ConnectionV1 other = (ConnectionV1) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    }
    else if (!id.equals(other.id)) {
      return false;
    }
    if (version == null) {
      if (other.version != null) {
        return false;
      }
    }
    else if (!version.equals(other.version)) {
      return false;
    }
    return true;
  }

}