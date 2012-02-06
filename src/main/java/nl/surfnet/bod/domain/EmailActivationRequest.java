package nl.surfnet.bod.domain;

import static com.google.common.base.Preconditions.checkState;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;
import org.joda.time.LocalDateTime;

/**
 * Domain object which is responsible for the administration of the activation
 * of email adresses
 * 
 */
//@Entity
public class EmailActivationRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Nonnull
  @Enumerated(EnumType.STRING)
  private final ActivationRequestSource requestSource;

  @Nonnull
  private final Long sourceId;

  @Nonnull
  private final String uuid;

  @Nullable
  @Type(type = "org.joda.time.contrib.hibernate.PersistentLocalDateTime")
  private LocalDateTime emailSentTimeStamp;

  @Nullable
  @Type(type = "org.joda.time.contrib.hibernate.PersistentLocalDateTime")
  private LocalDateTime activationTimeStamp;

  public EmailActivationRequest(ActivationRequestSource activationRequestSource, Long activationSourceId) {
    this.requestSource = activationRequestSource;
    this.sourceId = activationSourceId;
    this.uuid = UUID.randomUUID().toString();
    this.emailSentTimeStamp = null;
    this.activationTimeStamp = null;
  }

  public Long getId() {
    return id;
  }

  public ActivationRequestSource getRequestSource() {
    return requestSource;
  }

  public Long getSourceId() {
    return sourceId;
  }

  public String getUuid() {
    return uuid;
  }

  public LocalDateTime getEmailSentTimeStamp() {
    return emailSentTimeStamp;
  }

  public LocalDateTime getActivationTimeStamp() {
    return activationTimeStamp;
  }

  public void emailSent() {
    checkState(emailSentTimeStamp == null);
    emailSentTimeStamp = LocalDateTime.now();
  }

  public void activate() {
    checkState(activationTimeStamp == null);
    this.activationTimeStamp = LocalDateTime.now();
  }
}
