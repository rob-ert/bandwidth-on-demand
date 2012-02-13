package nl.surfnet.bod.service;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;

public class EmailSenderOffline extends EmailSenderOnline {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    log.info("USING MOCK EMAIL SENDER!");
  }

  @Override
  void sendMessage(SimpleMailMessage activationMessage) {
    log.info("Mock sending of message: {}", activationMessage);
  }

}
