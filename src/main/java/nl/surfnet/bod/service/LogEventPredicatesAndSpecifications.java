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
import java.util.List;

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

import org.apache.commons.lang.ArrayUtils;
import org.joda.time.DateTime;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Optional;

import static nl.surfnet.bod.web.WebUtils.not;

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

  
  
  
  
  
  static Specification<LogEvent> specLatestStateForReservationBeforeWithStateIn(
      final Optional<List<Long>> reservationIds, final DateTime before, final ReservationStatus... states) {
    final String domainObjectName = LogEvent.getDomainObjectName(Reservation.class);

    return new Specification<LogEvent>() {
      @Override
      public Predicate toPredicate(Root<LogEvent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        Predicate predicate = getPredicateForDomainObjectBefore(reservationIds, before, domainObjectName, root, cb);

        if (not(ArrayUtils.isEmpty(states))) {
          predicate = cb.and(predicate, cb.isNotNull(root.get(LogEvent_.newReservationStatus)), (root
              .get(LogEvent_.newReservationStatus).in((Object[]) states)));
        }

        return predicate;
      }
    };
  }

  static Specification<LogEvent> specLatestStateForReservationBetweenWithStateIn(
      final Optional<List<Long>> reservationIds, final DateTime start, final DateTime end,
      final ReservationStatus... states) {

    Specification<LogEvent> specBetween = new Specification<LogEvent>() {

      @Override
      public Predicate toPredicate(Root<LogEvent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.greaterThanOrEqualTo(root.get(LogEvent_.created), start);
      }
    };

    Specification<LogEvent> specBefore = specLatestStateForReservationBeforeWithStateIn(reservationIds, end, states);

    return Specifications.where(specBefore).and(specBetween);
  }

  static Specification<LogEvent> specStateChangeFromOldToNewForReservationIdBefore(final ReservationStatus oldStatus,
      final ReservationStatus newStatus, final List<Long> reservationIds, final DateTime before) {
    final String domainObjectName = LogEvent.getDomainObjectName(Reservation.class);

    return new Specification<LogEvent>() {
      @Override
      public Predicate toPredicate(Root<LogEvent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        Predicate predicate = getPredicateForDomainObjectBefore(Optional.of(reservationIds), before, domainObjectName,
            root, cb);

        predicate = cb.and(predicate, cb.equal(root.get(LogEvent_.oldReservationStatus), oldStatus), cb.equal(root
            .get(LogEvent_.newReservationStatus), newStatus));

        return predicate;
      }

    };
  }

  private static Predicate getPredicateForDomainObjectBefore(final Optional<List<Long>> reservationIds,
      final DateTime before, final String domainObjectName, Root<LogEvent> root, CriteriaBuilder cb) {

    Predicate predicate = cb.and(cb.equal(root.get(LogEvent_.domainObjectClass), domainObjectName), cb
        .lessThanOrEqualTo(root.get(LogEvent_.created), before));

    if (reservationIds.isPresent() && (not(CollectionUtils.isEmpty(reservationIds.get())))) {
      predicate = cb.and(predicate, root.get(LogEvent_.domainObjectId).in(reservationIds.get()));
    }

    return predicate;
  }
}