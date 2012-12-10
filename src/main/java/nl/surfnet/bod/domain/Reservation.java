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

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import nl.surfnet.bod.util.TimeStampBridge;

import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.*;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

/**
 * Entity which represents a Reservation for a specific connection between a
 * source and a destination point on a specific moment in time.
 *
 */
@Entity
@Indexed
public class Reservation implements Loggable, PersistableDomain {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Version
  private Integer version;

  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  private String name;

  @IndexedEmbedded
  @ManyToOne
  private VirtualResourceGroup virtualResourceGroup;

  @Enumerated(EnumType.STRING)
  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  private ReservationStatus status = ReservationStatus.REQUESTED;

  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  private String failedReason;

  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  private String cancelReason;

  @NotNull
  @ManyToOne(optional = false)
  @IndexedEmbedded
  private VirtualPort sourcePort;

  @NotNull
  @ManyToOne(optional = false)
  @IndexedEmbedded
  private VirtualPort destinationPort;

  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  @FieldBridge(impl = TimeStampBridge.class)
  private DateTime startDateTime;

  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  @FieldBridge(impl = TimeStampBridge.class)
  private DateTime endDateTime;

  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  @Column(nullable = false)
  private String userCreated;

  @NotNull
  @Column(nullable = false)
  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  private Integer bandwidth;

  @Basic
  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  private String reservationId;

  @NotNull
  @Column(nullable = false)
  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
  private final DateTime creationDateTime;

  @OneToOne(mappedBy = "reservation")
  @IndexedEmbedded
  private Connection connection;

  @Enumerated(EnumType.STRING)
  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  private ProtectionType protectionType = ProtectionType.PROTECTED;

  public Reservation() {
    creationDateTime = DateTime.now();
  }

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

  public VirtualResourceGroup getVirtualResourceGroup() {
    return virtualResourceGroup;
  }

  public ReservationStatus getStatus() {
    return status;
  }

  public void setStatus(ReservationStatus reservationStatus) {
    this.status = reservationStatus;
  }

  public VirtualPort getSourcePort() {
    return sourcePort;
  }

  /**
   * Sets the {@link #sourcePort} and the {@link #virtualResourceGroup} related
   * to this port.
   *
   * @param sourcePort
   *          The source port to set
   * @throws IllegalStateException
   *           When the {@link #virtualResourceGroup} is already set and is not
   *           equal to the one reference by the given port
   */
  public void setSourcePort(VirtualPort sourcePort) {
    this.sourcePort = sourcePort;

    if ((virtualResourceGroup != null) && (!virtualResourceGroup.equals(sourcePort.getVirtualResourceGroup()))) {
      throw new IllegalStateException(
          "Reservation contains a sourcePort and destinationPort from a different VirtualResourceGroup");
    }
    else {
      this.virtualResourceGroup = sourcePort.getVirtualResourceGroup();
    }
  }

  public VirtualPort getDestinationPort() {
    return destinationPort;
  }

  /**
   * Sets the {@link #destinationPort} and the {@link #virtualResourceGroup}
   * related to this port.
   *
   * @param destinationPort
   *          The destinationPort port to set
   * @throws IllegalStateException
   *           When the {@link #virtualResourceGroup} is already set and is not
   *           equal to the one reference by the given port
   */
  public void setDestinationPort(VirtualPort destinationPort) {
    this.destinationPort = destinationPort;

    if ((virtualResourceGroup != null) && (!virtualResourceGroup.equals(destinationPort.getVirtualResourceGroup()))) {
      throw new IllegalStateException(
          "Reservation contains a sourcePort and destinationPort from a different VirtualResourceGroup");
    }
    else {
      this.virtualResourceGroup = destinationPort.getVirtualResourceGroup();
    }
  }

  /**
   *
   * @return LocalTime the time part of the {@link #startDateTime}
   */
  public LocalTime getStartTime() {
    return startDateTime == null ? null : startDateTime.toLocalTime();
  }

  /**
   * Sets the time part of the {@link #startDateTime}
   *
   * @param startTime
   */
  public void setStartTime(LocalTime startTime) {

    if (startTime == null) {
      startDateTime = null;
      return;
    }

    if (startDateTime == null) {
      startDateTime = new DateTime(startTime);
    }
    else {
      startDateTime = startDateTime.withTime(startTime.getHourOfDay(), startTime.getMinuteOfHour(),
          startTime.getSecondOfMinute(), startTime.getMillisOfSecond());
    }
  }

  public String getUserCreated() {
    return userCreated;
  }

  public void setUserCreated(String user) {
    this.userCreated = user;
  }

  /**
   *
   * @return LocalDate The date part of the {@link #getStartDateTime()}
   */
  public LocalDate getStartDate() {
    return startDateTime == null ? null : startDateTime.toLocalDate();
  }

