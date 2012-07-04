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

import java.util.Collection;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Type;
import org.joda.time.LocalDateTime;

import com.google.common.collect.Lists;

@Entity
public class LogEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
  private final LocalDateTime created;

  @Basic
  private final String userId;

  @Basic
  private final String groupIds;

  @Enumerated(EnumType.STRING)
  private final LogEventType eventType;

  @Column(nullable = true)
  private final String className;

  @Type(type = "text")
  private final String serializedObject;

  @Type(type = "text")
  private final String details;

  /**
   * Default constructor for Hibernate
   */
  @SuppressWarnings("unused")
  private LogEvent() {
    this((String) null, (String) null, (LogEventType) null, null);
  }

  public LogEvent(String userId, String groupId, LogEventType type, Object domainObject) {
    this(userId, Lists.newArrayList(groupId), type, domainObject);
  }

  public LogEvent(String userId, String groupId, LogEventType type, Object domainObject, String details) {
    this(userId, Lists.newArrayList(groupId), type, domainObject, details);
  }

  public LogEvent(String userId, Collection<String> groupIds, LogEventType type, Object domainObject) {
    this(userId, groupIds, type, domainObject, null);
  }

  public LogEvent(String userId, Collection<String> groupIds, LogEventType type, Object domainObject, String details) {
    super();
    this.userId = userId;
    this.groupIds = groupIds != null ? groupIds.toString() : null;
    this.eventType = type;
    this.details = details;

    this.created = LocalDateTime.now();

    if (domainObject == null) {
      this.className = null;
      this.serializedObject = null;
    }
    else {
      this.className = domainObject.getClass().getSimpleName();
      this.serializedObject = serializeObject(domainObject);
    }
  }

  private String serializeObject(Object domainObject) {
    return domainObject.toString();
  }

  public LocalDateTime getCreated() {
    return created;
  }

  public String getGroupIds() {
    return groupIds;
  }

  public String getUserId() {
    return userId;
  }

  public LogEventType getEventType() {
    return eventType;
  }

  public String getClassName() {
    return className;
  }

  public String getSerializedObject() {
    return serializedObject;
  }

  public String getDetails() {
    return details;
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
    if (groupIds != null) {
      builder.append("groupIds=");
      builder.append(groupIds);
      builder.append(", ");
    }
    if (userId != null) {
      builder.append("userId=");
      builder.append(userId);
      builder.append(", ");
    }
    if (eventType != null) {
      builder.append("eventType=");
      builder.append(eventType);
      builder.append(", ");
    }
    if (className != null) {
      builder.append("className=");
      builder.append(className);
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
    }
    builder.append("]");
    return builder.toString();
  }

}