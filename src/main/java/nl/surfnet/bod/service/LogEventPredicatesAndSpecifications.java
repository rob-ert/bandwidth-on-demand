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

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import nl.surfnet.bod.domain.Loggable;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.event.LogEvent;
import nl.surfnet.bod.event.LogEventType;
import nl.surfnet.bod.event.LogEvent_;

import org.joda.time.DateTime;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.util.CollectionUtils;

import com.google.common.annotations.VisibleForTesting;

public final class LogEventPredicatesAndSpecifications {

  private LogEventPredicatesAndSpecifications() {
  }

  static Specification<LogEvent> specLogEventsByAdminGroups(final Collection<String> adminGroups) {
    return new Specification<LogEvent>() {
      @Override
      public javax.persistence.criteria.Predicate toPredicate(Root<LogEvent> root, CriteriaQuery<?> query,
          CriteriaBuilder cb) {
        return cb.and(root.get(LogEvent_.adminGroup).in(adminGroups));
      }
    };
  }

  static Specification<LogEvent> specStatistics(Collection<String> adminGroups, final LogEventType eventType,
      final Class<? extends Loggable> domainClass, final DateTime start, final DateTime end) {

    final Specification<LogEvent> specStatistics = new Specification<LogEvent>() {

      @Override
      public Predicate toPredicate(Root<LogEvent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        Predicate eventTypeIs = cb.equal(root.get(LogEvent_.eventType), eventType);

        return cb.and(eventTypeIs, specLogEventsByDomainClassAndCreatedBetween(domainClass, start, end).toPredicate(
            root, query, cb));
      }
    };

    if (CollectionUtils.isEmpty(adminGroups)) {
      return specStatistics;
    }
    else {
      return Specifications.where(specLogEventsByAdminGroups(adminGroups)).and(specStatistics);
    }
  }

  static Specification<LogEvent> specLogEventsByDomainClassAndCreatedBetween(
      final Class<? extends Loggable> domainClass, final DateTime start, final DateTime end) {

    final Specification<LogEvent> spec = new Specification<LogEvent>() {
      @Override
      public Predicate toPredicate(Root<LogEvent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        Predicate domainClassIs = cb.equal(root.get(LogEvent_.domainObjectClass), LogEvent
            .getDomainObjectName(domainClass));

        Predicate createdBetween = cb.between(root.get(LogEvent_.created), start, end);

        return cb.and(domainClassIs, createdBetween);
      }
    };

    return spec;
  }

  public static Specification<LogEvent> specReservationIdsFromLogEventsCreatedBetweenWithNonFinalStateOnStartAndSuccesfullyCreatedBetween(
      final Class<? extends Loggable> domainClass, final DateTime start, final DateTime end) {

    final Specification<LogEvent> spec = new Specification<LogEvent>() {
      final DateTime endOfStartDay = start.withTime(0, 0, 0, 0);

      @Override
      public Predicate toPredicate(Root<LogEvent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        Predicate createdOn = cb.between(root.get(LogEvent_.created), start, endOfStartDay);
        Predicate nonFinalState = determineDetailLikePredicate(root, cb, LogEvent
            .getStateChangeMessageNewStatusPart(ReservationStatus.TRANSITION_STATES_AS_ARRAY));
        Predicate successFullyCreated = determineDetailLikePredicate(root, cb, LogEvent
            .getStateChangeMessageNewStatusPart(ReservationStatus.SUCCESSFULLY_CREATED));

        return cb.and(createdOn, nonFinalState, successFullyCreated);
      }
    };

    // TODO specLogEventsByDomainClassAndCreatedBetween duplicate with
    // createdOn?
    return Specifications.where(specLogEventsByDomainClassAndCreatedBetween(domainClass, start, end)).and(spec);

  }

  static Specification<LogEvent> specLogEventsByDomainClassAndDescriptionPartBetween(
      final Class<? extends Loggable> domainClass, final DateTime start, final DateTime end, final String... textPart) {

    final Specification<LogEvent> spec = new Specification<LogEvent>() {

      @Override
      public Predicate toPredicate(Root<LogEvent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        Predicate domainClassPredicate = cb.and(cb.equal(root.get(LogEvent_.domainObjectClass), LogEvent
            .getDomainObjectName(domainClass)), cb.between(root.get(LogEvent_.created), start, end));

        Predicate detailPredicate = determineDetailLikePredicate(root, cb, textPart);

        return detailPredicate == null ? domainClassPredicate : cb.and(domainClassPredicate, detailPredicate);
      }

    };
    return spec;
  }

  @VisibleForTesting
  static Predicate determineDetailLikePredicate(final Root<LogEvent> root, final CriteriaBuilder cb,
      final String... textPart) {

    Predicate partPredicate = null;
    if (textPart != null) {
      Predicate textPartPredicate = null;
      for (String part : textPart) {
        part = "%".concat(part).concat("%");

        textPartPredicate = cb.like(root.get(LogEvent_.details), part);

        partPredicate = (partPredicate == null ? textPartPredicate : cb.or(textPartPredicate, partPredicate));
      }
    }
    return partPredicate;
  }

  static Specification<LogEvent> findLatestStateForReservationBefore(final EntityManager entityManager,
      final Long reservationId, final DateTime before) {
    final String domainObjectName = LogEvent.getDomainObjectName(Reservation.class);

    return new Specification<LogEvent>() {

      @Override
      public Predicate toPredicate(Root<LogEvent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        Predicate predicate = cb.and(cb.equal(root.get(LogEvent_.domainObjectClass), domainObjectName), cb.lessThan(
            root.get(LogEvent_.created), before), cb.equal(root.get(LogEvent_.domainObjectId), reservationId));

        CriteriaQuery<DateTime> subQuery = cb.createQuery(DateTime.class).select(
            cb.greatest(root.get(LogEvent_.created))).where(predicate);

        return cb.and(predicate, cb.equal(root.get(LogEvent_.created), entityManager.createQuery(subQuery)
            .getResultList()));
      }
    };
  }
}