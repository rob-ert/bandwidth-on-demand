package nl.surfnet.bod.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.net.SMTPAppender;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Log4JMail {

  private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

  public static String MAIL_LOGER_NAME = "MAIL";

  @Resource(name = "bodEnvironment")
  private Environment bodEnvironment;

  @Value("${log4jmail.smtphost}")
  private String smpthost;

  @Value("${log4jmail.smtpport}")
  private int smptport;

  @Value("${log4jmail.smtpdebug}")
  private boolean isDebug;

  @Value("${log4jmail.subject}")
  private String subject;

  @Value("${log4jmail.to}")
  private String to;

  @Value("${log4jmail.from}")
  private String from;

  @Value("${log4jmail.pattern.layout}")
  private String patternLayout;

  @PostConstruct
  public void init() throws UnknownHostException {
    if (bodEnvironment.isDevelopment()) {
      log.info("MAIL LOGGER DISABLED!");
    }
    else {
      log.info("MAIL LOGGER ENABLED!");

      final SMTPAppender smtpAppender = new SMTPAppender();
      smtpAppender.setName(MAIL_LOGER_NAME);
      smtpAppender.setSMTPHost(smpthost);
      smtpAppender.setSMTPPort(smptport);
      smtpAppender.setSMTPDebug(isDebug);
      smtpAppender.setFrom(from);
      smtpAppender.setTo(to);
      smtpAppender.setSubject(subject + " at host: " + InetAddress.getLocalHost().getHostName());
      smtpAppender.setBufferSize(1);
      smtpAppender.setLayout(new PatternLayout(patternLayout));
      smtpAppender.setThreshold(Level.WARN);
      smtpAppender.activateOptions();
      Logger.getRootLogger().addAppender(smtpAppender);

    }
  }

  public void setEnvironment(Environment environment) {
    this.bodEnvironment = environment;
  }

}
