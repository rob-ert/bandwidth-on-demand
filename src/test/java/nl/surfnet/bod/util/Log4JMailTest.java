package nl.surfnet.bod.util;

import static nl.surfnet.bod.util.Log4JMail.MAIL_LOGER_NAME;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class Log4JMailTest {

  private Log4JMail subject;

  @Before
  public void setUp() {
    subject = new Log4JMail();
  }

  @Test
  public void shouldUseMailAppenderInEveryOtherEnvironment() throws UnknownHostException {
    subject.setEnabled("true");
    subject.init();

    assertNotNull(Logger.getRootLogger().getAppender(MAIL_LOGER_NAME));
  }

  @Test
  public void shouldNotUseMailAppenderInDevelopmentMode() throws UnknownHostException {
    subject.setEnabled("false");
    subject.init();
    assertNull(Logger.getRootLogger().getAppender(MAIL_LOGER_NAME));
  }

}
