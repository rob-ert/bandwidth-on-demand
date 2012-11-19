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

import nl.surfnet.bod.util.TimeStampBridge;

import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.*;
import org.joda.time.DateTime;
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.ogf.schemas.nsi._2011._10.connection.types.PathType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceParametersType;

import com.google.common.base.Optional;

@Entity
@Indexed
public class Connection implements Loggable, PersistableDomain {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  /**
   * Connection id for the reservation is unique, but there have been
   * discussions that it is only unique in the context of the requesting NSA. We
   * save time and assume it is unique. This will also be the primary key used
   * in the storage structure
   */
  @Column(unique = true, nullable = false)
  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  private String connectionId;

  @Version
  private Integer version;

  @Column(nullable = false)
  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  private String requesterNsa;

  @Column(nullable = false)
  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  private String providerNsa;

  @Column(unique = true, nullable = false)
  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  private String globalReservationId;

  @Column(nullable = false)
  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  private ConnectionStateType currentState = ConnectionStateType.INITIAL;

  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  @FieldBridge(impl = TimeStampBridge.class)
  private DateTime startTime;

  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  @FieldBridge(impl = TimeStampBridge.class)
  private DateTime endTime;

  @Column(nullable = false)
  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  private int desiredBandwidth;

  @Column(nullable = false)
  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  private String sourceStpId;

  @Column(nullable = false)
  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
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
  public String getAdminGroup() {
    return reservation != null ? reservation.getAdminGroup() : null;
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

  public NsiRequestDetails getProvisionRequestDetails() {
    return provisionRequestDetails;
  }

  public void setProvisionRequestDetails(NsiRequestDetails provisionRequestDetails) {
    this.provisionRequestDetails = provisionRequestDetails;
  }

}
