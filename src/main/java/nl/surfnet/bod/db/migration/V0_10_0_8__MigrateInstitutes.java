package nl.surfnet.bod.db.migration;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;
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
import com.googlecode.flyway.core.migration.java.JavaMigration;

public class V0_10_0_8__MigrateInstitutes implements JavaMigration {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private IddClient iddClient;

  public V0_10_0_8__MigrateInstitutes() throws IOException {

    final Properties properties = new Properties();
    properties.load(new ClassPathResource("bod-default.properties").getInputStream());

    String iddUrl = getProperty("idd.url", properties);
    String iddUser = getProperty("idd.user", properties);
    String iddPassword = getProperty("idd.password", properties);

    logger.info("Using IDD @: {} with user {} and password {}", new Object[] { iddUrl, iddUser, iddPassword });

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
    logger.info("Migrating institutes");

    Collection<Klanten> klanten = iddClient.getKlanten();

    List<Institute> institutes = Functions.transformKlanten(klanten, true);

    PreparedStatement insertInstitute = jdbcTemplate.getDataSource().getConnection()
        .prepareStatement("INSERT INTO INSTITUTE  (id, name, short_name) VALUES (?,?,?)");

    for (Institute institute : institutes) {
      insertInstitute.setLong(1, institute.getId());
      insertInstitute.setString(2, institute.getName());
      insertInstitute.setString(3, institute.getShortName());

      insertInstitute.execute();
    }

  }
}
