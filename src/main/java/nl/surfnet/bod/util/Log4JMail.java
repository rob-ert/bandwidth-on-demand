package nl.surfnet.bod.util;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Log4JMail {

  private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

  public static String MAIL_LOGER_NAME = "MAIL";

  @Resource(name = "bodEnvironment")
  private Environment bodEnvironment;

  @PostConstruct
  public void init() {
    if (bodEnvironment.isDevelopment()) {
      final Logger logger = org.apache.log4j.Logger.getRootLogger();
      final Appender appender = logger.getAppender(MAIL_LOGER_NAME);
      logger.removeAppender(appender);
      log.info("USING OFFLINE MAIL LOGGER!");
    }
  }

  public void setEnvironment(Environment environment) {
    this.bodEnvironment = environment;
  }

}
