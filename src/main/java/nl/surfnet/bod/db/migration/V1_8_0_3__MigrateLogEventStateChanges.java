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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.event.LogEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import com.google.common.annotations.VisibleForTesting;
import com.googlecode.flyway.core.api.migration.spring.SpringJdbcMigration;

/**
 * Migrates {@link LogEvent} by subtracting the state changes from the
 * {@link LogEvent#getDetails()} and insert them into the oldState and newState
 * columns.
 * 
 * Any SCHEDULED state will be translated to
 * {@link ReservationStatus#AUTO_START}, even in the details text.
 * 
 */
public class V1_8_0_3__MigrateLogEventStateChanges implements SpringJdbcMigration {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Override
  public void migrate(JdbcTemplate jdbcTemplate) throws Exception {

    final Connection connection = jdbcTemplate.getDataSource().getConnection();

    try (PreparedStatement queryLogEvent = connection
        .prepareStatement("select id, details from log_event where details like '%changed state from%'");

        PreparedStatement updateLogEvent = connection
            .prepareStatement("update log_event set old_reservation_status = ?, new_reservation_status = ?, details = ? where id = ?");

        ResultSet resultSet = queryLogEvent.executeQuery();) {

      while (resultSet.next()) {
        String details = resultSet.getString("details");
        List<String> states = splitStates(details);

        if (states != null && states.size() == 2) {
          // old_status
          updateLogEvent.setString(1, translate(states.get(0)));

          // new_status
          updateLogEvent.setString(2, translate(states.get(1)));

          // details
          updateLogEvent.setString(3, translate(details));

          // Id
          updateLogEvent.setLong(4, resultSet.getLong("id"));

          int updatedRows = updateLogEvent.executeUpdate();
          if (updatedRows != 1) {
            logger.warn("Unexpected amount of rows updated [{}] for row: {}", updatedRows, details);
          }
        }
        else {
          logger.warn("Unexpected content of details: " + details);
        }
      }
    }
  }

  @VisibleForTesting
  String translate(String details) {
    if (StringUtils.hasText(details)) {
      return StringUtils.replace(details, "SCHEDULED", "AUTO_START");
    }
    else {
      return details;
    }

  }

  @VisibleForTesting
  List<String> splitStates(String details) {
    ArrayList<String> states = new ArrayList<>();

    if (StringUtils.hasText(details) && details.contains("changed state from")) {
      String[] tokens = StringUtils.tokenizeToStringArray(details, "[]");

      if ((tokens != null) && (tokens.length == 4)) {
        // Old state
        states.add(tokens[1]);

        // New state
        states.add(tokens[3]);
      }
    }

    return states;
  }
}