  /**
   * Sets the date part of the {@link #endDateTime}
   *
   * @param startDate
   */
  public void setStartDate(LocalDate startDate) {

    if (startDate == null) {
      startDateTime = null;
      return;
    }

    if (startDateTime == null) {
      startDateTime = new DateTime(startDate.toDate());
    }
    else {
      startDateTime = startDateTime
          .withDate(startDate.getYear(), startDate.getMonthOfYear(), startDate.getDayOfMonth());
    }
  }

  /**
   *
   * @return LocalDate the date part of the {@link #endDateTime}
   */
  public LocalDate getEndDate() {
    return endDateTime == null ? null : endDateTime.toLocalDate();
  }

  /**
   * Sets the date part of the {@link #endDateTime}
   *
   * @param endDate
   */
  public void setEndDate(LocalDate endDate) {
    if (endDate == null) {
      endDateTime = null;
      return;
    }

    if (endDateTime == null) {
      this.endDateTime = new DateTime(endDate.toDate());
    }
    else {
      endDateTime = endDateTime.withDate(endDate.getYear(), endDate.getMonthOfYear(), endDate.getDayOfMonth());
    }
  }

  /**
   *
   * @return LocalTime The time part of the {@link #endDateTime}
   */
  public LocalTime getEndTime() {
    return endDateTime == null ? null : endDateTime.toLocalTime();
  }

  /**
   * Sets the time part of the {@link #endDateTime}
   *
   * @param endTime
   */
  public void setEndTime(LocalTime endTime) {

    if (endTime == null) {
      endDateTime = null;
      return;
    }

    if (endDateTime == null) {
      this.endDateTime = new DateTime(endTime);
    }
    else {
      endDateTime = endDateTime.withTime(endTime.getHourOfDay(), endTime.getMinuteOfHour(),
          endTime.getSecondOfMinute(), endTime.getMillisOfSecond());
    }
  }

  public DateTime getEndDateTime() {
    return endDateTime;
  }

  public void setEndDateTime(DateTime endDateTime) {
    this.endDateTime = endDateTime;
  }

  public DateTime getStartDateTime() {
    return startDateTime;
  }

  public void setStartDateTime(DateTime startDateTime) {
    this.startDateTime = startDateTime;
  }

  public Integer getBandwidth() {
    return bandwidth;
  }

  public void setBandwidth(Integer bandwidth) {
    this.bandwidth = bandwidth;
  }

  public String getReservationId() {
    return reservationId;
  }

  public void setReservationId(String reservationId) {
    this.reservationId = reservationId;
  }

  public DateTime getCreationDateTime() {
    return creationDateTime;
  }

  @Override
  public Collection<String> getAdminGroups() {
    return ImmutableSet.of(
      virtualResourceGroup.getAdminGroup(),
      sourcePort.getPhysicalResourceGroup().getAdminGroup(),
      destinationPort.getPhysicalResourceGroup().getAdminGroup()
    );
  }

