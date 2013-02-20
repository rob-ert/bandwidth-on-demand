package nl.surfnet.bod.util;

import static nl.surfnet.bod.util.Log4JMail.MAIL_LOGER_NAME;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
    assertNotNull(Logger.getRootLogger().getAppender(MAIL_LOGER_NAME));
  }

  @Test
  public void shouldNotUseMailAppenderInDevelopmentMode() throws UnknownHostException {
    prepareSubject(true);
    assertNull(Logger.getRootLogger().getAppender(MAIL_LOGER_NAME));
  }

  private void prepareSubject(boolean isDevelopment) throws UnknownHostException {
    when(bodEnvironment.isDevelopment()).thenReturn(isDevelopment);
    subject.init();
  }

}
