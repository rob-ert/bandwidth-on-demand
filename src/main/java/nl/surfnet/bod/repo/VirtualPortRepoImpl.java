package nl.surfnet.bod.repo;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualPort_;

import org.springframework.data.jpa.domain.Specification;

import com.google.common.base.Optional;

public class VirtualPortRepoImpl implements VirtualPortRepoCustom {

  @PersistenceContext
  private EntityManager entityManager;

  public List<Long> findIdsWithWhereClause(final Optional<Specification<VirtualPort>> whereClause) {
    final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    final CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
    final Root<VirtualPort> root = criteriaQuery.from(VirtualPort.class);

    if (whereClause.isPresent()) {
      criteriaQuery.select(root.get(VirtualPort_.id)).where(
          whereClause.get().toPredicate(root, criteriaQuery, criteriaBuilder));
    }
    else {
      criteriaQuery.select(root.get(VirtualPort_.id));
    }

    return entityManager.createQuery(criteriaQuery).getResultList();
  }
}
