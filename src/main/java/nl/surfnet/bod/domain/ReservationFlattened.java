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
package nl.surfnet.bod.domain;

import javax.annotation.Nullable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;
import org.joda.time.LocalDateTime;

@Entity
public class ReservationFlattened {

  // reservation
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Version
  private Integer version;

  private String name;

  @Enumerated(EnumType.STRING)
  private ReservationStatus status = ReservationStatus.REQUESTED;

  private String failedMessage;

  @Type(type = "org.joda.time.contrib.hibernate.PersistentLocalDateTime")
  private LocalDateTime startDateTime;

  @Type(type = "org.joda.time.contrib.hibernate.PersistentLocalDateTime")
  private LocalDateTime endDateTime;

  @NotNull
  @Column(nullable = false)
  @Type(type = "org.joda.time.contrib.hibernate.PersistentLocalDateTime")
  private LocalDateTime creationDateTime;

  @Column(nullable = false)
  private String userCreated;

  @NotNull
  @Column(nullable = false)
  private Integer bandwidth;

  @Basic
  private String reservationId;

  // source vp
  @NotEmpty
  @Column(unique = true, nullable = false)
  private String sourceManagerLabel;

  @Column(unique = true)
  private String sourceUserLabel;

  @NotNull
  @Column(nullable = false)
  private Integer sourceMaxBandwidth;

  @Range(min = 1, max = 4095)
  private Integer sourceVlanId;

  // destination
  @NotEmpty
  @Column(unique = true, nullable = false)
  private String destinationManagerLabel;

  @Column(unique = true)
  private String destinationUserLabel;

  @NotNull
  @Column(nullable = false)
  private Integer destinationMaxBandwidth;

  @Range(min = 1, max = 4095)
  private Integer destinationVlanId;

  @NotEmpty
  @Column(nullable = false)
  private String virtualResourceGroupName;

  @Basic
  private String virtualResourceGroupDescription;

  @NotEmpty
  @Column(unique = true, nullable = false)
  private String virtualResourceGroupSurfconextGroupId;

  // physical port destination
  @NotEmpty
  @Column(nullable = false)
  private String ppDestinationNocLabel;

  private String ppDestinationManagerLabel;

  @NotEmpty
  @Column(nullable = false)
  private String ppDestinationPortId;

  @Nullable
  @Column(unique = true, nullable = false)
  private String ppDestinationNetworkElementPk;

  // physical port source
  @NotEmpty
  @Column(nullable = false)
  private String ppSourceNocLabel;

  private String ppSourceManagerLabel;

  @NotEmpty
  @Column(nullable = false)
  private String ppSourcePortId;

  @Nullable
  @Column(unique = true, nullable = false)
  private String ppSourceNetworkElementPk;

  public ReservationFlattened(final Reservation reservation) {
    super();
    this.bandwidth = reservation.getBandwidth();
    this.creationDateTime = reservation.getCreationDateTime();
    this.destinationManagerLabel = reservation.getDestinationPort().getManagerLabel();
    this.destinationMaxBandwidth = reservation.getDestinationPort().getMaxBandwidth();
    this.destinationUserLabel = reservation.getDestinationPort().getUserLabel();
    this.destinationVlanId = reservation.getDestinationPort().getVlanId();
    this.endDateTime = reservation.getEndDateTime();
    this.failedMessage = reservation.getFailedMessage();
    this.name = reservation.getName();
    this.ppDestinationManagerLabel = reservation.getDestinationPort().getPhysicalPort().getManagerLabel();
    this.ppDestinationNetworkElementPk = reservation.getDestinationPort().getPhysicalPort().getNetworkElementPk();
    this.ppDestinationNocLabel = reservation.getDestinationPort().getPhysicalPort().getNocLabel();
    this.ppDestinationPortId = reservation.getDestinationPort().getPhysicalPort().getPortId();
    this.ppSourceManagerLabel = reservation.getSourcePort().getPhysicalPort().getManagerLabel();
    this.ppSourceNetworkElementPk = reservation.getDestinationPort().getPhysicalPort().getNetworkElementPk();
    this.ppSourceNocLabel = reservation.getDestinationPort().getPhysicalPort().getNocLabel();
    this.ppSourcePortId = reservation.getDestinationPort().getPhysicalPort().getPortId();
    this.reservationId = reservation.getReservationId();
    this.sourceManagerLabel = reservation.getSourcePort().getManagerLabel();
    this.sourceMaxBandwidth = reservation.getSourcePort().getMaxBandwidth();
    this.sourceUserLabel = reservation.getSourcePort().getUserLabel();
    this.sourceVlanId = reservation.getSourcePort().getVlanId();
    this.startDateTime = reservation.getStartDateTime();
    this.status = reservation.getStatus();
    this.userCreated = reservation.getUserCreated();
    this.virtualResourceGroupDescription = reservation.getVirtualResourceGroup().getDescription();
    this.virtualResourceGroupName = reservation.getVirtualResourceGroup().getName();
    this.virtualResourceGroupSurfconextGroupId = reservation.getVirtualResourceGroup().getSurfconextGroupId();

  }

