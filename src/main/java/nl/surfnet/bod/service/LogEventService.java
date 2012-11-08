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

import static nl.surfnet.bod.service.LogEventPredicatesAndSpecifications.specLogEventsByAdminGroups;
import static nl.surfnet.bod.service.LogEventPredicatesAndSpecifications.specLogEventsByDomainClassAndCreatedBetween;

import java.util.*;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nl.surfnet.bod.domain.*;
import nl.surfnet.bod.event.LogEvent;
import nl.surfnet.bod.event.LogEventType;
import nl.surfnet.bod.repo.LogEventRepo;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.Iterators;

@Service
public class LogEventService extends AbstractFullTextSearchService<LogEvent> {

  private static final String SYSTEM_USER = "System";

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Resource
  private LogEventRepo logEventRepo;

  @Resource(name = "bodEnvironment")
  private Environment environment;

  @Resource
  private ManagerService managerService;

  @PersistenceContext
  private EntityManager entityManager;

  public void logCreateEvent(RichUserDetails user, Loggable domainObject) {
    logCreateEvent(user, domainObject, null);
  }

  public void logCreateEvent(RichUserDetails user, Collection<? extends Loggable> domainObjects, String details) {
    handleEvents(createLogEvents(user, domainObjects, LogEventType.CREATE, details));
  }

  public void logCreateEvent(RichUserDetails user, Loggable domainObject, String details) {
    handleEvent(createLogEvent(user, LogEventType.CREATE, domainObject, details));
  }

  public void logReadEvent(RichUserDetails user, Collection<? extends Loggable> domainObjects, String details) {
    handleEvents(createLogEvents(user, domainObjects, LogEventType.READ, details));
  }

  public void logReadEvent(RichUserDetails user, Loggable domainObject, String details) {
    handleEvent(createLogEvent(user, LogEventType.READ, domainObject, details));
  }

  public void logUpdateEvent(RichUserDetails user, Loggable domainObject) {
    logUpdateEvent(user, domainObject, null);
  }

  public void logUpdateEvent(RichUserDetails user, Collection<? extends Loggable> domainObjects, String details) {
    handleEvents(createLogEvents(user, domainObjects, LogEventType.UPDATE, details));
  }

  public void logUpdateEvent(RichUserDetails user, Loggable domainObject, String details) {
    handleEvent(createLogEvent(user, LogEventType.UPDATE, domainObject, details));
  }

  public void logDeleteEvent(RichUserDetails user, Loggable domainObject) {
    logDeleteEvent(user, domainObject, null);
  }

  public void logDeleteEvent(RichUserDetails user, Collection<? extends Loggable> domainObject, String details) {
    handleEvents(createLogEvents(user, domainObject, LogEventType.DELETE, details));
  }

  public void logDeleteEvent(RichUserDetails user, Loggable domainObject, String details) {
    handleEvent(createLogEvent(user, LogEventType.DELETE, domainObject, details));
  }

  public List<LogEvent> findAll(int firstResult, int maxResults, Sort sort) {
    return logEventRepo.findAll(WebUtils.createPageRequest(firstResult, maxResults, sort)).getContent();
  }

  public long count() {
    return logEventRepo.count();
  }

  public List<LogEvent> findByAdminGroups(Collection<String> adminGroups, int firstResult, int maxResults, Sort sort) {
    if (adminGroups.isEmpty()) {
      return Collections.emptyList();
    }

    return logEventRepo.findAll(specLogEventsByAdminGroups(adminGroups),
        WebUtils.createPageRequest(firstResult, maxResults, sort)).getContent();
  }

  public List<LogEvent> findByManagerRole(BodRole managerRole, int firstResult, int maxResults, Sort sort) {
    final Set<String> groupsForManager = managerService.findAllAdminGroupsForManager(managerRole);

    return findByAdminGroups(groupsForManager, firstResult, maxResults, sort);
  }

  @Override
  protected EntityManager getEntityManager() {
    return entityManager;
  }

  @VisibleForTesting
  String determineAdminGroup(RichUserDetails user, Loggable domainObject) {
    if ((domainObject != null) && (StringUtils.hasText(domainObject.getAdminGroup()))) {
      return domainObject.getAdminGroup();
    }

    if (user == null) {
      return environment.getNocGroup();
    }

    if (user.isSelectedManagerRole()) {
      return user.getSelectedRole().getAdminGroup().get();
    }

    if (user.isSelectedNocRole()) {
      return environment.getNocGroup();
    }

    throw new IllegalStateException("Could not determine adminGroup for user: " + user);
  }

  @VisibleForTesting
  LogEvent createLogEvent(RichUserDetails user, LogEventType eventType, Loggable domainObject, String details) {

    return new LogEvent(user == null ? SYSTEM_USER : user.getUsername(), determineAdminGroup(user, domainObject),
        eventType, domainObject, details);
  }

