package nl.surfnet.bod.nsi.v2;

import com.google.common.base.Preconditions;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class NsiV2MessageRepoImpl implements NsiV2MessageRepoCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<NsiV2Message> findQueryResults(String connectionId, Long resultIdStart, Long resultIdEnd) {
    Preconditions.checkNotNull(connectionId);

    StringBuffer hql = new StringBuffer("SELECT msg FROM NsiV2Message AS msg WHERE connectionId = :connectionId");
    Map<String, Object> queryParams = new HashMap<>();
    queryParams.put("connectionId", connectionId);
    if (resultIdStart != null){
      hql.append(" AND resultId >= :resultIdStart");
      queryParams.put("resultIdStart", resultIdStart);
    }
    if(resultIdEnd != null) {
      hql.append(" AND resultId <= :resultIdEnd");
      queryParams.put("resultIdEnd", resultIdEnd);
    }

    hql.append(" ORDER BY msg.resultId ASC");
    TypedQuery<NsiV2Message> query = entityManager.createQuery(hql.toString(), NsiV2Message.class);
    for (String paramName: queryParams.keySet()) {
      query.setParameter(paramName, queryParams.get(paramName));
    }

    return query.getResultList();
  }
}
