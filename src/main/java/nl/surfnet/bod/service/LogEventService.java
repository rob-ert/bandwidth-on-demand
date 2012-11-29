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
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.domain.Loggable;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.domain.VirtualPort;
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
import com.google.common.collect.ImmutableList;

import static com.google.common.collect.Iterables.toArray;

import static nl.surfnet.bod.service.LogEventPredicatesAndSpecifications.specLogEventsByAdminGroups;

@Service
public class LogEventService extends AbstractFullTextSearchService<LogEvent> {

  private static final String SYSTEM_USER = "System";
  private static final Collection<String> PERSISTABLE_LOG_EVENTS = ImmutableList.of(LogEvent
      .getDomainObjectName(Reservation.class), LogEvent.getDomainObjectName(VirtualPort.class), LogEvent
      .getDomainObjectName(PhysicalPort.class), LogEvent.getDomainObjectName(PhysicalResourceGroup.class), LogEvent
      .getDomainObjectName(Institute.class));

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Resource
  private LogEventRepo logEventRepo;

  @Resource(name = "bodEnvironment")
  private Environment environment;

  @Resource
  private ManagerService managerService;

  @PersistenceContext
  private EntityManager entityManager;

  public long count() {
    return logEventRepo.count();
  }

  public long count(Specification<LogEvent> whereClause) {
    return logEventRepo.count(whereClause);
  }

  public long countByAdminGroups(Collection<String> adminGroups) {
    if (adminGroups.isEmpty()) {
      return 0;
    }

    return logEventRepo.count(specLogEventsByAdminGroups(adminGroups));
  }

  public long countDistinctDomainObjectId(Specification<LogEvent> whereClause) {
    return logEventRepo.countDistinctDomainObjectIdsWithWhereClause(whereClause);
  }

  public long countStateChangeFromOldToNewForReservationIdBetween(DateTime start, DateTime end,
      ReservationStatus oldStatus, ReservationStatus newStatus, final Collection<String> adminGroups) {

    Specification<LogEvent> whereClause = LogEventPredicatesAndSpecifications
        .specStateChangeFromOldToNewForReservationIdInAdminGroupsBetween(oldStatus, newStatus, null, start, end,
            adminGroups);

    return logEventRepo.countDistinctDomainObjectIdsWithWhereClause(whereClause);
  }

  public List<Long> findStateChangeFromOldToNewForReservationIdInAdminGroupsBetween(DateTime start, DateTime end,
      ReservationStatus oldStatus, ReservationStatus newStatus, final Collection<String> adminGroups) {

    Specification<LogEvent> whereClause = LogEventPredicatesAndSpecifications
        .specStateChangeFromOldToNewForReservationIdInAdminGroupsBetween(oldStatus, newStatus, null, start, end,
            adminGroups);

    return logEventRepo.findDistinctDomainObjectIdsWithWhereClause(whereClause);
  }

  private LogEvent createReservationLogEvent(RichUserDetails user, LogEventType eventType, Reservation reservation,
      ReservationStatus oldStatus) {

    String details = getStateChangeMessage(reservation, oldStatus);
    return createLogEvent(user, eventType, reservation, details, Optional.of(oldStatus), Optional.of(reservation
        .getStatus()));
  }

  private LogEvent createLogEvent(RichUserDetails user, LogEventType eventType, Loggable domainObject, String details,
      Optional<ReservationStatus> oldStatus, Optional<ReservationStatus> newStatus) {

    return new LogEvent(user == null ? SYSTEM_USER : user.getUsername(), determineAdminGroup(user, domainObject),
        eventType, Optional.fromNullable(domainObject), details, oldStatus, newStatus);
  }

  private LogEvent createLogEvent(RichUserDetails user, LogEventType eventType, Loggable domainObject, String details) {
    return createLogEvent(user, eventType, domainObject, details, Optional.<ReservationStatus> absent(), Optional
        .<ReservationStatus> absent());
  }

  private List<LogEvent> createLogEvents(RichUserDetails user, LogEventType logEventType, String details,
      Loggable... domainObjects) {

    List<LogEvent> logEvents = new ArrayList<>();
    int size = domainObjects.length;

    for (int i = 0; i < domainObjects.length; i++) {
      LogEvent logEvent = createLogEvent(user, logEventType, domainObjects[i], details);

      if (size > 1) {
        logEvent.setCorrelationId(String.valueOf(i + 1) + "/" + String.valueOf(size));
      }

      logEvents.add(logEvent);
    }

    return logEvents;
  }

