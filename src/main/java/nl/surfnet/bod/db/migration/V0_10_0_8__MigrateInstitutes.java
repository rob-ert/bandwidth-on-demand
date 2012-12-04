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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.Properties;

import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.idd.IddClient;
import nl.surfnet.bod.idd.IddLiveClient;
import nl.surfnet.bod.idd.generated.Klanten;
import nl.surfnet.bod.util.Functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.base.Strings;
import com.googlecode.flyway.core.api.migration.spring.SpringJdbcMigration;

public class V0_10_0_8__MigrateInstitutes implements SpringJdbcMigration {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final String iddUrl;
  private final String iddUser;
  private final String iddPassword;
  private IddClient iddClient;

  public V0_10_0_8__MigrateInstitutes() throws IOException {
    final Properties properties = new Properties();
    properties.load(new ClassPathResource("bod-default.properties").getInputStream());

    iddUrl = getProperty("idd.url", properties);
    iddUser = getProperty("idd.user", properties);
    iddPassword = getProperty("idd.password", properties);

    iddClient = new IddLiveClient(iddUser, iddPassword, iddUrl);
  }

  private String getProperty(String name, Properties properties) {
    String result = System.getProperty(name);
    if (Strings.isNullOrEmpty(result)) {
      result = properties.getProperty(name);
    }

    return result;
  }

  @Override
  public void migrate(JdbcTemplate jdbcTemplate) throws Exception {
    logger.info("Migrating institutes using IDD @: {} with user {} and password {}", new Object[] { iddUrl, iddUser,
        iddPassword });

    Collection<Klanten> klanten = iddClient.getKlanten();

    Collection<Institute> institutes = Functions.transformKlanten(klanten, true);

    try (
      Connection connection = jdbcTemplate.getDataSource().getConnection();
      PreparedStatement insertInstitute = connection
        .prepareStatement("INSERT INTO INSTITUTE  (id, name, short_name) VALUES (?,?,?)");
    ) {
      for (Institute institute : institutes) {
        insertInstitute.setLong(1, institute.getId());
        insertInstitute.setString(2, institute.getName());
        insertInstitute.setString(3, institute.getShortName());
        insertInstitute.execute();
      }
    }
  }
}
