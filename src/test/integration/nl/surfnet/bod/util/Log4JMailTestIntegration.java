package nl.surfnet.bod.util;

import static nl.surfnet.bod.util.Log4JMail.MAIL_LOGER_NAME;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.UnknownHostException;

import javax.annotation.Resource;

import nl.surfnet.bod.AppConfiguration;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { AppConfiguration.class })
public class Log4JMailTestIntegration {

  @Resource
  private Environment bodEnvironment;

  @Resource
  private Log4JMail subject;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    TestHelper.testProperties();
  }

  @Before
  public void setup() throws UnknownHostException {

  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void shouldUseMailAppenderInEveryOtherMode() throws UnknownHostException {
    prepareSubject(false);
    assertNotNull(Logger.getRootLogger().getAppender(MAIL_LOGER_NAME));
    org.slf4j.Logger log = LoggerFactory.getLogger(getClass());
    log.error("Boem", new Exception());
  }

  @Test
  public void shouldNotUseMailAppenderInDevelopmentMode() throws UnknownHostException {
    prepareSubject(true);
    assertNull(Logger.getRootLogger().getAppender(MAIL_LOGER_NAME));
  }

  private void prepareSubject(boolean isDevelopment) throws UnknownHostException {
    bodEnvironment.setDevelopment(isDevelopment);
    subject.setEnvironment(bodEnvironment);
    subject.init();
  }

}
