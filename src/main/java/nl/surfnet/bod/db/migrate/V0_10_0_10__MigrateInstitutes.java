package nl.surfnet.bod.db.migrate;

import nl.surfnet.bod.service.InstituteService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.googlecode.flyway.core.migration.java.JavaMigration;

public class V0_10_0_10__MigrateInstitutes implements JavaMigration {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private InstituteService instituteService;

  @Override
  public void migrate(JdbcTemplate jdbcTemplate) throws Exception {
    logger.info("Migrating institutes");
    instituteService.refreshInstitutes();
  }
}
