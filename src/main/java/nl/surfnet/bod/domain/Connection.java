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

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;

import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.ogf.schemas.nsi._2011._10.connection.types.PathType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceParametersType;

@Entity
public class Connection {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Version
  private Integer version;

  // /////////////////////////////////////////////////////
  // Store all the parameters relating to the schedule.
  // /////////////////////////////////////////////////////

  // Requester NSA for this reservation.
  @Column(unique = false, nullable = false, length = 255)
  private String requesterNsa = null;

  // This will be my NSA.
  @Column(unique = false, nullable = false, length = 255)
  private String providerNsa = null;

  // Global Id of the reservation which may be null.
  @Column(unique = true, nullable = false, length = 255)
  private String globalReservationId = null;

  // Reservation description.
  @Column(unique = false, nullable = false, length = 255)
  private String description = null;

  /**
   * Connection id for the reservation is unique, but there have been
   * discussions that it is only unique in the context of the requesting NSA. We
   * save time and assume it is unique. This will also be the primary key used
   * in the storage structure
   */
  @Column(unique = true, nullable = false, length = 255)
  private String connectionId = null;

  // replyTo field for forcedEnd messages (my be moed to topology).
  @Column(unique = false, nullable = false, length = 255)
  private String replyTo = null;

  // State of the reservation.
  @Enumerated(EnumType.STRING)
  @Column(unique = false, nullable = false, length = 50)
  private ConnectionStateType currentState = ConnectionStateType.INITIAL;

  // // The original request service parameters.
  @Column(unique = false, nullable = false, length = 4096)
  private ServiceParametersType serviceParameters = null;

  // // Original path information.
  @Column(unique = false, nullable = false, length = 4096)
  private PathType path = null;

  // //////////////////////////////////////////////////
  // The reservation parameters to which we committed.
  // //////////////////////////////////////////////////

  // // The date and time of the reservation.
  @Column(unique = false, nullable = false, length = 255)
  private Date startTime = null;

  @Column(unique = false, nullable = false, length = 255)
  private Date endTime = null;

  // Use only desired bandwidth for now.
  @Column(unique = false, nullable = false, length = 255)
  private int desiredBandwidth = -1;

  @Column(unique = false, nullable = false, length = 255)
  private int minimumBandwidth;

  @Column(unique = false, nullable = false, length = 255)
  private int maximumBandwidth;

  @Basic
  private String reservationId;

  public final Long getId() {
    return id;
  }

  public final void setId(Long id) {
    this.id = id;
  }

  public final Integer getVersion() {
    return version;
  }

  public final void setVersion(Integer version) {
    this.version = version;
  }

  public final String getRequesterNsa() {
    return requesterNsa;
  }

  public final void setRequesterNsa(String requesterNSA) {
    this.requesterNsa = requesterNSA;
  }

  public final String getProviderNsa() {
    return providerNsa;
  }

  public final void setProviderNsa(String providerNSA) {
    this.providerNsa = providerNSA;
  }

  public final String getGlobalReservationId() {
    return globalReservationId;
  }

  public final void setGlobalReservationId(String globalReservationId) {
    this.globalReservationId = globalReservationId;
  }

  public final String getDescription() {
    return description;
  }

  public final void setDescription(String description) {
    this.description = description;
  }

  public final String getConnectionId() {
    return connectionId;
  }

  public final void setConnectionId(String connectionId) {
    this.connectionId = connectionId;
  }

  public final String getReplyTo() {
    return replyTo;
  }

  public final void setReplyTo(String replyTo) {
    this.replyTo = replyTo;
  }

  public final ConnectionStateType getCurrentState() {
    return currentState;
  }

  public final void setCurrentState(ConnectionStateType currentState) {
    this.currentState = currentState;
  }

  public final int getDesiredBandwidth() {
    return desiredBandwidth;
  }

  public final void setDesiredBandwidth(int desiredBandwidth) {
    this.desiredBandwidth = desiredBandwidth;
  }

  public final int getMinimumBandwidth() {
    return minimumBandwidth;
  }

  public final void setMinimumBandwidth(int minimumBandwidth) {
    this.minimumBandwidth = minimumBandwidth;
  }

  public final int getMaximumBandwidth() {
    return maximumBandwidth;
  }

  public final void setMaximumBandwidth(int maximumBandwidth) {
    this.maximumBandwidth = maximumBandwidth;
  }

  public final String getReservationId() {
    return reservationId;
  }

  public final void setReservationId(String reservationId) {
    this.reservationId = reservationId;
  }

  public final Date getStartTime() {
    return startTime;
  }

  public final void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public final Date getEndTime() {
    return endTime;
  }

  public final void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  public final ServiceParametersType getServiceParameters() {
    return serviceParameters;
  }

  public final void setServiceParameters(ServiceParametersType serviceParameters) {
    this.serviceParameters = serviceParameters;
  }

  public final PathType getPath() {
    return path;
  }

  public final void setPath(PathType path) {
    this.path = path;
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
    builder.append(", globalReservationId=");
    builder.append(globalReservationId);
    builder.append(", description=");
    builder.append(description);
    builder.append(", connectionId=");
    builder.append(connectionId);
    builder.append(", replyTo=");
    builder.append(replyTo);
    builder.append(", currentState=");
    builder.append(currentState);
    builder.append(", serviceParameters=");
    builder.append(serviceParameters);
    builder.append(", path=");
    builder.append(path);
    builder.append(", startTime=");
    builder.append(startTime);
    builder.append(", endTime=");
    builder.append(endTime);
    builder.append(", desiredBandwidth=");
    builder.append(desiredBandwidth);
    builder.append(", minimumBandwidth=");
    builder.append(minimumBandwidth);
    builder.append(", maximumBandwidth=");
    builder.append(maximumBandwidth);
    builder.append(", reservationId=");
    builder.append(reservationId);
    builder.append("]");
    return builder.toString();
  }

}
