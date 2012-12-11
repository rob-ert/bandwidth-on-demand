/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.domain;

import java.util.Collection;
import java.util.Collections;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Version;

import nl.surfnet.bod.util.TimeStampBridge;

import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
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

  /**
   * Connection id for the reservation is unique, but there have been
   * discussions that it is only unique in the context of the requesting NSA. We
   * save time and assume it is unique. This will also be the primary key used
   * in the storage structure
   */
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

  @Column(nullable = false, length = 4096)
  private PathType path;

  @Column(nullable = false, length = 4096)
  private ServiceParametersType serviceParameters;

  @OneToOne
  private Reservation reservation;

  @OneToOne(cascade = CascadeType.ALL)
  @IndexedEmbedded
  private NsiRequestDetails provisionRequestDetails;

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

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Connection [id=");
    builder.append(id);
    builder.append(", version=");
    builder.append(version);
    builder.append(", requesterNsa=");
    builder.append(requesterNsa);
    builder.append(", providerNsa=");
    builder.append(providerNsa);
    builder.append(", sourceStpId=");
    builder.append(sourceStpId);
    builder.append(", destinationStpId=");
    builder.append(destinationStpId);
    builder.append(", globalReservationId=");
    builder.append(globalReservationId);
    builder.append(", description=");
    builder.append(description);
    builder.append(", connectionId=");
    builder.append(connectionId);
    builder.append(", currentState=");
    builder.append(currentState);
    builder.append(", startTime=");
    builder.append(startTime);
    builder.append(", endTime=");
    builder.append(endTime);
    builder.append(", desiredBandwidth=");
    builder.append(desiredBandwidth);
    builder.append(", reservation=");
    builder.append(reservation);
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
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Connection other = (Connection) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    }
    else if (!id.equals(other.id))
      return false;
    if (version == null) {
      if (other.version != null)
        return false;
    }
    else if (!version.equals(other.version))
      return false;
    return true;
  }

  public NsiRequestDetails getProvisionRequestDetails() {
    return provisionRequestDetails;
  }

  public void setProvisionRequestDetails(NsiRequestDetails provisionRequestDetails) {
    this.provisionRequestDetails = provisionRequestDetails;
  }

}