  @VisibleForTesting
  String determineAdminGroup(RichUserDetails user, Loggable domainObject) {
    if (domainObject != null && StringUtils.hasText(domainObject.getAdminGroup())) {
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

  public List<LogEvent> findAll(int firstResult, int maxResults, Sort sort) {
    return logEventRepo.findAll(WebUtils.createPageRequest(firstResult, maxResults, sort)).getContent();
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

  public List<Long> findDistinctDomainObjectIdsWithWhereClause(Specification<LogEvent> whereClause) {
    return logEventRepo.findDistinctDomainObjectIdsWithWhereClause(whereClause);
  }

  public List<Long> findReservationIdsCreatedBetweenForNocWithStateInAdminGroups(DateTime start, DateTime end,
      ReservationStatus state, Collection<String> adminGroups) {

    Specification<LogEvent> spec = LogEventPredicatesAndSpecifications
        .specForReservationBetweenForAdminGroupsWithStateIn(null, start, end, adminGroups, state);

    return logEventRepo.findDistinctDomainObjectIdsWithWhereClause(spec);
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

  public LogEvent findLatestStateChangeForReservationIdBeforeInAdminGroups(Long id, DateTime before,
      final Collection<String> adminGroups) {

    Specification<LogEvent> whereClause = LogEventPredicatesAndSpecifications
        .specForReservationBeforeInAdminGroupsWithStateIn(id, before, adminGroups);

    Long logEventId = logEventRepo.findMaxIdWithWhereClause(whereClause);

    return logEventId == null ? null : logEventRepo.findOne(logEventId);
  }

  @Override
  protected EntityManager getEntityManager() {
    return entityManager;
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
    log.info("Event: {}", logEvent);

    if (shouldLogEventBePersisted(logEvent)) {
      logEventRepo.save(logEvent);
    }
  }

  private void handleEvents(List<LogEvent> logEvents) {
    for (LogEvent logEvent : logEvents) {
      handleEvent(logger, logEvent);
    }
  }

  public Collection<LogEvent> logCreateEvent(RichUserDetails user, Iterable<? extends Loggable> domainObjects) {
    return logCreateEvent(user, toArray(domainObjects, Loggable.class));
  }

  public Collection<LogEvent> logCreateEvent(RichUserDetails user, Loggable... domainObjects) {
    final List<LogEvent> logEvents = createLogEvents(user, LogEventType.CREATE, "", domainObjects);
    handleEvents(logEvents);
    return logEvents;
  }

  public Collection<LogEvent> logDeleteEvent(RichUserDetails user, String details,
      Iterable<? extends Loggable> domainObjects) {
    return logDeleteEvent(user, details, toArray(domainObjects, Loggable.class));
  }

  public Collection<LogEvent> logDeleteEvent(RichUserDetails user, String details, Loggable... domainObjects) {
    final List<LogEvent> logEvents = createLogEvents(user, LogEventType.DELETE, details, domainObjects);
    handleEvents(logEvents);
    return logEvents;
  }

  public List<LogEvent> logUpdateEvent(RichUserDetails user, String details, Loggable... domainObjects) {
    List<LogEvent> logEvents = createLogEvents(user, LogEventType.UPDATE, details, domainObjects);
    handleEvents(logEvents);
    return logEvents;
  }

  public List<LogEvent> logUpdateEvent(RichUserDetails user, String details, Iterable<? extends Loggable> domainObjects) {
    return logUpdateEvent(user, details, toArray(domainObjects, Loggable.class));
  }

  public LogEvent logReservationStatusChangeEvent(RichUserDetails user, Reservation reservation,
      ReservationStatus oldStatus) {
    LogEvent logEvent = createReservationLogEvent(user, LogEventType.UPDATE, reservation, oldStatus);
    handleEvent(logger, logEvent);
    return logEvent;
  }

  private static String getStateChangeMessage(final Reservation reservation, final ReservationStatus oldStatus) {
    return String.format("Changed state from [%s] to [%s]", oldStatus, reservation.getStatus());
  }

  private boolean shouldLogEventBePersisted(LogEvent logEvent) {
    return PERSISTABLE_LOG_EVENTS.contains(logEvent.getDomainObjectClass());
  }

}