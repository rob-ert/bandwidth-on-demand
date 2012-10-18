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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.Iterators;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.domain.Loggable;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.event.LogEvent;
import nl.surfnet.bod.event.LogEventType;
import nl.surfnet.bod.event.LogEvent_;
import nl.surfnet.bod.repo.CustomLogEventRepo;
import nl.surfnet.bod.repo.LogEventRepo;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

@Service
public class LogEventService extends AbstractFullTextSearchService<LogEvent> {

  private static final String SYSTEM_USER = "system";

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Resource
  private LogEventRepo logEventRepo;

  @Resource
  private CustomLogEventRepo customLogEventRepo;

  @Resource
  private Environment environment;

  @Resource
  private ManagerService managerService;

  @PersistenceContext
  private EntityManager entityManager;

  private static Specification<LogEvent> specLogEventsByAdminGroups(final Collection<String> adminGroups) {
    return new Specification<LogEvent>() {
      @Override
      public javax.persistence.criteria.Predicate toPredicate(Root<LogEvent> root, CriteriaQuery<?> query,
          CriteriaBuilder cb) {
        return cb.and(root.get(LogEvent_.adminGroup).in(adminGroups));
      }
    };
  }

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
    if (domainObject != null) {
      return domainObject.getAdminGroup();
    }
    else if (user.isSelectedManagerRole()) {
      return user.getSelectedRole().getAdminGroup().get();
    }
    else if (user.isSelectedNocRole()) {
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
      logEvent.setCorrelationId(String.valueOf(index) + "-" + String.valueOf(size));

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
    Reservation.class.getSimpleName(), //
        VirtualPort.class.getSimpleName(), //
        PhysicalPort.class.getSimpleName(), //
        Institute.class.getSimpleName() };

    for (String clazz : supportedClasses) {
      if ((logEvent.getDescription() != null) && (logEvent.getDescription().contains(clazz))) {
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
    final Set<String> adminGroups = managerService.findAllAdminGroupsForManager(Security.getSelectedRole());

    if (selectedRole.isManagerRole()) {
      return customLogEventRepo.findIdsWithWhereClause(Optional.of(specLogEventsByAdminGroups(adminGroups)));
    }
    else if (selectedRole.isNocRole()) {
      return customLogEventRepo.findIdsWithWhereClause(Optional.<Specification<LogEvent>> absent());
    }
    
    return new ArrayList<>();

  }

  public List<Long> findIdsForUser(List<String> determinGroupsToSearchFor) {
    return customLogEventRepo
        .findIdsWithWhereClause(Optional.of(specLogEventsByAdminGroups(determinGroupsToSearchFor)));
  }

}