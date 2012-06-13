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

import javax.persistence.*;

import org.hibernate.annotations.Type;
import org.joda.time.LocalDateTime;

@Entity
public class ReservationArchive {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Version
  private Integer version;

  // /////////////////////
  // reservation details
  // /////////////////////
  private final String name;
  private final String failedReason;

  @Enumerated(EnumType.STRING)
  private ReservationStatus status = ReservationStatus.REQUESTED;

  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
  private final LocalDateTime startDateTime;

  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
  private final LocalDateTime endDateTime;

  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
  private final LocalDateTime creationDateTime;

  private final String userCreated;
  private final Integer bandwidth;
  private final  String reservationId;

  // ////////////
  // source
  // ////////////
  private final String sourceManagerLabel;
  private final String sourceUserLabel;
  private final Integer sourceMaxBandwidth;
  private final Integer sourceVlanId;

  // /////////////////
  // destination
  // /////////////////
  private final String destinationManagerLabel;
  private final String destinationUserLabel;
  private final Integer destinationMaxBandwidth;
  private final Integer destinationVlanId;

  private final String virtualResourceGroupName;
  private final String virtualResourceGroupDescription;
  private final String virtualResourceGroupSurfconextGroupId;

  // ///////////////////////
  // physical port source
  // ///////////////////////
  private final String physicalPortSourceNocLabel;
  private final String physicalPortSourceManagerLabel;
  private final String physicalPortSourcePortId;
  private final String physicalPortSourceNetworkElementPk;

  // ///////////////////////////
  // physical port destination
  // ///////////////////////////
  private final String physicalPortDestinationNocLabel;
  private final String physicalPortDestinationManagerLabel;
  private final String physicalPortDestinationPortId;
  private final String physicalPortDestinationNetworkElementPk;

  // //////////////////////////////////
  // physical resource group source
  // //////////////////////////////////
  private final long physicalResourceGroupSourceInstituteId;
  private final String physicalResourceGroupSourceAdminGroupName;
  private final String physicalResourceGroupSourceManagerEmail;

  // ///////////////////////////////////////
  // physical resource group destination
  // ///////////////////////////////////////
  private final long physicalResourceGroupDestinationInstituteId;
  private final String physicalResourceGroupDestinationAdminGroupName;
  private final String physicalResourceGroupDestinationManagerEmail;