  public final Long getId() {
    return id;
  }

  public final Integer getVersion() {
    return version;
  }

  public final String getName() {
    return name;
  }

  public final ReservationStatus getStatus() {
    return status;
  }

  public final String getFailedMessage() {
    return failedMessage;
  }

  public final LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  public final LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  public final LocalDateTime getCreationDateTime() {
    return creationDateTime;
  }

  public final String getUserCreated() {
    return userCreated;
  }

  public final Integer getBandwidth() {
    return bandwidth;
  }

  public final String getReservationId() {
    return reservationId;
  }

  public final String getSourceManagerLabel() {
    return sourceManagerLabel;
  }

  public final String getSourceUserLabel() {
    return sourceUserLabel;
  }

  public final Integer getSourceMaxBandwidth() {
    return sourceMaxBandwidth;
  }

  public final Integer getSourceVlanId() {
    return sourceVlanId;
  }

  public final String getDestinationManagerLabel() {
    return destinationManagerLabel;
  }

  public final String getDestinationUserLabel() {
    return destinationUserLabel;
  }

  public final Integer getDestinationMaxBandwidth() {
    return destinationMaxBandwidth;
  }

  public final Integer getDestinationVlanId() {
    return destinationVlanId;
  }

  public final String getVirtualResourceGroupName() {
    return virtualResourceGroupName;
  }

  public final String getVirtualResourceGroupDescription() {
    return virtualResourceGroupDescription;
  }

  public final String getVirtualResourceGroupSurfconextGroupId() {
    return virtualResourceGroupSurfconextGroupId;
  }

  public final String getPpDestinationNocLabel() {
    return ppDestinationNocLabel;
  }

  public final String getPpDestinationManagerLabel() {
    return ppDestinationManagerLabel;
  }

  public final String getPpDestinationPortId() {
    return ppDestinationPortId;
  }

  public final String getPpDestinationNetworkElementPk() {
    return ppDestinationNetworkElementPk;
  }

  public final String getPpSourceNocLabel() {
    return ppSourceNocLabel;
  }

  public final String getPpSourceManagerLabel() {
    return ppSourceManagerLabel;
  }

  public final String getPpSourcePortId() {
    return ppSourcePortId;
  }

  public final String getPpSourceNetworkElementPk() {
    return ppSourceNetworkElementPk;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("ReservationFlattened [id=");
    builder.append(id);
    builder.append(", version=");
    builder.append(version);
    builder.append(", name=");
    builder.append(name);
    builder.append(", status=");
    builder.append(status);
    builder.append(", failedMessage=");
    builder.append(failedMessage);
    builder.append(", startDateTime=");
    builder.append(startDateTime);
    builder.append(", endDateTime=");
    builder.append(endDateTime);
    builder.append(", creationDateTime=");
    builder.append(creationDateTime);
    builder.append(", userCreated=");
    builder.append(userCreated);
    builder.append(", bandwidth=");
    builder.append(bandwidth);
    builder.append(", reservationId=");
    builder.append(reservationId);
    builder.append(", sourceManagerLabel=");
    builder.append(sourceManagerLabel);
    builder.append(", sourceUserLabel=");
    builder.append(sourceUserLabel);
    builder.append(", sourceMaxBandwidth=");
    builder.append(sourceMaxBandwidth);
    builder.append(", sourceVlanId=");
    builder.append(sourceVlanId);
    builder.append(", destinationManagerLabel=");
    builder.append(destinationManagerLabel);
    builder.append(", destinationUserLabel=");
    builder.append(destinationUserLabel);
    builder.append(", destinationMaxBandwidth=");
    builder.append(destinationMaxBandwidth);
    builder.append(", destinationVlanId=");
    builder.append(destinationVlanId);
    builder.append(", virtualResourceGroupName=");
    builder.append(virtualResourceGroupName);
    builder.append(", virtualResourceGroupDescription=");
    builder.append(virtualResourceGroupDescription);
    builder.append(", virtualResourceGroupSurfconextGroupId=");
    builder.append(virtualResourceGroupSurfconextGroupId);
    builder.append(", ppDestinationNocLabel=");
    builder.append(ppDestinationNocLabel);
    builder.append(", ppDestinationManagerLabel=");
    builder.append(ppDestinationManagerLabel);
    builder.append(", ppDestinationPortId=");
    builder.append(ppDestinationPortId);
    builder.append(", ppDestinationNetworkElementPk=");
    builder.append(ppDestinationNetworkElementPk);
    builder.append(", ppSourceNocLabel=");
    builder.append(ppSourceNocLabel);
    builder.append(", ppSourceManagerLabel=");
    builder.append(ppSourceManagerLabel);
    builder.append(", ppSourcePortId=");
    builder.append(ppSourcePortId);
    builder.append(", ppSourceNetworkElementPk=");
    builder.append(ppSourceNetworkElementPk);
    builder.append("]");
    return builder.toString();
  }
}
