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

import nl.surfnet.bod.event.LogEvent;
import nl.surfnet.bod.event.LogEvent_;

@Repository
public class CustomLogEventRepo {

  @PersistenceContext
  private EntityManager entityManager;

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

    final List<Long> resultList = entityManager.createQuery(criteriaQuery).getResultList();
    return resultList;
  }
}
