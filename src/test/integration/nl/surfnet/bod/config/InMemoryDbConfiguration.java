package nl.surfnet.bod.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import com.googlecode.flyway.core.Flyway;

@Configuration
public class InMemoryDbConfiguration {

  private static final String jdbcUrl = "jdbc:h2:mem:bod-tmp-db;FILE_LOCK=NO;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0;TRACE_LEVEL_FILE=2;MVCC=true";

  @Bean
  public DataSource dataSource() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName("org.h2.Driver");
    dataSource.setUrl(jdbcUrl);
    dataSource.setUsername("sa");
    dataSource.setPassword("");
    return dataSource;
  }

  @Bean
  public Flyway flyway() {
    return null;
  }

  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
    LocalContainerEntityManagerFactoryBean emfBean = new LocalContainerEntityManagerFactoryBean();
    emfBean.setPersistenceUnitName("testDb");
    emfBean.setDataSource(dataSource());
    return emfBean;
  }
}
