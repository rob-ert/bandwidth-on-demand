/**
 * Copyright (c) 2012, 2013 SURFnet BV
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
package nl.surfnet.bod;

import java.beans.PropertyVetoException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executor;
import javax.sql.DataSource;

import org.jasypt.spring31.properties.EncryptablePropertyPlaceholderConfigurer;
import org.jasypt.util.text.StrongTextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.MessageSourceSupport;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.googlecode.flyway.core.Flyway;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;

import nl.surfnet.bod.sab.EntitlementsHandler;
import nl.surfnet.bod.service.EmailSender;

@Configuration
@ComponentScan(basePackages = "nl.surfnet.bod", excludeFilters = { @Filter(Controller.class), @Filter(Configuration.class), @Filter(Repository.class) })
@ImportResource({ "classpath:spring/appCtx-security.xml", "classpath:spring/appCtx-ws.xml" })
@EnableTransactionManagement
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableJpaRepositories(basePackages = "nl.surfnet.bod")
@EnableAsync
public class AppComponents implements AsyncConfigurer {

//  static {
//    System.setProperty("jaxb.formatted.output", "true");
//    System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
//    System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
//  }

  private static final Logger logger = LoggerFactory.getLogger(AppComponents.class);

  @Value("${jdbc.jdbcUrl}")
  private String jdbcUrl;
  @Value("${jdbc.driverClass}")
  private String driverClass;
  @Value("${jdbc.user}")
  private String jdbcUser;
  @Value("${jdbc.password}")
  private String jdbcPassword;
  @Value("${jdbc.initialPoolSize}")
  private int initialPoolSize;
  @Value("${jdbc.maxPoolSize}")
  private int maxPoolSize;
  @Value("${jdbc.minPoolSize}")
  private int minPoolSize;
  @Value("${jdbc.acquireIncrement}")
  private int acquireIncrement;
  @Value("${jdbc.acquireRetryAttempts}")
  private int acquireRetryAttempts;
  @Value("${jdbc.idleConnectionTestPeriod}")
  private int idleConnectionTestPeriod;
  @Value("${mail.sender.class}")
  private String emailSenderClass;

  @Bean
  public static PropertyPlaceholderConfigurer propertyPlaceholderConfigurer() {
    StrongTextEncryptor encryptor = new StrongTextEncryptor();
    encryptor.setPassword(System.getenv("BOD_ENCRYPTION_PASSWORD"));

    EncryptablePropertyPlaceholderConfigurer configurer = new EncryptablePropertyPlaceholderConfigurer(encryptor);
    configurer.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);

    Resource[] resources = getPropertyResources();

    logger.info("Using property files: {}", Joiner.on(",").join(resources));

    configurer.setLocations(resources);

    return configurer;
  }

  private static Resource[] getPropertyResources() {
    List<Resource> resources = Lists.newArrayList(BodProperties.getDefaultProperties());

    Resource envResource = BodProperties.getEnvProperties();
    if (envResource.exists()) {
      resources.add(envResource);
    }

    return resources.toArray(new Resource[resources.size()]);
  }

  @Bean
  public MessageSourceSupport messageSource() {
    ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
    messageSource.setBasenames("WEB-INF/i18n/messages", "WEB-INF/i18n/application");
    messageSource.setUseCodeAsDefaultMessage(true);
    return messageSource;
  }

  @Bean(name = "stunnelTranslationMap")
  @Profile("stunnel")
  @SuppressWarnings("unchecked")
  /**
   * Maps the incoming 'reply-to' host+port to our local stunnel port (which in turn delivers the message at the
   * original reply-to address)
   */
  public Optional<Map<String, String>> stunnelTranslationMap() {
    final Config configFile = ConfigFactory.load("stunnel-port-mappings");
    if (!configFile.hasPath("nsi.tlsmap")) {
      throw new IllegalStateException("stunnel port mappings config file does not contain required nsi.tlsmap entry");
    }
    final ConfigObject stunnelConfig = configFile.getObject("nsi.tlsmap");
    logger.debug("Successfully read stunnel-port-mappings.conf");

    Map<String, String> entries = new HashMap<>();
    for (Map.Entry<String,ConfigValue> entry: stunnelConfig.entrySet()) {
      String value = (String) entry.getValue().unwrapped();
      entries.put(entry.getKey(), value);
    }
    return Optional.<Map<String, String>> of(ImmutableMap.copyOf(entries));

  }

  @Bean
  public JavaMailSender mailSender(
      @Value("${mail.host}") String host,
      @Value("${mail.port}") int port,
      @Value("${mail.protocol}") String protocol,
      @Value("${mail.debug}") boolean debug) {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setDefaultEncoding("UTF-8");
    mailSender.setHost(host);
    mailSender.setPort(port);
    mailSender.setProtocol(protocol);

    Properties properties = new Properties();
    properties.put("mail.debug", debug);
    mailSender.setJavaMailProperties(properties);

    return mailSender;
  }

  @Bean
  public EmailSender emailSender() {
    return quietlyInitiateClass(emailSenderClass);
  }

  @Bean
  public EntitlementsHandler entitlementsHandler(@Value("${sab.handler.class}") String sabHandlerClass) {
    return quietlyInitiateClass(sabHandlerClass);
  }

  @Bean(initMethod = "migrate")
  public Flyway flyway() throws PropertyVetoException {
    Flyway flyway = new Flyway();
    flyway.setDataSource(dataSource());
    flyway.setLocations("db/migration", "nl.surfnet.bod.db.migration");
    flyway.setInitVersion("0");

    return flyway;
  }

  @Bean(destroyMethod = "close")
  public DataSource dataSource() throws PropertyVetoException {
    ComboPooledDataSource dataSource = new ComboPooledDataSource();
    dataSource.setJdbcUrl(jdbcUrl);
    dataSource.setDriverClass(driverClass);
    dataSource.setUser(jdbcUser);
    dataSource.setPassword(jdbcPassword);
    dataSource.setInitialPoolSize(initialPoolSize);
    dataSource.setMaxPoolSize(maxPoolSize);
    dataSource.setMinPoolSize(minPoolSize);
    dataSource.setAcquireIncrement(acquireIncrement);
    dataSource.setAcquireRetryAttempts(acquireRetryAttempts);
    dataSource.setIdleConnectionTestPeriod(idleConnectionTestPeriod);

    return dataSource;
  }

  @Bean
  @DependsOn("flyway")
  public LocalContainerEntityManagerFactoryBean entityManagerFactory() throws PropertyVetoException {
    LocalContainerEntityManagerFactoryBean emfBean = new LocalContainerEntityManagerFactoryBean();
    emfBean.setPersistenceUnitName("bod");
    emfBean.setDataSource(dataSource());

    return emfBean;
  }

  @Bean
  public PlatformTransactionManager transactionManager() {
    return new JpaTransactionManager();
  }

  // TransactionTemplate is mutable, so provide a new instance to each user.
  @Bean @Scope(value=ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
    return new TransactionTemplate(transactionManager);
  }

  @SuppressWarnings("unchecked")
  private <T> T quietlyInitiateClass(String clazz) {
    try {
      return (T) Class.forName(clazz).newInstance();
    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Executor getAsyncExecutor() {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(Thread t, Throwable e) {
        logger.error("Uncaught exception in thread " + t + ": " + e, e);
      }
    });

    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(7);
    executor.setMaxPoolSize(42);
    executor.setQueueCapacity(11);
    executor.setThreadNamePrefix("BoDExecutor-");
    executor.initialize();


    return new LoggingExecutor(new TaskExecutorAdapter(new TransactionAwareExecutor(executor)), logger);
  }
}
