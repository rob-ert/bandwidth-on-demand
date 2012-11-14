/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
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
public class V1_7_0_1__MigrateLogEventStateChanges implements SpringJdbcMigration {

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