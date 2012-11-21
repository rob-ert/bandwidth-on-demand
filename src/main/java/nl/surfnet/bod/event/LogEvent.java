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
package nl.surfnet.bod.event;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import nl.surfnet.bod.domain.Loggable;
import nl.surfnet.bod.domain.PersistableDomain;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.util.TimeStampBridge;
import nl.surfnet.bod.web.WebUtils;

import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.*;
import org.joda.time.DateTime;
import org.springframework.util.StringUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

@Entity
@Indexed
public class LogEvent implements PersistableDomain {

  @VisibleForTesting
  static final String LIST_STRING = "List of %d %s(s)";

  @VisibleForTesting
  static final String LIST_EMPTY = "Empty list";

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  @FieldBridge(impl = TimeStampBridge.class)
  private final DateTime created;

  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  @Basic
  private final String userId;

  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  @Basic
  private final String adminGroup;

  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  @Enumerated(EnumType.STRING)
  private final LogEventType eventType;

  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  @Column(nullable = true)
  private final String domainObjectClass;

  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  @Column(nullable = true)
  private final String description;

  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  @Type(type = "text")
  private final String serializedObject;

  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  @Type(type = "text")
  private final String details;

  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  @Basic
  private String correlationId;

  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  @Basic
  @Column(nullable = true)
  private final Long domainObjectId;

  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  @Enumerated(EnumType.STRING)
  @Column(nullable = true)
  private final ReservationStatus oldReservationStatus;

  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  @Enumerated(EnumType.STRING)
  @Column(nullable = true)
  private final ReservationStatus newReservationStatus;

  /**
   * Default constructor for Hibernate
   */
  @SuppressWarnings("unused")
  private LogEvent() {
    this((String) null, (String) null, (LogEventType) null, null);
  }

  public LogEvent(String userId, String adminGroup, LogEventType type, Loggable domainObject) {
    this(userId, adminGroup, type, Optional.fromNullable(domainObject), null, Optional.<ReservationStatus> absent(),
        Optional.<ReservationStatus> absent());
  }

  public LogEvent(String userId, String adminGroup, LogEventType type, Optional<Loggable> domainObject, String details,
      Optional<ReservationStatus> oldStatus, Optional<ReservationStatus> newStatus) {
    super();
    this.userId = userId;
    this.adminGroup = adminGroup;
    this.eventType = type;
    this.details = details;

    this.created = DateTime.now();

    if (domainObject.isPresent()) {
      this.domainObjectId = domainObject.get().getId();
      this.domainObjectClass = getDomainObjectName(domainObject.get().getClass());
      this.description = domainObject.get().getLabel();
      this.serializedObject = serializeObject(domainObject.get());
    }
    else {
      this.domainObjectId = null;
      this.description = null;
      this.domainObjectClass = null;
      this.serializedObject = null;
    }

    oldReservationStatus = oldStatus.orNull();
    newReservationStatus = newStatus.orNull();
  }

  public static String getDomainObjectName(Class<? extends Loggable> domainClass) {
    return domainClass.getSimpleName();
  }

  public static String getStateChangeMessage(final Reservation reservation, final ReservationStatus oldStatus) {
    return reservation.getLabel() + ": changed state from [" + oldStatus
        + getStateChangeMessageNewStatusPart(reservation.getStatus());
  }

  public static String getStateChangeMessageNewStatusPart(final ReservationStatus status) {
    return "] to [" + status + "]";
  }

  public static String[] getStateChangeMessageNewStatusPart(final ReservationStatus... states) {
    List<String> statusMessages = new ArrayList<>();
    for (ReservationStatus status : states) {
      statusMessages.add(getStateChangeMessageNewStatusPart(status));
    }

    return statusMessages.toArray(new String[statusMessages.size()]);
  }

  public DateTime getCreated() {
    return created;
  }

  /**
   * Includes seconds in the string since the default conversion does not.
   *
   * @return Time stamp formatted like
   *         {@link WebUtils#DEFAULT_DATE_TIME_FORMATTER}
   */
  public String getCreatedAsText() {
    return created != null ? created.toString(WebUtils.DEFAULT_DATE_TIME_FORMATTER) : "";
  }

  public String getAdminGroup() {
    return StringUtils.deleteAny(adminGroup, "[]");
  }

  public String getShortAdminGroup() {
    return WebUtils.shortenAdminGroup(getAdminGroup());
  }

  public String getUserId() {
    return userId;
  }

  public String getDescription() {
    return description;
  }

  public String getDomainObjectClass() {
    return domainObjectClass;
  }

  public Long getDomainObjectId() {
    return domainObjectId;
  }

  public String getSerializedObject() {
    return serializedObject;
  }

  public String getDetails() {
    return details;
  }

  public LogEventType getEventType() {
    return eventType;
  }

  /**
   * Used to relate logEvents which originates out of a List
   *
   * @return String CorreleationId
   */
  public String getCorrelationId() {
    return correlationId;
  }

  @Override
  public Long getId() {
    return id;
  }

  /**
   * Used to relate logEvents which originates out of a List
   *
   * @param correlationId
   */
  public void setCorrelationId(String correlationId) {
    this.correlationId = correlationId;
  }

  public ReservationStatus getOldReservationStatus() {
    return oldReservationStatus;
  }

  public ReservationStatus getNewReservationStatus() {
    return newReservationStatus;
  }

  public String getEventTypeWithCorrelationId() {
    return StringUtils.hasText(correlationId) ? StringUtils.capitalize(eventType.name().toLowerCase()).concat(" ")
        .concat(correlationId) : StringUtils.capitalize(eventType.name().toLowerCase());
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("LogEvent [");
    if (id != null) {
      builder.append("id=");
      builder.append(id);
      builder.append(", ");
    }
    if (created != null) {
      builder.append("created=");
      builder.append(created);
      builder.append(", ");
    }
    if (userId != null) {
      builder.append("userId=");
      builder.append(userId);
      builder.append(", ");
    }
    if (adminGroup != null) {
      builder.append("adminGroup=");
      builder.append(adminGroup);
      builder.append(", ");
    }
    if (eventType != null) {
      builder.append("eventType=");
      builder.append(eventType);
      builder.append(", ");
    }
    if (domainObjectClass != null) {
      builder.append("domainObjectClass=");
      builder.append(domainObjectClass);
      builder.append(", ");
    }
    if (description != null) {
      builder.append("description=");
      builder.append(description);
      builder.append(", ");
    }
    if (serializedObject != null) {
      builder.append("serializedObject=");
      builder.append(serializedObject);
      builder.append(", ");
    }
    if (details != null) {
      builder.append("details=");
      builder.append(details);
      builder.append(", ");
    }
    if (correlationId != null) {
      builder.append("correlationId=");
      builder.append(correlationId);
      builder.append(", ");
    }
    if (domainObjectId != null) {
      builder.append("domainObjectId=");
      builder.append(domainObjectId);
      builder.append(", ");
    }
    if (oldReservationStatus != null) {
      builder.append("oldReservationStatus=");
      builder.append(oldReservationStatus);
      builder.append(", ");
    }
    if (newReservationStatus != null) {
      builder.append("newReservationStatus=");
      builder.append(newReservationStatus);
    }
    builder.append("]");
    return builder.toString();
  }

  private String serializeObject(Object domainObject) {
    return domainObject.toString();
  }

}