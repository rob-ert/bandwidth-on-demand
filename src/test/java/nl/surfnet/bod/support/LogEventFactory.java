/**
 * Copyright (c) 2012, SURFnet BV
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
package nl.surfnet.bod.support;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

import nl.surfnet.bod.domain.Loggable;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.event.LogEvent;
import nl.surfnet.bod.event.LogEventType;

import org.joda.time.DateTime;
import org.springframework.util.ReflectionUtils;

import com.google.common.base.Optional;

public class LogEventFactory {

  private Long id = null;
  private Integer version = 0;

  private String userId = "test";
  private Collection<String> adminGroups = new ArrayList<>();
  private final String details = "details go here";
  private DateTime created = DateTime.now();
  private LogEventType eventType = LogEventType.UPDATE;
  private Loggable domainObject = null;
  private Long domainObjectId = null;
  private ReservationStatus oldReservationStatus = ReservationStatus.REQUESTED;
  private ReservationStatus newReservationStatus = ReservationStatus.RESERVED;

  public LogEvent create() {
    if (domainObject != null) {
      this.adminGroups = domainObject.getAdminGroups();
    }
    LogEvent logEvent = new LogEvent(userId, adminGroups, eventType, Optional.<Loggable> fromNullable(domainObject),
        details, Optional.<ReservationStatus> fromNullable(oldReservationStatus), Optional
            .<ReservationStatus> fromNullable(newReservationStatus));

    // Set immutable createdField
    Field createdField = ReflectionUtils.findField(LogEvent.class, "created");
    createdField.setAccessible(true);
    ReflectionUtils.setField(createdField, logEvent, created);

    return logEvent;
  }

  public String getUserId() {
    return userId;
  }

  public LogEventFactory setUserId(String userId) {
    this.userId = userId;
    return this;
  }

  public LogEventFactory setAdminGroups(Collection<String> adminGroups) {
    this.adminGroups = adminGroups;
    return this;
  }

  public DateTime getCreated() {
    return created;
  }

  public LogEventFactory setCreated(DateTime created) {
    this.created = created;
    return this;
  }

  public LogEventType getEventType() {
    return eventType;
  }

  public LogEventFactory setEventType(LogEventType eventType) {
    this.eventType = eventType;
    return this;
  }

  public Loggable getDomainObject() {
    return domainObject;
  }

  public LogEventFactory setDomainObject(Loggable domainObject) {
    this.domainObject = domainObject;
    return this;
  }

  public Long getDomainObjectId() {
    return domainObjectId;
  }

  public LogEventFactory setDomainObjectId(Long domainObjectId) {
    this.domainObjectId = domainObjectId;
    return this;
  }

  public ReservationStatus getOldReservationStatus() {
    return oldReservationStatus;
  }

  public LogEventFactory setOldReservationStatus(ReservationStatus oldReservationStatus) {
    this.oldReservationStatus = oldReservationStatus;
    return this;
  }

  public ReservationStatus getNewReservationStatus() {
    return newReservationStatus;
  }

  public LogEventFactory setNewReservationStatus(ReservationStatus newReservationStatus) {
    this.newReservationStatus = newReservationStatus;
    return this;
  }

  public Long getId() {
    return id;
  }

  public LogEventFactory setId(Long id) {
    this.id = id;
    return this;
  }

  public Integer getVersion() {
    return version;
  }

  public LogEventFactory setVersion(Integer version) {
    this.version = version;
    return this;
  }
}
