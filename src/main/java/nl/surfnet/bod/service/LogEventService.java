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
package nl.surfnet.bod.service;

import static com.google.common.collect.Iterables.toArray;
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

@Service
public class LogEventService extends AbstractFullTextSearchService<LogEvent> {

  private static final String SYSTEM_USER = "System";
  private static final Collection<String> PERSISTABLE_LOG_EVENTS = ImmutableList.of(
      LogEvent.getDomainObjectName(Reservation.class),
      LogEvent.getDomainObjectName(VirtualPort.class),
      LogEvent.getDomainObjectName(PhysicalPort.class),
      LogEvent.getDomainObjectName(PhysicalResourceGroup.class),
      LogEvent.getDomainObjectName(Institute.class),
      LogEvent.getDomainObjectName(VirtualPortRequestLink.class));

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

    return new LogEvent(
        user == null ? SYSTEM_USER : user.getUsername(),
        domainObject.getAdminGroups(),
        eventType,
        Optional.fromNullable(domainObject),
        details,
        oldStatus,
        newStatus);
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

  public List<Long> findReservationIdsCreatedBetweenWithStateInAdminGroups(DateTime start, DateTime end,
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