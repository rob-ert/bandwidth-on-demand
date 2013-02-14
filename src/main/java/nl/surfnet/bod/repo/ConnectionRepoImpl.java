package nl.surfnet.bod.repo;

import static nl.surfnet.bod.repo.CustomRepoHelper.addSortClause;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.Connection_;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.google.common.base.Optional;

public class ConnectionRepoImpl implements CustomRepo<Connection> {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<Long> findIdsWithWhereClause(Optional<Specification<Connection>> whereClause, Optional<Sort> sort) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
    Root<Connection> root = criteriaQuery.from(Connection.class);

    criteriaQuery.select(root.get(Connection_.id));

    if (whereClause.isPresent()) {
      criteriaQuery.where(whereClause.get().toPredicate(root, criteriaQuery, criteriaBuilder));
    }

    addSortClause(sort, criteriaBuilder, criteriaQuery, root);

    return entityManager.createQuery(criteriaQuery).getResultList();
  }

}
