package nl.surfnet.bod.repo;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.domain.VirtualResourceGroup_;

import org.springframework.data.jpa.domain.Specification;

import com.google.common.base.Optional;

public class VirtualResourceGroupRepoImpl implements VirtualResourceGroupRepoCustom {

  @PersistenceContext
  private EntityManager entityManager;

  public List<Long> findIdsWithWhereClause(final Optional<Specification<VirtualResourceGroup>> whereClause) {
    final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    final CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
    final Root<VirtualResourceGroup> root = criteriaQuery.from(VirtualResourceGroup.class);

    if (whereClause.isPresent()) {
      criteriaQuery.select(root.get(VirtualResourceGroup_.id)).where(
          whereClause.get().toPredicate(root, criteriaQuery, criteriaBuilder));
    }
    else {
      criteriaQuery.select(root.get(VirtualResourceGroup_.id));
    }
    return entityManager.createQuery(criteriaQuery).getResultList();
  }
}
