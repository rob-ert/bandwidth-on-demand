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

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.event.LogEvent;
import nl.surfnet.bod.event.LogEvent_;

import org.apache.commons.lang.ArrayUtils;
import org.joda.time.DateTime;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.Lists;

import static nl.surfnet.bod.web.WebUtils.not;

public final class LogEventPredicatesAndSpecifications {

  private LogEventPredicatesAndSpecifications() {
  }

  static Specification<LogEvent> specLogEventsByAdminGroups(final Collection<String> adminGroups) {

    return new Specification<LogEvent>() {
      @Override
      public javax.persistence.criteria.Predicate toPredicate(Root<LogEvent> root, CriteriaQuery<?> query,
          CriteriaBuilder cb) {
        return getPredicateInAdminGroups(adminGroups, root, cb);
      }
    };
  }

  static Specification<LogEvent> specForReservationBeforeInAdminGroupsWithStateIn(final Long reservationId, final DateTime before,
      final List<String> adminGroups, final ReservationStatus... states) {

    final String domainObjectName = LogEvent.getDomainObjectName(Reservation.class);
    return new Specification<LogEvent>() {

      @Override
      public Predicate toPredicate(Root<LogEvent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        Predicate predicate = getPredicateForDomainObjectBeforeInAdminGroups(root, cb, Lists.newArrayList(reservationId), before,
            domainObjectName, adminGroups);
        Predicate predicateStateIn = getPredicateForStateIn(root, cb, states);

        return predicateStateIn == null ? predicate : cb.and(predicate, predicateStateIn);
      }
    };
  }

  static Specification<LogEvent> specForReservationBetweenForAdminGroupsWithStateIn(final List<Long> reservationIds,
      final DateTime start, final DateTime end, final List<String> adminGroups, final ReservationStatus... states) {

    return new Specification<LogEvent>() {
      private final String domainObjectName = LogEvent.getDomainObjectName(Reservation.class);

      @Override
      public Predicate toPredicate(Root<LogEvent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        Predicate predicate = getPredicateForDomainObjectInAdminGroupsBetween(root, cb, reservationIds, start, end,
            domainObjectName, adminGroups);

        return cb.and(predicate, getPredicateForStateIn(root, cb, states));
      }
    };
  }

  static Specification<LogEvent> specStateChangeFromOldToNewForReservationIdInAdminGroupsBetween(final ReservationStatus oldStatus,
      final ReservationStatus newStatus, final List<Long> reservationIds, final DateTime start, final DateTime end,
      final List<String> adminGroups) {

    return new Specification<LogEvent>() {
      private final String domainObjectName = LogEvent.getDomainObjectName(Reservation.class);

      @Override
      public Predicate toPredicate(Root<LogEvent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        Predicate predicate = getPredicateForDomainObjectInAdminGroupsBetween(root, cb, reservationIds, start, end,
            domainObjectName, adminGroups);

        return cb.and(predicate, getPredicateForStateTransition(root, cb, oldStatus, newStatus));
      }
    };
  }

  private static Predicate getPredicateForDomainObjectInAdminGroupsBetween(final Root<LogEvent> root, final CriteriaBuilder cb,
      final List<Long> reservationIds, final DateTime start, final DateTime end, final String domainObjectName,
      final List<String> adminGroups) {

    Predicate predicate = getDomainObjectWithIdsInAdminGroups(root, cb, reservationIds, domainObjectName, adminGroups);

    return cb.and(predicate, cb.between(root.get(LogEvent_.created), start, end));
  }

  private static Predicate getPredicateForDomainObjectBeforeInAdminGroups(Root<LogEvent> root, CriteriaBuilder cb,
      final List<Long> reservationIds, final DateTime before, final String domainObjectName,
      final List<String> adminGroups) {

    Predicate predicate = getDomainObjectWithIdsInAdminGroups(root, cb, reservationIds, domainObjectName, adminGroups);
    return cb.and(predicate, cb.lessThanOrEqualTo(root.get(LogEvent_.created), before));
  }

  private static Predicate getDomainObjectWithIdsInAdminGroups(final Root<LogEvent> root, final CriteriaBuilder cb,
      final List<Long> reservationIds, final String domainObjectName, List<String> adminGroups) {

    Predicate predicate = cb.equal(root.get(LogEvent_.domainObjectClass), domainObjectName);

    if (not(CollectionUtils.isEmpty(reservationIds))) {
      predicate = cb.and(predicate, root.get(LogEvent_.domainObjectId).in(reservationIds));
    }

    Predicate inAdminGroups = getPredicateInAdminGroups(adminGroups, root, cb);
    if (inAdminGroups != null) {
      predicate = cb.and(predicate, inAdminGroups);
    }

    return predicate;
  }

  private static Predicate getPredicateInAdminGroups(Collection<String> adminGroups, Root<LogEvent> root, CriteriaBuilder cb) {

    Predicate predicate = null;

    if (not(CollectionUtils.isEmpty(adminGroups))) {
      predicate = root.get(LogEvent_.adminGroup).in(adminGroups);
    }

    return predicate;
  }

  private static Predicate getPredicateForStateTransition(Root<LogEvent> root, CriteriaBuilder cb,
      final ReservationStatus oldStatus, final ReservationStatus newStatus) {

    return cb.and(cb.equal(root.get(LogEvent_.oldReservationStatus), oldStatus), cb.equal(root
        .get(LogEvent_.newReservationStatus), newStatus));
  }

  private static Predicate getPredicateForStateIn(Root<LogEvent> root, CriteriaBuilder cb,
      final ReservationStatus... states) {
    Predicate predicate = null;

    if (not(ArrayUtils.isEmpty(states))) {
      predicate = cb.and(cb.isNotNull(root.get(LogEvent_.newReservationStatus)), (root
          .get(LogEvent_.newReservationStatus).in((Object[]) states)));
    }
    return predicate;
  }
}