package nl.surfnet.bod.repo;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.QueryUtils;

import com.google.common.base.Optional;

public class CustomRepoHelper {

  private CustomRepoHelper() {

  }

  public static <T, K> void addSortClause(Optional<Sort> sort, final CriteriaBuilder criteriaBuilder,
      final CriteriaQuery<T> criteriaQuery, final Root<K> root) {

    if (sort.isPresent()) {
      List<javax.persistence.criteria.Order> orderList = QueryUtils.toOrders(sort.get(), root, criteriaBuilder);
      criteriaQuery.orderBy(orderList);
    }
  }
}
