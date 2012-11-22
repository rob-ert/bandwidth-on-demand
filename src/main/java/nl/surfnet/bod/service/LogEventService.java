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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

@Service
public class LogEventService extends AbstractFullTextSearchService<LogEvent> {

  private static final String SYSTEM_USER = "System";
  private static final Collection<String> PERSISTABLE_LOG_EVENTS = ImmutableList.of(
    LogEvent.getDomainObjectName(Reservation.class),
    LogEvent.getDomainObjectName(VirtualPort.class),
    LogEvent.getDomainObjectName(PhysicalPort.class),
    LogEvent.getDomainObjectName(PhysicalResourceGroup.class),
    LogEvent.getDomainObjectName(Institute.class));


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
      ReservationStatus oldStatus, ReservationStatus newStatus) {

    Specification<LogEvent> whereClause = LogEventPredicatesAndSpecifications
        .specStateChangeFromOldToNewForReservationIdBetween(oldStatus, newStatus, Optional.<List<Long>> absent(),
            start, end);

    return logEventRepo.countDistinctDomainObjectIdsWithWhereClause(whereClause);
  }

  @VisibleForTesting
  LogEvent createLogEvent(RichUserDetails user, LogEventType eventType, Loggable domainObject, String details,
      Optional<ReservationStatus> oldStatus) {

    Optional<ReservationStatus> newStatus = getStatusWhenReservationObject(domainObject);

    return new LogEvent(user == null ? SYSTEM_USER : user.getUsername(), determineAdminGroup(user, domainObject),
        eventType, Optional.fromNullable(domainObject), details, oldStatus, newStatus);
  }

  @VisibleForTesting
  List<LogEvent> createLogEvents(RichUserDetails user, Collection<? extends Loggable> domainObjects,
      LogEventType logEventType, String details) {

    List<LogEvent> logEvents = new ArrayList<>();
    int size = Iterators.size((domainObjects).iterator());

    Iterator<? extends Loggable> it = (domainObjects).iterator();

    for (int i = 1; it.hasNext(); i++) {
      Loggable object = it.next();

      LogEvent logEvent = createLogEvent(user, logEventType, object, details, Optional.<ReservationStatus> absent());

      logEvent.setCorrelationId(String.valueOf(i) + "/" + String.valueOf(size));

      logEvents.add(logEvent);
    }

    return logEvents;
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

  public List<Long> findReservationIdsCreatedBetweenForNocWithState(DateTime start, DateTime end,
      ReservationStatus state) {

    Specification<LogEvent> spec = LogEventPredicatesAndSpecifications.specLatestStateForReservationBetweenWithStateIn(
        Optional.<List<Long>> absent(), start, end, state);

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

  public LogEvent findLatestStateChangeForReservationIdBeforeWithStateIn(Long id, DateTime before,
      ReservationStatus... states) {

    return findLatestStateChangeForReservationIdBeforeWithStateIn(Optional.<List<Long>> of(Lists.newArrayList(id)),
        before, states);
  }

  public LogEvent findLatestStateChangeForReservationIdBeforeWithStateIn(Optional<List<Long>> reservationIds,
      DateTime before, ReservationStatus... states) {

    Specification<LogEvent> whereClause = LogEventPredicatesAndSpecifications
        .specLatestStateForReservationBeforeWithStateIn(reservationIds, before, states);

    Long logEventId = logEventRepo.findMaxIdWithWhereClause(whereClause);

    return logEventId == null ? null : logEventRepo.findOne(logEventId);
  }

  @Override
  protected EntityManager getEntityManager() {
    return entityManager;
  }

  @VisibleForTesting
  Optional<ReservationStatus> getStatusWhenReservationObject(Loggable domainObject) {
    Optional<ReservationStatus> newStatus;
    if (Reservation.class.equals(domainObject.getClass())) {
      newStatus = Optional.fromNullable(((Reservation) domainObject).getStatus());
    }
    else {
      newStatus = Optional.<ReservationStatus> absent();
    }
    return newStatus;
  }

  /**
   * Delegates to {@link #handleEvent(Logger, LogEvent)}
   *
   * @param logEvent
   */
  private void handleEvent(LogEvent logEvent) {
    handleEvent(logger, logEvent);
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
      handleEvent(logEvent);
    }
  }

  public void logCreateEvent(RichUserDetails user, Collection<? extends Loggable> domainObjects, String details) {
    handleEvents(createLogEvents(user, domainObjects, LogEventType.CREATE, details));
  }

  public void logCreateEvent(RichUserDetails user, Loggable domainObject) {
    logCreateEvent(user, domainObject, null);
  }

  public void logCreateEvent(RichUserDetails user, Loggable domainObject, String details) {
    handleEvent(createLogEvent(user, LogEventType.CREATE, domainObject, details, Optional.<ReservationStatus> absent()));
  }

  public void logDeleteEvent(RichUserDetails user, Collection<? extends Loggable> domainObject, String details) {
    handleEvents(createLogEvents(user, domainObject, LogEventType.DELETE, details));
  }

  public void logDeleteEvent(RichUserDetails user, Loggable domainObject) {
    logDeleteEvent(user, domainObject, null);
  }

  public void logDeleteEvent(RichUserDetails user, Loggable domainObject, String details) {
    handleEvent(createLogEvent(user, LogEventType.DELETE, domainObject, details, Optional.<ReservationStatus> absent()));
  }

  public void logReadEvent(RichUserDetails user, Collection<? extends Loggable> domainObjects, String details) {
    handleEvents(createLogEvents(user, domainObjects, LogEventType.READ, details));
  }

  public void logReadEvent(RichUserDetails user, Loggable domainObject, String details) {
    handleEvent(createLogEvent(user, LogEventType.READ, domainObject, details, Optional.<ReservationStatus> absent()));
  }

  public void logUpdateEvent(RichUserDetails user, Collection<? extends Loggable> domainObjects, String details) {
    handleEvents(createLogEvents(user, domainObjects, LogEventType.UPDATE, details));
  }

  public void logUpdateEvent(RichUserDetails user, Loggable domainObject) {
    logUpdateEvent(user, domainObject, null, Optional.<ReservationStatus> absent());
  }

  public void logUpdateEvent(RichUserDetails user, Loggable domainObject, String details) {
    logUpdateEvent(user, domainObject, details, Optional.<ReservationStatus> absent());
  }

  public void logUpdateEvent(RichUserDetails user, Loggable domainObject, String details,
      Optional<ReservationStatus> oldStatus) {
    handleEvent(createLogEvent(user, LogEventType.UPDATE, domainObject, details, oldStatus));
  }

  /**
   * Determines if an Event should be persisted.
   */
  private boolean shouldLogEventBePersisted(LogEvent logEvent) {
    return PERSISTABLE_LOG_EVENTS.contains(logEvent.getDomainObjectClass());
  }

}