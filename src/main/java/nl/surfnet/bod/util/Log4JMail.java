/**
 * Copyright (c) 2012, SURFnet BV
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
package nl.surfnet.bod.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.annotation.PostConstruct;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.net.SMTPAppender;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.annotations.VisibleForTesting;

@Component
public class Log4JMail {

  private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

  public static String MAIL_LOGGER_NAME = "MAIL";

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

  @Value("${log4jmail.enabled}")
  private boolean enabled;

  @PostConstruct
  public void init() throws UnknownHostException {
    if (enabled) {
      log.info("MAIL LOGGER ENABLED!");

      final SMTPAppender smtpAppender = new SMTPAppender();
      smtpAppender.setName(MAIL_LOGGER_NAME);
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
    else {
      log.info("MAIL LOGGER DISABLED!");
      Logger.getRootLogger().removeAppender(MAIL_LOGGER_NAME);
    }
  }

  @VisibleForTesting
  void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
