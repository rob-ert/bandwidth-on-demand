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

import nl.surfnet.bod.event.LogEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import com.google.common.annotations.VisibleForTesting;
import com.googlecode.flyway.core.api.migration.spring.SpringJdbcMigration;

/**
 * Migrates the {@link LogEvent} table. The
 * {@link LogEvent#getDomainObjectClass()} is added in migration V1_6_0_0 to
 * enable searching on it. Before this migration the domainObjectClass was
 * stored in the {@link LogEvent#getDescription()} column appended with a label.
 *
 * This migration will split the domainObjectClass and the description and write
 * them to the corresponding columns.
 *
 * e.g [description] "VirtualPort: Port One" will be migrated to
 * [domainObjectClass] "VirtualPort", [description] "Port One"
 */
public class V1_6_0_2__MigrateLogEvents implements SpringJdbcMigration {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Override
  public void migrate(JdbcTemplate jdbcTemplate) throws Exception {

    final Connection connection = jdbcTemplate.getDataSource().getConnection();

    try (
      PreparedStatement queryLogEvent = connection
          .prepareStatement("select * from log_event where domain_object_class is null OR domain_object_class = ''");
      PreparedStatement updateLogEvent = connection
            .prepareStatement("update log_event set description = ?, domain_object_class = ? where id = ?");
      ResultSet resultSet = queryLogEvent.executeQuery();
    ) {
      while (resultSet.next()) {
        String description = resultSet.getString("description");
        String[] parts = splitAtColon(description);

        if (parts != null && parts.length == 2) {
          // domainObjectClass
          updateLogEvent.setString(2, parts[0]);

          // description, the rest without the domainObjectClass
          updateLogEvent.setString(1, parts[1]);

          // Id
          updateLogEvent.setLong(3, resultSet.getLong("id"));

          logger.info("Migrating: " + description);
          int updatedRows = updateLogEvent.executeUpdate();
          if (updatedRows != 1) {
            logger.warn("Unexpected amount of rows updated: " + updatedRows);
          }
        }
        else {
          logger.warn("Unexpected content of description: " + description);
        }
      }
    }
  }

  @VisibleForTesting
  String[] splitAtColon(String description) {
    String[] parts = StringUtils.split(description, ":");

    for (int i = 0; parts != null && i < parts.length; i++) {
      if (parts[i] != null) {
        parts[i] = parts[i].trim();
      }
    }

    return parts;
  }
}