package nl.surfnet.bod.service;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;

public class EmailSenderOffline extends EmailSenderOnline {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @SuppressWarnings("unused")
  @PostConstruct
  protected void init() {
    super.init();
    log.info("USING MOCK EMAIL SENDER!");
  }

  @Override
  public void send(SimpleMailMessage activationMessage) {
    log.info("Mock sending of message: {}", activationMessage);
  }

}
