package nl.surfnet.bod.support;

import nl.surfnet.bod.domain.Loggable;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.event.LogEvent;
import nl.surfnet.bod.event.LogEventType;

import org.joda.time.DateTime;

public class LogEventFactory {

  private Long id = null;
  private Integer version = 0;

  private String userId = "test";
  private String adminGroup = "urn:admin";
  private DateTime created = DateTime.now();
  private LogEventType eventType = LogEventType.UPDATE;
  private Loggable domainObject = new ReservationFactory().create();
  private Long domainObjectId = id;
  private ReservationStatus oldReservationStatus = ReservationStatus.REQUESTED;
  private ReservationStatus newReservationStatus = ReservationStatus.RESERVED;

  public LogEvent create() {
    LogEvent logEvent = new LogEvent(userId, adminGroup, eventType, domainObject);

    return logEvent;
  }

  public String getUserId() {
    return userId;
  }

  public LogEventFactory setUserId(String userId) {
    this.userId = userId;
    return this;
  }

  public String getAdminGroup() {
    return adminGroup;
  }

  public LogEventFactory setAdminGroup(String adminGroup) {
    this.adminGroup = adminGroup;
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
