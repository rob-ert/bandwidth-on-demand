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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import nl.surfnet.bod.domain.Loggable;
import nl.surfnet.bod.web.WebUtils;

import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.joda.time.LocalDateTime;
import org.springframework.util.StringUtils;

import com.google.common.annotations.VisibleForTesting;

@Entity
@Indexed
public class LogEvent {

  @VisibleForTesting
  static final String LIST_STRING = "List of %d %s(s)";

  @VisibleForTesting
  static final String LIST_EMPTY = "Empty list";

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
  private final LocalDateTime created;

  @Field
  @Basic
  private final String userId;

  @Field
  @Basic
  private final String adminGroup;

  @Enumerated(EnumType.STRING)
  private final LogEventType eventType;

  @Field
  @Column(nullable = true)
  private final String description;

  @Field
  @Type(type = "text")
  private final String serializedObject;

  @Field
  @Type(type = "text")
  private final String details;

  @Field
  @Basic
  private String correlationId;

  /**
   * Default constructor for Hibernate
   */
  @SuppressWarnings("unused")
  private LogEvent() {
    this((String) null, (String) null, (LogEventType) null, null);
  }

  public LogEvent(String userId, String adminGroup, LogEventType type, Loggable domainObject) {
    this(userId, adminGroup, type, domainObject, null);
  }

  public LogEvent(String userId, String adminGroup, LogEventType type, Loggable domainObject, String details) {
    super();
    this.userId = userId;
    this.adminGroup = adminGroup;
    this.eventType = type;
    this.details = details;

    this.created = LocalDateTime.now();

    if (domainObject == null) {
      this.description = null;
      this.serializedObject = null;
    }
    else {
      this.description = new StringBuffer(domainObject.getClass().getSimpleName()).append(": ")
          .append(domainObject.getLabel()).toString();
      this.serializedObject = serializeObject(domainObject);
    }
  }

  private String serializeObject(Object domainObject) {
    return domainObject.toString();
  }

  public LocalDateTime getCreated() {
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

  /**
   * Used to relate logEvents which originates out of a List
   * 
   * @param correlationId
   */
  public void setCorrelationId(String correlationId) {
    this.correlationId = correlationId;
  }

  public String getEventTypeWithCorrelationId() {
    return (StringUtils.hasText(correlationId) ? StringUtils.capitalize(eventType.name().toLowerCase()).concat(" ")
        .concat(correlationId) : StringUtils.capitalize(eventType.name().toLowerCase()));
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
    }
    builder.append("]");
    return builder.toString();
  }

}