  @Override
  public String getLabel() {
    return getName();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Reservation [");
    builder.append("id=");
    builder.append(id);

    builder.append(", ");
    builder.append("version=");
    builder.append(version);

    if (name != null) {
      builder.append(", ");
      builder.append("name=");
      builder.append(name);
    }
    if (virtualResourceGroup != null) {
      builder.append(", ");
      builder.append("virtualResourceGroup=");
      builder.append(virtualResourceGroup.getName());
    }
    if (status != null) {
      builder.append(", ");
      builder.append("status=");
      builder.append(status);
    }
    if (failedReason != null) {
      builder.append(", ");
      builder.append("failedReason=");
      builder.append(failedReason);
    }
    if (cancelReason != null) {
      builder.append(", ");
      builder.append("cancelReason=");
      builder.append(cancelReason);
    }
    if (sourcePort != null) {
      builder.append(", ");
      builder.append("sourcePort=");
      builder.append(sourcePort);
    }
    if (destinationPort != null) {
      builder.append(", ");
      builder.append("destinationPort=");
      builder.append(destinationPort);
    }
    if (startDateTime != null) {
      builder.append(", ");
      builder.append("startDateTime=");
      builder.append(startDateTime);
    }
    if (endDateTime != null) {
      builder.append(", ");
      builder.append("endDateTime=");
      builder.append(endDateTime);
    }
    if (userCreated != null) {
      builder.append(", ");
      builder.append("userCreated=");
      builder.append(userCreated);
    }
    if (bandwidth != null) {
      builder.append(", ");
      builder.append("bandwidth=");
      builder.append(bandwidth);
    }
    if (reservationId != null) {
      builder.append(", ");
      builder.append("reservationId=");
      builder.append(reservationId);
    }
    if (creationDateTime != null) {
      builder.append(", ");
      builder.append("creationDateTime=");
      builder.append(creationDateTime);
    }

    if (connection != null) {
      builder.append(", ");
      builder.append("connection=");
      builder.append(connection.getLabel());
    }
    builder.append("]");
    return builder.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((bandwidth == null) ? 0 : bandwidth.hashCode());
    result = prime * result + ((cancelReason == null) ? 0 : cancelReason.hashCode());
    result = prime * result + ((connection == null) ? 0 : connection.hashCode());
    result = prime * result + ((creationDateTime == null) ? 0 : creationDateTime.hashCode());
    result = prime * result + ((destinationPort == null) ? 0 : destinationPort.hashCode());
    result = prime * result + ((endDateTime == null) ? 0 : endDateTime.hashCode());
    result = prime * result + ((failedReason == null) ? 0 : failedReason.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((protectionType == null) ? 0 : protectionType.hashCode());
    result = prime * result + ((reservationId == null) ? 0 : reservationId.hashCode());
    result = prime * result + ((sourcePort == null) ? 0 : sourcePort.hashCode());
    result = prime * result + ((startDateTime == null) ? 0 : startDateTime.hashCode());
    result = prime * result + ((status == null) ? 0 : status.hashCode());
    result = prime * result + ((userCreated == null) ? 0 : userCreated.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
    result = prime * result + ((virtualResourceGroup == null) ? 0 : virtualResourceGroup.getLabel().hashCode());
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
    Reservation other = (Reservation) obj;
    if (bandwidth == null) {
      if (other.bandwidth != null)
        return false;
    }
    else if (!bandwidth.equals(other.bandwidth))
      return false;
    if (cancelReason == null) {
      if (other.cancelReason != null)
        return false;
    }
    else if (!cancelReason.equals(other.cancelReason))
      return false;
    if (connection == null) {
      if (other.connection != null)
        return false;
    }
    else if (!connection.equals(other.connection))
      return false;
    if (creationDateTime == null) {
      if (other.creationDateTime != null)
        return false;
    }
    else if (!creationDateTime.equals(other.creationDateTime))
      return false;
    if (destinationPort == null) {
      if (other.destinationPort != null)
        return false;
    }
    else if (!destinationPort.equals(other.destinationPort))
      return false;
    if (endDateTime == null) {
      if (other.endDateTime != null)
        return false;
    }
    else if (!endDateTime.equals(other.endDateTime))
      return false;
    if (failedReason == null) {
      if (other.failedReason != null)
        return false;
    }
    else if (!failedReason.equals(other.failedReason))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    }
    else if (!id.equals(other.id))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    }
    else if (!name.equals(other.name))
      return false;
    if (protectionType != other.protectionType)
      return false;
    if (reservationId == null) {
      if (other.reservationId != null)
        return false;
    }
    else if (!reservationId.equals(other.reservationId))
      return false;
    if (sourcePort == null) {
      if (other.sourcePort != null)
        return false;
    }
    else if (!sourcePort.equals(other.sourcePort))
      return false;
    if (startDateTime == null) {
      if (other.startDateTime != null)
        return false;
    }
    else if (!startDateTime.equals(other.startDateTime))
      return false;
    if (status != other.status)
      return false;
    if (userCreated == null) {
      if (other.userCreated != null)
        return false;
    }
    else if (!userCreated.equals(other.userCreated))
      return false;
    if (version == null) {
      if (other.version != null)
        return false;
    }
    else if (!version.equals(other.version))
      return false;
    if (virtualResourceGroup == null) {
      if (other.virtualResourceGroup != null)
        return false;
    }
    else if (!virtualResourceGroup.getLabel().equals(other.virtualResourceGroup.getLabel()))
      return false;
    return true;
  }

  public String getFailedReason() {
    return failedReason;
  }

  public void setFailedReason(String failedReason) {
    this.failedReason = failedReason;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  // needed for nsi and integration tests
  public final void setVirtualResourceGroup(VirtualResourceGroup virtualResourceGroup) {
    this.virtualResourceGroup = virtualResourceGroup;
  }

  public String getCancelReason() {
    return cancelReason;
  }

  public void setCancelReason(String cancelReason) {
    this.cancelReason = cancelReason;
  }

  public Optional<Connection> getConnection() {
    return Optional.fromNullable(connection);
  }

  public void setConnection(Connection connection) {
    this.connection = connection;
  }

  /**
   * @return True if this reservation was made using NSI, false otherwise
   */
  public boolean isNSICreated() {
    return connection != null;
  }

  public ProtectionType getProtectionType() {
    return protectionType;
  }

  public void setProtectionType(ProtectionType protectionType) {
    this.protectionType = protectionType;
  }
}
