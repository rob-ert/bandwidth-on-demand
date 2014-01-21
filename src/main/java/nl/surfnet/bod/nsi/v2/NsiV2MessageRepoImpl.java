package nl.surfnet.bod.nsi.v2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import org.springframework.stereotype.Repository;

@Repository
public class NsiV2MessageRepoImpl implements NsiV2MessageRepoCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<NsiV2Message> findQueryResults(String connectionId, Optional<Long> resultIdStart, Optional<Long> resultIdEnd) {
    Preconditions.checkNotNull(connectionId);

    StringBuffer hql = new StringBuffer("SELECT msg FROM NsiV2Message AS msg WHERE connectionId = :connectionId");
    Map<String, Object> queryParams = new HashMap<>();
    queryParams.put("connectionId", connectionId);
    if (resultIdStart.isPresent()){
      hql.append(" AND resultId >= :resultIdStart");
      queryParams.put("resultIdStart", resultIdStart.get());
    }
    if(resultIdEnd.isPresent()) {
      hql.append(" AND resultId <= :resultIdEnd");
      queryParams.put("resultIdEnd", resultIdEnd.get());
    }

    hql.append(" ORDER BY msg.resultId ASC");
    TypedQuery<NsiV2Message> query = entityManager.createQuery(hql.toString(), NsiV2Message.class);
    for (String paramName: queryParams.keySet()) {
      query.setParameter(paramName, queryParams.get(paramName));
    }

    return query.getResultList();
  }
}
