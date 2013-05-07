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

import java.util.Collection;
import java.util.Collections;

import javax.persistence.*;

import nl.surfnet.bod.util.TimeStampBridge;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.*;
import org.joda.time.DateTime;
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.ogf.schemas.nsi._2011._10.connection.types.PathType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceParametersType;

import com.google.common.base.Optional;

@Entity
@Indexed
@Analyzer(definition = "customanalyzer")
public class Connection implements Loggable, PersistableDomain {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @DocumentId
  private Long id;

  @Column(unique = true, nullable = false)
  @Field
  private String connectionId;

  @Version
  private Integer version;

  @Column(nullable = false)
  @Field
  private String requesterNsa;

  @Column(nullable = false)
  @Field
  private String providerNsa;

  @Column(unique = true, nullable = false)
  @Field
  private String globalReservationId;

  @Column(nullable = false)
  @Field
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  @Field
  private ConnectionStateType currentState = ConnectionStateType.INITIAL;

  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
  @Field
  @FieldBridge(impl = TimeStampBridge.class)
  private DateTime startTime;

  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
  @Field
  @FieldBridge(impl = TimeStampBridge.class)
  private DateTime endTime;

  @Column(nullable = false)
  @Field
  private int desiredBandwidth;

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

  @OneToOne
  @JsonIgnore //prevent loop back to reservation
  @ContainedIn
  private Reservation reservation;

  @OneToOne(cascade = CascadeType.ALL)
  @IndexedEmbedded
  private NsiRequestDetails provisionRequestDetails;

  @Column(nullable = false)
  @Field
  private String protectionType;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private NsiVersion nsiVersion;

  @Override
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public String getRequesterNsa() {
    return requesterNsa;
  }

  public void setRequesterNsa(String requesterNSA) {
    this.requesterNsa = requesterNSA;
  }

  public String getProviderNsa() {
    return providerNsa;
  }

  public void setProviderNsa(String providerNSA) {
    this.providerNsa = providerNSA;
  }

  public String getGlobalReservationId() {
    return globalReservationId;
  }

  public void setGlobalReservationId(String globalReservationId) {
    this.globalReservationId = globalReservationId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getConnectionId() {
    return connectionId;
  }

  public void setConnectionId(String connectionId) {
    this.connectionId = connectionId;
  }

  public ConnectionStateType getCurrentState() {
    return currentState;
  }

  public void setCurrentState(ConnectionStateType currentState) {
    this.currentState = currentState;
  }

  public int getDesiredBandwidth() {
    return desiredBandwidth;
  }

  public void setDesiredBandwidth(int desiredBandwidth) {
    this.desiredBandwidth = desiredBandwidth;
  }

  public Reservation getReservation() {
    return reservation;
  }

  public void setReservation(Reservation reservation) {
    this.reservation = reservation;
  }

  public Optional<DateTime> getStartTime() {
    return Optional.fromNullable(startTime);
  }

  public void setStartTime(DateTime startTime) {
    this.startTime = startTime;
  }

  public Optional<DateTime> getEndTime() {
    return Optional.fromNullable(endTime);
  }

  public void setEndTime(DateTime endTime) {
    this.endTime = endTime;
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

  @Override
  public Collection<String> getAdminGroups() {
    return reservation != null ? reservation.getAdminGroups() : Collections.<String> emptyList();
  }

  @Override
  public String getLabel() {
    return reservation != null ? reservation.getLabel() : connectionId;
  }

  public NsiRequestDetails getProvisionRequestDetails() {
    return provisionRequestDetails;
  }

  public void setProvisionRequestDetails(NsiRequestDetails provisionRequestDetails) {
    this.provisionRequestDetails = provisionRequestDetails;
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

  public void setNsiVersion(NsiVersion nsiVersion) {
    this.nsiVersion = nsiVersion;
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
    Connection other = (Connection) obj;
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