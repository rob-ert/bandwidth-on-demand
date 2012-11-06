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
package nl.surfnet.bod.repo;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import nl.surfnet.bod.event.LogEvent;
import nl.surfnet.bod.event.LogEvent_;

import org.springframework.data.jpa.domain.Specification;

import com.google.common.base.Optional;

public class LogEventRepoImpl implements LogEventRepoCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<Long> findIdsWithWhereClause(final Optional<Specification<LogEvent>> whereClause) {
    final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    final CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
    final Root<LogEvent> root = criteriaQuery.from(LogEvent.class);

    if (whereClause.isPresent()) {
      criteriaQuery.select(root.get(LogEvent_.id)).where(
          whereClause.get().toPredicate(root, criteriaQuery, criteriaBuilder));
    }
    else {
      criteriaQuery.select(root.get(LogEvent_.id));
    }

    return entityManager.createQuery(criteriaQuery).getResultList();
  }

  @Override
  public List<Long> findDistinctDomainObjectIdsWithWhereClause(final Specification<LogEvent> whereClause) {
    final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    final CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
    final Root<LogEvent> root = criteriaQuery.from(LogEvent.class);

    criteriaQuery.distinct(true).select(root.get(LogEvent_.domainObjectId)).where(
        whereClause.toPredicate(root, criteriaQuery, criteriaBuilder));

    return entityManager.createQuery(criteriaQuery).getResultList();
  }

}
