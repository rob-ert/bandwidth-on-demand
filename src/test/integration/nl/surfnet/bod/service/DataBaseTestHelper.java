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
package nl.surfnet.bod.service;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import nl.surfnet.bod.config.IntegrationDbConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

public final class DataBaseTestHelper {

  private static final String DB_DRIVER_CLASS_KEY = "jdbc.driverClass";
  private static final String DB_PASS_KEY = "jdbc.password";
  private static final String DB_URL_KEY = "jdbc.jdbcUrl";
  private static final String DB_USER_KEY = "jdbc.user";

  private static final List<String> TABLES_THAT_SHOULD_NOT_BE_TRUNCATED = ImmutableList.of("schema_version", "institute");

  private static final Logger logger = LoggerFactory.getLogger(DataBaseTestHelper.class);

  private DataBaseTestHelper() {
  }

  public static void clearSeleniumDatabaseSkipBaseData() {
    Properties props = getSeleniumProperties();

    clearDatabaseAndSkipBaseData(
      props.getProperty(DB_URL_KEY),
      props.getProperty(DB_USER_KEY),
      props.getProperty(DB_PASS_KEY),
      props.getProperty(DB_DRIVER_CLASS_KEY));
  }

  private static Properties getSeleniumProperties() {
    Properties props = new Properties();
    try {
      props.load(DataBaseTestHelper.class.getResourceAsStream("/bod-selenium.properties"));
      return props;
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void clearIntegrationDatabaseSkipBaseData() {
    clearDatabaseAndSkipBaseData(
      IntegrationDbConfiguration.DB_URL,
      IntegrationDbConfiguration.DB_USER,
      IntegrationDbConfiguration.DB_PASSWORD,
      IntegrationDbConfiguration.DB_DRIVER_CLASS);
  }

  private static void clearDatabaseAndSkipBaseData(String dbUrl, String dbUser, String dbPassword, String dbDriverClass) {

    try (Connection connection = createDbConnection(dbUrl, dbUser, dbPassword, dbDriverClass)) {

      List<String> tablesToTruncate = getTablesToTruncate(connection);
      if (tablesToTruncate.isEmpty()) {
        logger.warn("Database [{}] has no tables, nothing to truncate", dbUrl);
      }
      else {
        truncateTables(tablesToTruncate, connection);
        logger.warn("Cleared database [{}], truncated tables: {}", dbUrl, tablesToTruncate);
      }
    }
    catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private static void truncateTables(Collection<String> tables, Connection connection) {
    try (Statement statement = connection.createStatement()) {
      statement.executeUpdate(createTruncateQuery(tables));
    }
    catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private static Connection createDbConnection(String dbUrl, String dbUser, String dbPass, String dbDriverClass) throws SQLException {
      try {
        Class.forName(dbDriverClass).newInstance();
      }
      catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
        throw new RuntimeException(e);
      }

      return DriverManager.getConnection(dbUrl, dbUser, dbPass);
  }

  private static List<String> getTablesToTruncate(Connection connection) {
    List<String> result = new ArrayList<>();

    try (ResultSet tables = getTables(connection)) {
      while (tables.next()) {
        String tableName = tables.getString(3);
        if (!TABLES_THAT_SHOULD_NOT_BE_TRUNCATED.contains(tableName)) {
          result.add(tableName);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return result;
  }

  private static String createTruncateQuery(Collection<String> tables) {
    StringBuilder truncateQuery = new StringBuilder("TRUNCATE ");

    truncateQuery.append(Joiner.on(", ").join(tables));

    truncateQuery.append(" CASCADE");

    return truncateQuery.toString();
  }

  private static ResultSet getTables(Connection connection) throws SQLException {
    return connection.getMetaData().getTables(null, null, "%", new String[] { "TABLE" });
  }

}