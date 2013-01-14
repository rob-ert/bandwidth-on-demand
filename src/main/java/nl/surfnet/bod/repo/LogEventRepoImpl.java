/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.google.common.base.Optional;

public class LogEventRepoImpl implements LogEventRepoCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<Long> findIdsWithWhereClause(final Specification<LogEvent> whereClause, Optional<Sort> sort) {
    return findIds(Optional.of(whereClause), sort);
  }

  @Override
  public List<Long> findAllIds(Optional<Sort> sort) {
    return findIds(Optional.<Specification<LogEvent>> absent(), sort);
  }

  private List<Long> findIds(Optional<Specification<LogEvent>> whereClause, Optional<Sort> sort) {
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
    CustomRepoHelper.addSortClause(sort, criteriaBuilder, criteriaQuery, root);

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

  @Override
  public long countDistinctDomainObjectIdsWithWhereClause(final Specification<LogEvent> whereClause) {
    return findDistinctDomainObjectIdsWithWhereClause(whereClause).size();
  }

  @Override
  public Long findMaxIdWithWhereClause(final Specification<LogEvent> whereClause) {
    final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    final CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
    final Root<LogEvent> root = criteriaQuery.from(LogEvent.class);

    // TODO Franky, should be on created field, but since id is numbered
    // incrementally, the result will be the same
    criteriaQuery.select(criteriaBuilder.greatest(root.get(LogEvent_.id))).where(
        whereClause.toPredicate(root, criteriaQuery, criteriaBuilder));

    return entityManager.createQuery(criteriaQuery).getSingleResult();
  }

}