  /**
   * Handles the event. Writes it to the given logger. Only events with a
   * domainObject with one a specific type, as determined by
   * {@link #shouldLogEventBePersisted(LogEvent)} are persisted to the
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

    if (shouldLogEventBePersisted(logEvent)) {
      logEventRepo.save(logEvent);
    }
  }

  @VisibleForTesting
  List<LogEvent> createLogEvents(RichUserDetails user, Collection<? extends Loggable> domainObjects,
      LogEventType logEventType, String details) {
    List<LogEvent> logEvents = new ArrayList<>();
    int size = Iterators.size((domainObjects).iterator());

    Iterator<? extends Loggable> it = (domainObjects).iterator();
    int index = 0;
    while (it.hasNext()) {
      index++;
      Loggable object = it.next();

      LogEvent logEvent = createLogEvent(user, logEventType, object, details);

      // Relate list items
      logEvent.setCorrelationId(String.valueOf(index) + "/" + String.valueOf(size));

      logEvents.add(logEvent);
    }

    return logEvents;
  }

  /**
   * Determines if an Event should be persisted. Only the following types are
   * supported:
   * <ul>
   * <li>Reservation</li>
   * <li>PhysicalPort</li>
   * <li>VirtualPort</li>
   * <li>Institute</li>
   * </ul>
   *
   * @param logEvent
   * @return true when the {@link LogEvent#getDescription()} matches one of the
   *         listed above, false otherwise.
   */
  private boolean shouldLogEventBePersisted(LogEvent logEvent) {
    String[] supportedClasses = { //
    LogEvent.getDomainObjectName(Reservation.class), //
        LogEvent.getDomainObjectName(VirtualPort.class), //
        LogEvent.getDomainObjectName(PhysicalPort.class), //
        LogEvent.getDomainObjectName(Institute.class) };

    for (String clazz : supportedClasses) {
      if (clazz.equals(logEvent.getDomainObjectClass())) {
        return true;
      }
    }
    return false;
  }

  private void handleEvents(List<LogEvent> logEvents) {
    for (LogEvent logEvent : logEvents) {
      handleEvent(logEvent);
    }
  }

  /**
   * Delegates to {@link #handleEvent(Logger, LogEvent)}
   *
   * @param logEvent
   */
  private void handleEvent(LogEvent logEvent) {
    handleEvent(logger, logEvent);
  }

  public long countByAdminGroups(Collection<String> adminGroups) {
    if (adminGroups.isEmpty()) {
      return 0;
    }

    return logEventRepo.count(specLogEventsByAdminGroups(adminGroups));
  }

  public List<Long> findIdsForManagerOrNoc(RichUserDetails userDetails) {
    final BodRole selectedRole = userDetails.getSelectedRole();

    if (selectedRole.isManagerRole()) {
      Set<String> adminGroups = managerService.findAllAdminGroupsForManager(Security.getSelectedRole());
      return logEventRepo.findIdsWithWhereClause(Optional.of(specLogEventsByAdminGroups(adminGroups)));
    }
    else if (selectedRole.isNocRole()) {
      return logEventRepo.findIdsWithWhereClause(Optional.<Specification<LogEvent>> absent());
    }

    return Collections.emptyList();
  }

  public List<Long> findIdsForUser(List<String> determinGroupsToSearchFor) {
    return logEventRepo.findIdsWithWhereClause(Optional.of(specLogEventsByAdminGroups(determinGroupsToSearchFor)));
  }

  public List<LogEvent> findByDomainClassCreatedBetweenForNoc(final Class<? extends Loggable> domainClass,
      DateTime start, DateTime end) {

    return logEventRepo.findAll(specLogEventsByDomainClassAndCreatedBetween(domainClass, start, end));
  }

  public List<Long> findDomainObjectIdsByDomainClassCreatedBetweenForNoc(final Class<? extends Loggable> domainClass,
      DateTime start, DateTime end) {

    return logEventRepo.findDistinctDomainObjectIdsWithWhereClause(LogEventPredicatesAndSpecifications
        .specLogEventsByDomainClassAndCreatedBetween(domainClass, start, end));
  }

  public List<Long> findDomainObjectIdsByDomainClassCreatedBetweenForNocWithState(
      final Class<? extends Loggable> domainClass, DateTime start, DateTime end, ReservationStatus state) {

    Specification<LogEvent> spec = LogEventPredicatesAndSpecifications
        .specLogEventsByDomainClassAndDescriptionPartBetween(domainClass, start, end, LogEvent
            .getStateChangeMessageNewStatusPart(state));

    return logEventRepo.findDistinctDomainObjectIdsWithWhereClause(spec);
  }

  public long count(Specification<LogEvent> whereClause) {
    return logEventRepo.count(whereClause);
  }

  public long countDistinctDomainObjectId(Specification<LogEvent> whereClause) {
    return logEventRepo.countDistinctDomainObjectIdsWithWhereClause(whereClause);
  }

  public List<Long> findDistinctDomainObjectIdsWithWhereClause(Specification<LogEvent> whereClause) {
    return logEventRepo.findDistinctDomainObjectIdsWithWhereClause(whereClause);
  }

}