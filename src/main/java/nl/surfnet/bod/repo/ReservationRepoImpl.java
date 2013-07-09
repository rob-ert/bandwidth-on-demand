/**
 * Copyright (c) 2012, 2013 SURFnet BV
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
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.Reservation_;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.google.common.base.Optional;

public class ReservationRepoImpl implements CustomRepo<Reservation> {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<Long> findIdsWithWhereClause(Optional<Specification<Reservation>> whereClause, Optional<Sort> sort) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
    Root<Reservation> root = criteriaQuery.from(Reservation.class);

    criteriaQuery.select(root.get(Reservation_.id));

    if (whereClause.isPresent()) {
      criteriaQuery.where(whereClause.get().toPredicate(root, criteriaQuery, criteriaBuilder));
    }

    CustomRepoHelper.addSortClause(sort, criteriaBuilder, criteriaQuery, root);

    return entityManager.createQuery(criteriaQuery).getResultList();
  }

  public long countDistinctIdsWithWhereClause(Specification<Reservation> whereClause) {

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
    Root<Reservation> root = criteriaQuery.from(Reservation.class);
    // TODO get count
    criteriaQuery.distinct(true).select(root.get(Reservation_.id)).where(
        whereClause.toPredicate(root, criteriaQuery, criteriaBuilder));

    return entityManager.createQuery(criteriaQuery).getResultList().size();
  }

  public Reservation getByReservationIdWithPessimisticWriteLock(String reservationId) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Reservation> query = builder.createQuery(Reservation.class);
    Root<Reservation> root = query.from(Reservation.class);
    query.where(builder.equal(root.get(Reservation_.reservationId), reservationId));

    return entityManager.createQuery(query).setLockMode(LockModeType.PESSIMISTIC_WRITE).getSingleResult();
  }
}
