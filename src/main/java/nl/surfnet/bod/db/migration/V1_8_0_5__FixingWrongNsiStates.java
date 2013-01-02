/**
 * Copyright (c) 2012, SURFnet BV
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
package nl.surfnet.bod.db.migration;

import nl.surfnet.bod.domain.ReservationStatus;

import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.springframework.jdbc.core.JdbcTemplate;

import com.googlecode.flyway.core.api.migration.spring.SpringJdbcMigration;

public class V1_8_0_5__FixingWrongNsiStates implements SpringJdbcMigration {

  @Override
  public void migrate(JdbcTemplate jdbcTemplate) throws Exception {
    jdbcTemplate.update(queryChangeState(ConnectionStateType.CLEANING, ConnectionStateType.TERMINATED));
    jdbcTemplate.update(queryChangeState(ConnectionStateType.TERMINATING, ConnectionStateType.TERMINATED));

    jdbcTemplate.update(queryChangeState(ReservationStatus.TIMED_OUT, ConnectionStateType.TERMINATED));
    jdbcTemplate.update(queryChangeState(ReservationStatus.CANCELLED, ConnectionStateType.TERMINATED));
    jdbcTemplate.update(queryChangeState(ReservationStatus.SUCCEEDED, ConnectionStateType.TERMINATED));
  }

  private String queryChangeState(ConnectionStateType fromState, ConnectionStateType toState) {
    return String.format(
        "update connection set current_state = '%2$s' where reservation is null and current_state = '%1$s'",
        fromState, toState);
  }

  private String queryChangeState(ReservationStatus rStatus, ConnectionStateType cStatus) {
    return String.format(
        "update connection c set current_state = '%1$s' from reservation r where c.reservation = r.id and r.status = '%2$s' and c.current_state != '%1$s'", cStatus, rStatus);
  }

}
