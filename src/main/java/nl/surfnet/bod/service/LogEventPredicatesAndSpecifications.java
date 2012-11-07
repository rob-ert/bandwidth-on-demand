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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import nl.surfnet.bod.domain.Loggable;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.event.LogEvent;
import nl.surfnet.bod.event.LogEventType;
import nl.surfnet.bod.event.LogEvent_;

import org.joda.time.DateTime;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.util.CollectionUtils;

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
      final String domainObjectClass, final DateTime start, final DateTime end) {

    final Specification<LogEvent> specStatistics = new Specification<LogEvent>() {

      @Override
      public Predicate toPredicate(Root<LogEvent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.and(cb.equal(root.get(LogEvent_.eventType), eventType), (cb.equal(root
            .get(LogEvent_.domainObjectClass), domainObjectClass)), (cb
            .between(root.get(LogEvent_.created), start, end)));
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
        return cb.and(cb.equal(root.get(LogEvent_.domainObjectClass), LogEvent.getDomainObjectName(domainClass)), cb
            .between(root.get(LogEvent_.created), start, end));
      }
    };

    return spec;
  }

  public static Specification<LogEvent> specReservationIdsFromLogEventsCreatedBetweenWithNonFinalStateOnStartAndSuccesfullyCreatedBetween(
      final Class<? extends Loggable> domainClass, final DateTime start, final DateTime end) {

    final Specification<LogEvent> specReservationWithNonFinalStateOnDay = new Specification<LogEvent>() {
      final DateTime endOfStartDay = start.withTime(0, 0, 0, 0);

      @Override
      public Predicate toPredicate(Root<LogEvent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        return cb.and(cb.between(root.get(LogEvent_.created), start, endOfStartDay), determineDetailPredicate(root, cb,
            LogEvent.getStateChangeMessageNewStatusPart(ReservationStatus.TRANSITION_STATES_AS_ARRAY)));
      }
    };

    final Specification<LogEvent> specReservationCreatedSuccesfullyBetween = new Specification<LogEvent>() {
      @Override
      public Predicate toPredicate(Root<LogEvent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.and(determineDetailPredicate(root, cb, LogEvent
            .getStateChangeMessageNewStatusPart(ReservationStatus.SUCCESSFULLY_CREATED)), //
            cb.between(root.get(LogEvent_.created), start, end));
      }
    };

    return Specifications.where(specLogEventsByDomainClassAndCreatedBetween(domainClass, start, end)).and(
        specReservationWithNonFinalStateOnDay).and(specReservationCreatedSuccesfullyBetween);
  }

  static Specification<LogEvent> specLogEventsByDomainClassAndDescriptionPartBetween(
      final Class<? extends Loggable> domainClass, final DateTime start, final DateTime end, final String... textPart) {

    final Specification<LogEvent> spec = new Specification<LogEvent>() {

      @Override
      public Predicate toPredicate(Root<LogEvent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        Predicate domainClassPredicate = cb.and(cb.equal(root.get(LogEvent_.domainObjectClass), LogEvent
            .getDomainObjectName(domainClass)), cb.between(root.get(LogEvent_.created), start, end));

        Predicate detailPredicate = determineDetailPredicate(root, cb, textPart);
        return detailPredicate == null ? domainClassPredicate : cb.and(domainClassPredicate, detailPredicate);
      }

    };
    return spec;
  }

  private static Predicate determineDetailPredicate(final Root<LogEvent> root, final CriteriaBuilder cb,
      final String... textPart) {

    Predicate partPredicate = null;
    if (textPart != null) {
      Predicate textPartPredicate = null;
      for (String part : textPart) {
        part = "%".concat(part).concat("%");

        partPredicate = cb.like(root.get(LogEvent_.details), part);

        partPredicate = (textPartPredicate == null ? partPredicate : cb.or(textPartPredicate, partPredicate));
      }
    }
    return partPredicate;
  }
}