  public ReservationArchive(final Reservation reservation) {
    super();
    this.bandwidth = reservation.getBandwidth();
    this.creationDateTime = reservation.getCreationDateTime();
    this.destinationManagerLabel = reservation.getDestinationPort().getManagerLabel();
    this.destinationMaxBandwidth = reservation.getDestinationPort().getMaxBandwidth();
    this.destinationUserLabel = reservation.getDestinationPort().getUserLabel();
    this.destinationVlanId = reservation.getDestinationPort().getVlanId();
    this.endDateTime = reservation.getEndDateTime();
    this.failedReason = reservation.getFailedReason();
    this.name = reservation.getName();
    this.physicalPortDestinationManagerLabel = reservation.getDestinationPort().getPhysicalPort().getManagerLabel();
    this.physicalPortDestinationNetworkElementPk = reservation.getDestinationPort().getPhysicalPort()
        .getNetworkElementPk();
    this.physicalPortDestinationNocLabel = reservation.getDestinationPort().getPhysicalPort().getNocLabel();
    this.physicalPortDestinationPortId = reservation.getDestinationPort().getPhysicalPort().getPortId();
    this.physicalPortSourceManagerLabel = reservation.getSourcePort().getPhysicalPort().getManagerLabel();
    this.physicalPortSourceNetworkElementPk = reservation.getDestinationPort().getPhysicalPort().getNetworkElementPk();
    this.physicalPortSourceNocLabel = reservation.getDestinationPort().getPhysicalPort().getNocLabel();
    this.physicalPortSourcePortId = reservation.getDestinationPort().getPhysicalPort().getPortId();
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

    this.physicalResourceGroupSourceAdminGroupName = reservation.getSourcePort().getPhysicalResourceGroup()
        .getAdminGroup();
    this.physicalResourceGroupSourceInstituteId = reservation.getSourcePort().getPhysicalPort()
        .getPhysicalResourceGroup().getInstituteId();
    this.physicalResourceGroupSourceManagerEmail = reservation.getSourcePort().getPhysicalPort()
        .getPhysicalResourceGroup().getManagerEmail();

    this.physicalResourceGroupDestinationAdminGroupName = reservation.getSourcePort().getPhysicalResourceGroup()
        .getAdminGroup();
    this.physicalResourceGroupDestinationInstituteId = reservation.getSourcePort().getPhysicalPort()
        .getPhysicalResourceGroup().getInstituteId();
    this.physicalResourceGroupDestinationManagerEmail = reservation.getSourcePort().getPhysicalPort()
        .getPhysicalResourceGroup().getManagerEmail();
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

  public final String getFailedReason() {
    return failedReason;
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

  public final String getPhysicalPortDestinationNocLabel() {
    return physicalPortDestinationNocLabel;
  }

  public final String getPhysicalPortDestinationManagerLabel() {
    return physicalPortDestinationManagerLabel;
  }

  public final String getPhysicalPortDestinationPortId() {
    return physicalPortDestinationPortId;
  }

  public final String getPhysicalPortDestinationNetworkElementPk() {
    return physicalPortDestinationNetworkElementPk;
  }

  public final String getPhysicalPortSourceNocLabel() {
    return physicalPortSourceNocLabel;
  }

  public final String getPhysicalPortSourceManagerLabel() {
    return physicalPortSourceManagerLabel;
  }

  public final String getPhysicalPortSourcePortId() {
    return physicalPortSourcePortId;
  }

  public final String getPhysicalPortSourceNetworkElementPk() {
    return physicalPortSourceNetworkElementPk;
  }

  public final String getPhysicalResourceGroupSourceAdminGroupName() {
    return physicalResourceGroupSourceAdminGroupName;
  }

  public final String getPhysicalResourceGroupSourceManagerEmail() {
    return physicalResourceGroupSourceManagerEmail;
  }

  public final String getPhysicalResourceGroupDestinationAdminGroupName() {
    return physicalResourceGroupDestinationAdminGroupName;
  }

  public final String getPhysicalResourceGroupDestinationManagerEmail() {
    return physicalResourceGroupDestinationManagerEmail;
  }

  public final long getPhysicalResourceGroupSourceInstituteId() {
    return physicalResourceGroupSourceInstituteId;
  }

  public final long getPhysicalResourceGroupDestinationInstituteId() {
    return physicalResourceGroupDestinationInstituteId;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("ReservationArchive [id=");
    builder.append(id);
    builder.append(", version=");
    builder.append(version);
    builder.append(", name=");
    builder.append(name);
    builder.append(", failedReason=");
    builder.append(failedReason);
    builder.append(", status=");
    builder.append(status);
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
    builder.append(", physicalPortSourceNocLabel=");
    builder.append(physicalPortSourceNocLabel);
    builder.append(", physicalPortSourceManagerLabel=");
    builder.append(physicalPortSourceManagerLabel);
    builder.append(", physicalPortSourcePortId=");
    builder.append(physicalPortSourcePortId);
    builder.append(", physicalPortSourceNetworkElementPk=");
    builder.append(physicalPortSourceNetworkElementPk);
    builder.append(", physicalPortDestinationNocLabel=");
    builder.append(physicalPortDestinationNocLabel);
    builder.append(", physicalPortDestinationManagerLabel=");
    builder.append(physicalPortDestinationManagerLabel);
    builder.append(", physicalPortDestinationPortId=");
    builder.append(physicalPortDestinationPortId);
    builder.append(", physicalPortDestinationNetworkElementPk=");
    builder.append(physicalPortDestinationNetworkElementPk);
    builder.append(", physicalResourceGroupSourceInstituteId=");
    builder.append(physicalResourceGroupSourceInstituteId);
    builder.append(", physicalResourceGroupSourceAdminGroupName=");
    builder.append(physicalResourceGroupSourceAdminGroupName);
    builder.append(", physicalResourceGroupSourceManagerEmail=");
    builder.append(physicalResourceGroupSourceManagerEmail);
    builder.append(", physicalResourceGroupDestinationInstituteId=");
    builder.append(physicalResourceGroupDestinationInstituteId);
    builder.append(", physicalResourceGroupDestinationAdminGroupName=");
    builder.append(physicalResourceGroupDestinationAdminGroupName);
    builder.append(", physicalResourceGroupDestinationManagerEmail=");
    builder.append(physicalResourceGroupDestinationManagerEmail);
    builder.append("]");
    return builder.toString();
  }
}
