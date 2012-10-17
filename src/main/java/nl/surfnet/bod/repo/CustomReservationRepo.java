package nl.surfnet.bod.repo;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import com.google.common.base.Optional;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.Reservation_;

@Repository
public class CustomReservationRepo {

  @PersistenceContext
  private EntityManager entityManager;

  public Optional<List<Long>> findIdsWithWhereClause(final Specification<Reservation> whereClause) {
    final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    final CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
    final Root<Reservation> root = criteriaQuery.from(Reservation.class);

    criteriaQuery.select(root.get(Reservation_.id))
      .where(whereClause.toPredicate(root, criteriaQuery, criteriaBuilder));

    return Optional.of(entityManager.createQuery(criteriaQuery).getResultList());
  }
}
