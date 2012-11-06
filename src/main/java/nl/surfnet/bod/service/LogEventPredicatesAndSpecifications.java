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

  static Specification<LogEvent> specDomainObjectIsFromLogEventsByDomainClassAndCreatedBetween(
      final Class<? extends Loggable> domainClass, final DateTime start, final DateTime end) {

    final Specification<LogEvent> spec = new Specification<LogEvent>() {
      @Override
      public Predicate toPredicate(Root<LogEvent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        return (cb.createQuery().distinct(true).select(root.get(LogEvent_.domainObjectId))
            .where(specLogEventsByDomainClassAndCreatedBetween(domainClass, start, end).toPredicate(root, query, cb)))
            .getRestriction();
      }
    };

    return spec;
  }

  static Specification<LogEvent> specLogEventsByDomainClassAndDescriptionPartBetween(
      final Class<? extends Loggable> domainClass, final DateTime start, final DateTime end, final String... textPart) {

    final Specification<LogEvent> spec = new Specification<LogEvent>() {

      @Override
      public Predicate toPredicate(Root<LogEvent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        final Predicate predicate;

        Predicate domainClassPredicate = cb.and(cb.equal(root.get(LogEvent_.domainObjectClass), LogEvent
            .getDomainObjectName(domainClass)), cb.between(root.get(LogEvent_.created), start, end));

        if (textPart != null) {
          Predicate textPartPredicate = null;
          Predicate partPredicate = null;
          for (String part : textPart) {
            part = "%".concat(part).concat("%");

            partPredicate = cb.or(cb.like(root.get(LogEvent_.description), part), cb.like(root.get(LogEvent_.details),
                part));

            textPartPredicate = (textPartPredicate == null ? partPredicate : cb.or(textPartPredicate, partPredicate));
          }
          predicate = cb.and(domainClassPredicate, textPartPredicate);
        }
        else {
          predicate = domainClassPredicate;
        }

        return predicate;
      }
    };
    return spec;
  }

}
