package nl.surfnet.bod.repo;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.PhysicalResourceGroup_;

import org.springframework.data.jpa.domain.Specification;

import com.google.common.base.Optional;

public class PhysicalResourceGroupRepoImpl implements PhysicalResourceGroupRepoCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<Long> findIdsWithWhereClause(final Optional<Specification<PhysicalResourceGroup>> whereClause) {
    final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    final CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
    final Root<PhysicalResourceGroup> root = criteriaQuery.from(PhysicalResourceGroup.class);

    if (whereClause.isPresent()) {
      criteriaQuery.select(root.get(PhysicalResourceGroup_.id)).where(
          whereClause.get().toPredicate(root, criteriaQuery, criteriaBuilder));
    }
    else {
      criteriaQuery.select(root.get(PhysicalResourceGroup_.id));
    }

    return entityManager.createQuery(criteriaQuery).getResultList();
  }

}
