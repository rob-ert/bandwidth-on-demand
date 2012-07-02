package nl.surfnet.bod.service;

import nl.surfnet.bod.event.LogEvent;
import nl.surfnet.bod.event.LogEventType;
import nl.surfnet.bod.repo.LogEventRepo;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;

@Service
public class LogEventService {

  private static final String SYSTEM_USER = "system";
  private static final String ALL_GROUPS = "all";

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private LogEventRepo logEventRepo;

  public void logCreateEvent(Object domainObject) {
    logCreateEvent(null, domainObject);
  }

  public void logCreateEvent(RichUserDetails user, Object domainObject) {
    logCreateEvent(user, domainObject, null);
  }

  public void logCreateEvent(RichUserDetails user, Object domainObject, String details) {
    handleEvent(createLogEvent(user, LogEventType.CREATE, domainObject, details));
  }

  public void logReadEvent(Object domainObject) {
    logReadEvent(null, domainObject);
  }

  public void logReadEvent(RichUserDetails user, Object domainObject) {
    logReadEvent(user, domainObject, null);
  }

  public void logReadEvent(RichUserDetails user, Object domainObject, String details) {
    handleEvent(createLogEvent(user, LogEventType.READ, domainObject, details));
  }

  public void logUpdateEvent(Object domainObject) {
    logUpdateEvent(null, domainObject);
  }

  public void logUpdateEvent(RichUserDetails user, Object domainObject) {
    logUpdateEvent(user, domainObject, null);
  }

  public void logUpdateEvent(RichUserDetails user, Object domainObject, String details) {
    handleEvent(createLogEvent(user, LogEventType.UPDATE, domainObject, details));
  }

  public void logDeleteEvent(Object domainObject) {
    logUpdateEvent(null, domainObject);
  }

  public void logDeleteEvent(RichUserDetails user, Object domainObject) {
    logDeleteEvent(user, domainObject, null);
  }

  public void logDeleteEvent(RichUserDetails user, Object domainObject, String details) {
    handleEvent(createLogEvent(user, LogEventType.DELETE, domainObject, details));
  }

  @VisibleForTesting
  LogEvent createLogEvent(RichUserDetails user, LogEventType eventType, Object domainObject, String details) {
    if (user == null) {
      return new LogEvent(SYSTEM_USER, ALL_GROUPS, eventType, domainObject, details);
    }
    else {
      return new LogEvent(user.getUsername(), user.getUserGroupIds(), eventType, domainObject);
    }
  }

  @VisibleForTesting
  void handleEvent(LogEvent logEvent) {
    logger.info("Handling event: {}", logEvent);
    logEventRepo.save(logEvent);
  }
}
