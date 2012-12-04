/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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