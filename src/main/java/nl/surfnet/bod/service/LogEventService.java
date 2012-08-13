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
package nl.surfnet.bod.service;

import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nl.surfnet.bod.event.LogEvent;
import nl.surfnet.bod.event.LogEventType;
import nl.surfnet.bod.repo.LogEventRepo;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;

@Service
public class LogEventService extends AbstractFullTextSearchService<LogEvent, LogEvent> {

  private static final String SYSTEM_USER = "system";
  private static final String ALL_GROUPS = "all";

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Resource
  private LogEventRepo logEventRepo;

  @PersistenceContext
  private EntityManager entityManager;

  public void logCreateEvent(Object domainObject) {
    logCreateEvent(null, domainObject);
  }

  public void logCreateEvent(Object domainObject, String details) {
    logCreateEvent(null, domainObject, details);
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

  public void logUpdateEvent(Object domainObject, String details) {
    logUpdateEvent(null, domainObject, details);
  }

  public void logUpdateEvent(RichUserDetails user, Object domainObject) {
    logUpdateEvent(user, domainObject, null);
  }

  public void logUpdateEvent(RichUserDetails user, Object domainObject, String details) {
    handleEvent(createLogEvent(user, LogEventType.UPDATE, domainObject, details));
  }

  public void logDeleteEvent(Object domainObject) {
    logDeleteEvent(null, domainObject);
  }

  public void logDeleteEvent(Object domainObject, String details) {
    logDeleteEvent(null, domainObject, details);
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
      return new LogEvent(user.getUsername(), user.getUserGroupIds(), eventType, domainObject, details);
    }
  }

  public List<LogEvent> findAll(int firstResult, int maxResults, Sort sort) {
    return logEventRepo.findAll(new PageRequest(firstResult / maxResults, maxResults, sort)).getContent();
  }

  public long count() {
    return logEventRepo.count();
  }

  /**
   * Handles the event. Writes it to the given logger and persists it in the
   * {@link LogEventRepo}
   * 
   * @param logger
   *          Logger to write to
   * 
   * @param logEvent
   *          LogEvent to handle
   */
  @VisibleForTesting
  void handleEvent(Logger log, LogEvent logEvent) {
    log.info("Handling event: {}", logEvent);

    logEventRepo.save(logEvent);
  }

  @Override
  protected EntityManager getEntityManager() {
    return entityManager;
  }

  /**
   * Delegates to {@link #handleEvent(Logger, LogEvent)}
   * 
   * @param logEvent
   */
  private void handleEvent(LogEvent logEvent) {
    handleEvent(logger, logEvent);
  }

  @Override
  public List<LogEvent> transformToView(List<LogEvent> listToTransform, RichUserDetails user) {
    return listToTransform;
  }

}