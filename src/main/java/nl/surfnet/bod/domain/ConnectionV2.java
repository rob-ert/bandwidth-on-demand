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
package nl.surfnet.bod.domain;

import static nl.surfnet.bod.util.XmlUtils.dateTimeToXmlCalendar;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.namespace.QName;

import com.google.common.base.Optional;

import nl.surfnet.bod.nsi.v2.ConnectionsV2;
import nl.surfnet.bod.util.NsiV2UserType;
import nl.surfnet.bod.util.TimeStampBridge;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.joda.time.DateTime;
import org.ogf.schemas.nsi._2013._12.connection.types.*;
import org.ogf.schemas.nsi._2013._12.services.point2point.P2PServiceBaseType;

@Entity
@Indexed
@Analyzer(definition = "customanalyzer")
@Table(name = "CONNECTION_V2")
public class ConnectionV2 extends AbstractConnection {

  @Column(unique = true, nullable = false)
  @Field
  private String globalReservationId;

  @Enumerated(EnumType.STRING)
  @Field
  private ReservationStateEnumType reservationState;

  @Enumerated(EnumType.STRING)
  @Field
  private ProvisionStateEnumType provisionState;

  @Enumerated(EnumType.STRING)
  @Field
  private LifecycleStateEnumType lifecycleState;

  @Column(nullable = false)
  @Field
  private String sourceStpId;

  @Column(nullable = false)
  @Field
  private String destinationStpId;

  @NotNull
  @Type(type = "nl.surfnet.bod.domain.ConnectionV2$ReservationConfirmCriteriaTypeUserType")
  @Column(nullable = false)
  private ReservationConfirmCriteriaType criteria;

  @Field
  private boolean dataPlaneActive;

  @NotNull
  @Field
  @Column(nullable = true)
  private int reserveVersion;

  @Field
  private Integer committedVersion;

  @OneToOne(cascade = CascadeType.ALL)
  @IndexedEmbedded
  private NsiV2RequestDetails initialReserveRequestDetails;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private NsiV2RequestDetails lastReservationRequestDetails;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private NsiV2RequestDetails lastProvisionRequestDetails;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private NsiV2RequestDetails lastLifecycleRequestDetails;

  @Field
  private int reserveHeldTimeoutValue;

  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
  @Field
  @FieldBridge(impl = TimeStampBridge.class)
  private DateTime reserveHeldTimeout;

  @ElementCollection(fetch = FetchType.EAGER)
  @Type(type = "nl.surfnet.bod.domain.ConnectionV2$NotificationBaseTypeUserType")
  @CollectionTable(name = "notification")
  @Column(name = "notification")
  private List<NotificationBaseType> notifications = new ArrayList<>();

  public ConnectionV2() {
  }

  /**
   *
   * @return the next id that you can assign to the NotificationBaseType that you want to add to the collection of notifications
   */
  public int nextNotificationId(){
    // this looks like its vulnerable to lost updates but it won't, because a lost update will be prevented by an optimistic locking exception
    return notifications.size() + 1;
  }
  public String getGlobalReservationId() {
    return globalReservationId;
  }

  public void setGlobalReservationId(String globalReservationId) {
    this.globalReservationId = globalReservationId;
  }

  public ReservationStateEnumType getReservationState() {
    return reservationState;
  }

  public LifecycleStateEnumType getLifecycleState() {
    return lifecycleState;
  }

  public void setLifecycleState(LifecycleStateEnumType lifecycleState) {
    this.lifecycleState = lifecycleState;
  }

  public Optional<ProvisionStateEnumType> getProvisionState() {
    return Optional.fromNullable(provisionState);
  }

  public void setProvisionState(ProvisionStateEnumType provisionState) {
    this.provisionState = provisionState;
  }

  public void setReservationState(ReservationStateEnumType reservationState) {
    this.reservationState = reservationState;
  }

  public String getSourceStpId() {
    return sourceStpId;
  }

  public String getDestinationStpId() {
    return destinationStpId;
  }

  public NsiVersion getNsiVersion() {
    return NsiVersion.TWO;
  }

  public boolean getDataPlaneActive() {
    return dataPlaneActive;
  }

  public void setDataPlaneActive(boolean dataPlaneActive) {
    this.dataPlaneActive = dataPlaneActive;
  }

  public void setReserveVersion(int reserveVersion) {
    this.reserveVersion = reserveVersion;
  }

  public int getReserveVersion() {
    return reserveVersion;
  }

  public void setCommittedVersion(Optional<Integer> committedVersion) {
    this.committedVersion = committedVersion.orNull();
  }

  public Optional<Integer> getCommittedVersion() {
    return Optional.fromNullable(committedVersion);
  }

  public void setReserveHeldTimeoutValue(int reserveHeldTimeoutValue) {
    this.reserveHeldTimeoutValue = reserveHeldTimeoutValue;
  }

  public int getReserveHeldTimeoutValue() {
    return this.reserveHeldTimeoutValue;
  }

  public void setReserveHeldTimeout(Optional<DateTime> reserveHeldTimeout) {
    this.reserveHeldTimeout = reserveHeldTimeout.orNull();
  }

  public Optional<DateTime> getReserveHeldTimeout() {
    return Optional.fromNullable(reserveHeldTimeout);
  }

