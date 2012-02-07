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
import org.joda.time.Days;
import org.joda.time.LocalDateTime;

import com.google.common.base.Preconditions;

@Entity
public class ActivationEmailLink<T> {

  public static final int VALID_PERIOD_DAYS = 5;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Version
  private Integer version;

  @NotNull
  @Column(nullable = false)
  private final String uuid;

  @NotNull
  @Type(type = "org.joda.time.contrib.hibernate.PersistentLocalDateTime")
  @Column(nullable = false)
  private LocalDateTime emailSentDateTime;

  @NotNull
  @Type(type = "org.joda.time.contrib.hibernate.PersistentLocalDateTime")
  @Column(nullable = false)
  private LocalDateTime activationDateTime;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private final ActivationRequestSource requestSource;

  @NotNull
  @Column(nullable = false)
  private final Long sourceId;

  @Transient
  private T sourceObject;

  @SuppressWarnings("unchecked")
  public ActivationEmailLink(PhysicalResourceGroup physicalResourceGroup) {
    this((T) physicalResourceGroup, ActivationRequestSource.PHYSICAL_RESOURCE_GROUP, physicalResourceGroup.getId());
    physicalResourceGroup.setActive(false);
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

  public LocalDateTime getEmailSentDateTime() {
    return emailSentDateTime;
  }

  public void setActivationDateTime(LocalDateTime activationDateTime) {
    this.activationDateTime = activationDateTime;
  }

  public LocalDateTime getActivationDateTime() {
    return activationDateTime;
  }

  public ActivationRequestSource getRequestSource() {
    return requestSource;
  }

  public Long getSourceId() {
    return sourceId;
  }

  public void activate() {
    this.activationDateTime = LocalDateTime.now();
  }

  public boolean isActivated() {
    return activationDateTime != null;
  }

  public void emailWasSent() {
    this.emailSentDateTime = LocalDateTime.now();
  }

  public boolean isEmailSent() {
    return emailSentDateTime != null;
  }

  public T getSourceObject() {
    return sourceObject;
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
      daysBetween = Days.daysBetween(emailSentDateTime, LocalDateTime.now());
    }

    return !isActivated() && (daysBetween != null) && (daysBetween.getDays() <= VALID_PERIOD_DAYS);
  }
}
