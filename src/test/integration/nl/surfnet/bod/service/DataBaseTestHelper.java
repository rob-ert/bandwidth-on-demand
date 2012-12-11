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
package nl.surfnet.bod.service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataBaseTestHelper {
  public static final String DB_DRIVER_CLASS = "jdbc.driverClass";
  public static final String DB_PASS = "jdbc.password";
  public static final String DB_URL = "jdbc.jdbcUrl";
  public static final String DB_USER = "jdbc.user";

  public static final InputStream PROP_DEFAULT = DataBaseTestHelper.class
      .getResourceAsStream("/bod-default.properties");
  public static final InputStream PROP_BOD = DataBaseTestHelper.class.getResourceAsStream("/bod.properties");
  public static final InputStream PROP_SELENIUM = DataBaseTestHelper.class
      .getResourceAsStream("/bod-selenium.properties");

  private static final Logger logger = LoggerFactory.getLogger(DataBaseTestHelper.class);

  private static Properties props;

  private DataBaseTestHelper() {
  };

  public static void clearSeleniumDatabaseSkipBaseData() {
    String dbUrl = getProperty(DB_URL, PROP_DEFAULT, PROP_SELENIUM);
    String dbUser = getProperty(DB_USER, PROP_DEFAULT, PROP_SELENIUM);
    String dbPassword = getProperty(DB_PASS, PROP_DEFAULT, PROP_SELENIUM);
    String dbDriverClass = getProperty(DB_DRIVER_CLASS, PROP_DEFAULT, PROP_SELENIUM);

    clearDatabaseSkipBaseData(dbUrl, dbUser, dbPassword, dbDriverClass);
  }

  public static void clearIntegrationDatabaseSkipBaseData() {
    String dbUrl = getProperty(DB_URL, PROP_DEFAULT, PROP_BOD).concat("-integration");
    String dbUser = getProperty(DB_USER, PROP_DEFAULT, PROP_BOD);
    String dbPassword = getProperty(DB_PASS, PROP_DEFAULT, PROP_BOD);
    String dbDriverClass = getProperty(DB_DRIVER_CLASS, PROP_DEFAULT, PROP_BOD);

    clearDatabaseSkipBaseData(dbUrl, dbUser, dbPassword, dbDriverClass);
  }

  private static void clearDatabaseSkipBaseData(String dbUrl, String dbUser, String dbPassword, String dbDriverClass) {
    Connection connection = createDbConnection(dbUrl, dbUser, dbPassword, dbDriverClass);

    String truncateQuery;
    Statement statement = null;
    try {
      truncateQuery = createDeleteQueriesWithoutBaseData(connection);
      statement = connection.createStatement();
      statement.executeUpdate(truncateQuery);
    }
    catch (SQLException e) {
      throw new RuntimeException(e);
    }
    finally {
      try {
        if (statement != null) {
          statement.close();
        }

        if (connection != null) {
          connection.close();
        }
      }
      catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }

    logger.warn("Cleared database [{}], deleted tables: {}", dbUrl, truncateQuery);
  }

  private static Connection createDbConnection(String dbUrl, String dbUser, String dbPass, String dbDriverClass) {

    Connection dbConnection = null;

    try {
      Class.forName(dbDriverClass).newInstance();
    }
    catch (InstantiationException e) {
      throw new RuntimeException(e);
    }
    catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

    try {
      dbConnection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
    }
    catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return dbConnection;
  }

  private static String createDeleteQueriesWithoutBaseData(Connection connection) throws SQLException {
    StringBuilder truncateQuery = new StringBuilder("TRUNCATE ");

    ResultSet tables = connection.getMetaData().getTables(null, null, "%", new String[] { "TABLE" });
    String tableName = null;
    while (tables.next()) {
      tableName = tables.getString(3);
      if ("schema_version".equals(tableName) || "institute".equals(tableName)) {
        logger.debug("Skipping truncate of table to preserve data: {}", tableName);
      }
      else {
        truncateQuery.append(tableName).append(", ");
      }
    }
    return StringUtils.removeEnd(truncateQuery.toString(), ", ").concat(" CASCADE");
  }

  private static String getProperty(String key, InputStream... propertyFiles) {

    if (props == null) {
      props = new Properties();
      try {
        for (final InputStream is : propertyFiles) {
          props.load(is);
        }
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return props.getProperty(key);
  }
}
