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

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.Days;

import com.google.common.base.Preconditions;

@Entity
public class ActivationEmailLink<T> implements Loggable {

  public static final int VALID_PERIOD_DAYS = 5;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Version
  private Integer version;

  @NotNull
  @Column(nullable = false)
  private String uuid;

  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
  private DateTime emailSentDateTime;

  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
  private DateTime activationDateTime;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ActivationRequestSource requestSource;

  @NotNull
  @Column(nullable = false)
  private Long sourceId;

  @NotNull
  @Column(nullable = false)
  private String toEmail;

  @Transient
  private T sourceObject;

  public ActivationEmailLink() {
  }

  @SuppressWarnings("unchecked")
  public ActivationEmailLink(PhysicalResourceGroup physicalResourceGroup) {
    this((T) physicalResourceGroup, ActivationRequestSource.PHYSICAL_RESOURCE_GROUP, physicalResourceGroup.getId());

    this.toEmail = physicalResourceGroup.getManagerEmail();
  }

  private ActivationEmailLink(T sourceObject, ActivationRequestSource activationRequestSource, Long sourceId) {
    Preconditions.checkArgument(sourceId != null);

    this.requestSource = activationRequestSource;
    this.sourceObject = sourceObject;
    this.sourceId = sourceId;
    this.uuid = UUID.randomUUID().toString();
  }

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

  public String getUuid() {
    return uuid;
  }

  public DateTime getEmailSentDateTime() {
    return emailSentDateTime;
  }

  void setEmailSentDateTime(DateTime emailSentDateTime) {
    this.emailSentDateTime = emailSentDateTime;
  }

  public DateTime getActivationDateTime() {
    return activationDateTime;
  }

  public ActivationRequestSource getRequestSource() {
    return requestSource;
  }

  public Long getSourceId() {
    return sourceId;
  }

  public String getToEmail() {
    return toEmail;
  }

  public void activate() {
    this.activationDateTime = DateTime.now();
  }

  public boolean isActivated() {
    return activationDateTime != null;
  }

  public void emailWasSent() {
    this.emailSentDateTime = DateTime.now();
  }

  public boolean isEmailSent() {
    return emailSentDateTime != null;
  }

  public T getSourceObject() {
    return sourceObject;
  }

  public void setSourceObject(T sourceObject) {
    this.sourceObject = sourceObject;
  }

  public DateTime getExpirationDateTime() {
    return emailSentDateTime != null ? emailSentDateTime.plusDays(VALID_PERIOD_DAYS) : null;
  }

  @Override
  public String getAdminGroup() {
    return sourceObject != null ? ((Loggable) sourceObject).getAdminGroup() : null;
  }
  
  @Override
  public String getLabel() {
   return "To: " + getToEmail();
  }

  /**
   * This link is valid when the activationEmail was sent, this link is not
   * activated yet and the email was sent within the last
   * {@link #VALID_PERIOD_DAYS}
   * 
   * @return true if valid, false otherwise
   */
  public boolean isValid() {
    Days daysBetween = null;

    if (isEmailSent()) {
      daysBetween = Days.daysBetween(emailSentDateTime, DateTime.now());
    }

    return !isActivated() && daysBetween != null && daysBetween.getDays() <= VALID_PERIOD_DAYS;
  }
}
