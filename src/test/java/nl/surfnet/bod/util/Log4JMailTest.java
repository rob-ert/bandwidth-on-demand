package nl.surfnet.bod.util;

import static nl.surfnet.bod.util.Log4JMail.MAIL_LOGER_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Log4JMailTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void shouldNotUseMailAppenderInDevelopmentMode() {
    assertThat(getAppender(true), nullValue());
  }

  private Appender getAppender(final boolean isDevelopment) {
    final Environment environment = new Environment();
    final Log4JMail log4jMail = new Log4JMail();
    environment.setDevelopment(isDevelopment);
    log4jMail.setEnvironment(environment);
    log4jMail.init();

    final Appender appender = Logger.getRootLogger().getAppender(MAIL_LOGER_NAME);
    return appender;
  }

}
