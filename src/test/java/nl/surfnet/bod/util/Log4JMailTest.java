package nl.surfnet.bod.util;

import static nl.surfnet.bod.util.Log4JMail.MAIL_LOGER_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Log4JMailTest {

  @Mock
  private Environment bodEnvironment;

  @InjectMocks
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
  public void shouldUseMailAppenderInEveryOtherEnvironment() throws UnknownHostException {
    prepareSubject(false);
    assertThat(Logger.getRootLogger().getAppender(MAIL_LOGER_NAME), notNullValue());
  }

  @Test
  public void shouldNotUseMailAppenderInDevelopmentMode() throws UnknownHostException {
    prepareSubject(true);
    assertThat(Logger.getRootLogger().getAppender(MAIL_LOGER_NAME), nullValue());
  }

  private void prepareSubject(boolean isDevelopment) throws UnknownHostException {
    when(bodEnvironment.isDevelopment()).thenReturn(isDevelopment);
    subject.init();
  }

}