  public QuerySummaryResultType getQuerySummaryResult() {
    QuerySummaryResultType result = new QuerySummaryResultType()
        .withConnectionId(getConnectionId())
        .withGlobalReservationId(getGlobalReservationId())
        .withDescription(getDescription())
        .withRequesterNSA(getRequesterNsa())
        .withConnectionStates(getConnectionStates());
    if (committedVersion != null) {
      result.withCriteria(new QuerySummaryResultCriteriaType()
          .withAny(criteria.getAny())
          .withSchedule(criteria.getSchedule())
          .withServiceType(criteria.getServiceType())
          .withVersion(committedVersion));
    }
    return result;
  }

  public QueryRecursiveResultType getQueryRecursiveResult() {
    return new QueryRecursiveResultType()
        .withConnectionId(getConnectionId())
        .withGlobalReservationId(getGlobalReservationId())
        .withDescription(getDescription())
        .withCriteria(new QueryRecursiveResultCriteriaType()
            .withAny(criteria.getAny())
            .withSchedule(criteria.getSchedule())
            .withServiceType(criteria.getServiceType())
            .withVersion(0)) // FIXME: committed version?
        .withRequesterNSA(getRequesterNsa())
        .withConnectionStates(getConnectionStates());
  }

  public ScheduleType getSchedule() {
    return new ScheduleType()
        .withEndTime(getEndTime().transform(dateTimeToXmlCalendar).orNull())
        .withStartTime(getStartTime().transform(dateTimeToXmlCalendar).orNull());
  }

  public void setCriteria(ReservationConfirmCriteriaType criteria) {
    this.criteria = criteria;
    Optional<P2PServiceBaseType> service = ConnectionsV2.findPointToPointService(criteria);
    if (service.isPresent()) {
      this.sourceStpId = service.get().getSourceSTP();
      this.destinationStpId = service.get().getDestSTP();
    }
  }

  public ReservationConfirmCriteriaType getCriteria() {
    return criteria;
  }

  public ConnectionStatesType getConnectionStates() {
    return new ConnectionStatesType()
        .withReservationState(getReservationState())
        .withLifecycleState(getLifecycleState())
        .withProvisionState(getProvisionState().orNull())
        .withDataPlaneStatus(getDataPlaneStatus());
  }

  private DataPlaneStatusType getDataPlaneStatus() {
    return new DataPlaneStatusType()
      .withActive(dataPlaneActive)
      .withVersionConsistent(true)
      .withVersion(committedVersion != null ? committedVersion : reserveVersion);
  }

  public NsiV2RequestDetails getInitialReserveRequestDetails() {
    return initialReserveRequestDetails;
  }

  public void setInitialReserveRequestDetails(NsiV2RequestDetails initialReserveRequestDetails) {
    this.initialReserveRequestDetails = initialReserveRequestDetails;
  }

  public NsiV2RequestDetails getLastReservationRequestDetails() {
    return lastReservationRequestDetails;
  }

  public void setLastReservationRequestDetails(NsiV2RequestDetails reserveRequestDetails) {
    this.lastReservationRequestDetails = reserveRequestDetails;
  }

  public NsiV2RequestDetails getLastProvisionRequestDetails() {
    return lastProvisionRequestDetails;
  }

  public void setLastProvisionRequestDetails(NsiV2RequestDetails provisionRequestDetails) {
    this.lastProvisionRequestDetails = provisionRequestDetails;
  }

  public NsiV2RequestDetails getLastLifecycleRequestDetails() {
    return lastLifecycleRequestDetails;
  }

  public void setLastLifecycleRequestDetails(NsiV2RequestDetails terminateRequestDetails) {
    this.lastLifecycleRequestDetails = terminateRequestDetails;
  }

  public List<NotificationBaseType> getNotifications() {
    return notifications;
  }

  public void setNotifications(List<NotificationBaseType> notifications) {
    this.notifications = notifications;
  }

  public boolean addNotification(NotificationBaseType notification) {
    return this.notifications.add(notification);
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
    if (jpaVersion != null) {
      builder.append("jpaVersion=");
      builder.append(jpaVersion);
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
    if (reservationState != null) {
      builder.append("resevationState=");
      builder.append(reservationState);
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
    if (reservation != null) {
      builder.append("reservation=");
      builder.append(reservation.getId()).append(" ").append(reservation.getName());
      builder.append(", ");
    }
    if (initialReserveRequestDetails != null) {
      builder.append("initialReserveRequestDetails=");
      builder.append(initialReserveRequestDetails);
    }
    builder.append("]");
    return builder.toString();
  }

  public static class NotificationBaseTypeUserType extends NsiV2UserType<NotificationBaseType> {
    public NotificationBaseTypeUserType() {
      super(new QName("http://schemas.ogf.org/nsi/2013/12/connection/types", "notificationBaseType"), NotificationBaseType.class);
    }
  }
  public static class ReservationConfirmCriteriaTypeUserType extends NsiV2UserType<ReservationConfirmCriteriaType> {
    public ReservationConfirmCriteriaTypeUserType() {
      super(new QName("criteria"), ReservationConfirmCriteriaType.class);
    }
  }
}
