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
package nl.surfnet.bod.nsi.v2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import java.util.Optional;
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
    if (resultIdEnd.isPresent()) {
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
