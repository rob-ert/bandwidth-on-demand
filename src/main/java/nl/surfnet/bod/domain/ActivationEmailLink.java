/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
public class ActivationEmailLink implements Loggable {

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
  private PhysicalResourceGroup sourceObject;

  public ActivationEmailLink() {
  }

  public ActivationEmailLink(PhysicalResourceGroup physicalResourceGroup) {
    this(physicalResourceGroup, ActivationRequestSource.PHYSICAL_RESOURCE_GROUP, physicalResourceGroup.getId());

    this.toEmail = physicalResourceGroup.getManagerEmail();
  }

  private ActivationEmailLink(PhysicalResourceGroup sourceObject, ActivationRequestSource activationRequestSource, Long sourceId) {
    Preconditions.checkArgument(sourceId != null);

    this.requestSource = activationRequestSource;
    this.sourceObject = sourceObject;
    this.sourceId = sourceId;
    this.uuid = UUID.randomUUID().toString();
  }

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

  public PhysicalResourceGroup getSourceObject() {
    return sourceObject;
  }

  public void setSourceObject(PhysicalResourceGroup sourceObject) {
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
