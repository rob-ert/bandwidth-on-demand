/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
package nl.surfnet.bod.service;

import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import nl.surfnet.bod.domain.ActivationEmailLink;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualPortRequestLink;
import nl.surfnet.bod.service.Emails.ActivationEmail;
import nl.surfnet.bod.service.Emails.ErrorMail;
import nl.surfnet.bod.service.Emails.VirtualPortRequestApproveMail;
import nl.surfnet.bod.service.Emails.VirtualPortRequestDeclineMail;
import nl.surfnet.bod.service.Emails.VirtualPortRequestMail;
import nl.surfnet.bod.web.manager.ActivationEmailController;
import nl.surfnet.bod.web.manager.VirtualPortController;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.util.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class EmailSenderOnline implements EmailSender {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Value("${mail.fromAddress}")
  private String fromAddress;

  @Value("${mail.bodTeamAddress}")
  private String bodTeamMailAddress;

  @Value("${external.bod.url}")
  private String externalBodUrl;

  @Resource
  private MailSender mailSender;

  @Resource
  private LogEventService logEventService;

  /**
   * Removes a trailing slash at the end the {@link #externalBodUrl} which is
   * configurable
   */
  @PostConstruct
  protected void init() {
    if (StringUtils.endsWithIgnoreCase(externalBodUrl, "/")) {
      externalBodUrl = externalBodUrl.substring(0, externalBodUrl.length() - 1);
    }

    log.debug("Expecting BOD to be externally accessible from: {}", externalBodUrl);
  }

  @Override
  public void sendActivationMail(ActivationEmailLink<PhysicalResourceGroup> activationEmailLink) {
    String bodyText = ActivationEmail.body(generateActivationUrl(activationEmailLink).toExternalForm());
    String subject = ActivationEmail.subject(activationEmailLink.getSourceObject().getName());

    SimpleMailMessage mail = new MailMessageBuilder().withTo(activationEmailLink.getToEmail()).withSubject(subject)
        .withBodyText(bodyText).create();

    send(mail);
  }

  @Override
  public void sendVirtualPortRequestMail(RichUserDetails from, VirtualPortRequestLink requestLink) {
    String link = String.format(externalBodUrl + VirtualPortController.PAGE_URL + "/create/%s", requestLink.getUuid());

    SimpleMailMessage mail = new MailMessageBuilder().withTo(requestLink.getPhysicalResourceGroup().getManagerEmail())
        .withReplyTo(from.getEmail()).withSubject(VirtualPortRequestMail.subject(from))
        .withBodyText(VirtualPortRequestMail.body(from, requestLink, link)).create();

    send(mail);
  }

  @Override
  public void sendErrorMail(RichUserDetails user, Throwable throwable, HttpServletRequest request) {
    SimpleMailMessage mail = new MailMessageBuilder().withTo(bodTeamMailAddress)
        .withSubject(ErrorMail.subject(externalBodUrl, throwable))
        .withBodyText(ErrorMail.body(user, throwable, request)).create();

    send(mail);
  }

  @Override
  public void sendVirtualPortRequestApproveMail(VirtualPortRequestLink link, VirtualPort port) {
    SimpleMailMessage mail = new MailMessageBuilder().withTo(link.getRequestorName(), link.getRequestorEmail())
        .withSubject(VirtualPortRequestApproveMail.subject(port))
        .withBodyText(VirtualPortRequestApproveMail.body(link, port)).create();

    send(mail);
  }

  @Override
  public void sendVirtualPortRequestDeclineMail(VirtualPortRequestLink link, String declineMessage) {
    SimpleMailMessage mail = new MailMessageBuilder().withTo(link.getRequestorName(), link.getRequestorEmail())
        .withSubject(VirtualPortRequestDeclineMail.subject())
        .withBodyText(VirtualPortRequestDeclineMail.body(link, declineMessage)).create();

    send(mail);
  }

  private URL generateActivationUrl(ActivationEmailLink<PhysicalResourceGroup> activationEmailLink) {
    try {
      return new URL(String.format(externalBodUrl + "%s/%s", ActivationEmailController.ACTIVATION_MANAGER_PATH,
          activationEmailLink.getUuid()));
    }
    catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  protected void send(SimpleMailMessage mail) {
    mailSender.send(mail);
  }

  protected void setFromAddress(String fromAddress) {
    this.fromAddress = fromAddress;
  }

  protected void setExternalBodUrl(String externalBodUrl) {
    this.externalBodUrl = externalBodUrl;
  }

  protected void setBodTeamMailAddress(String bodTeamMailAddress) {
    this.bodTeamMailAddress = bodTeamMailAddress;
  }

  private final class MailMessageBuilder {
    private String to;
    private String subject;
    private String text;
    private String replyTo;

    public MailMessageBuilder() {
    }

    public SimpleMailMessage create() {
      Preconditions.checkState(Strings.emptyToNull(to) != null);
      Preconditions.checkState(Strings.emptyToNull(subject) != null);
      Preconditions.checkState(Strings.emptyToNull(text) != null);

      SimpleMailMessage mailMessage = new SimpleMailMessage();
      mailMessage.setFrom(String.format("Bandwidth on Demand <%s>", fromAddress));
      mailMessage.setTo(to);
      mailMessage.setReplyTo(replyTo);
      mailMessage.setSubject(subject);
      mailMessage.setText(text);
      return mailMessage;
    }

    public MailMessageBuilder withReplyTo(String rt) {
      this.replyTo = rt;
      return this;
    }

    public MailMessageBuilder withSubject(String sub) {
      this.subject = sub;
      return this;
    }

    public MailMessageBuilder withTo(String t) {
      this.to = t;
      return this;
    }

    public MailMessageBuilder withTo(String name, String email) {
      this.to = String.format("%s <%s>", name, email);
      return this;
    }

    public MailMessageBuilder withBodyText(String body) {
      this.text = body;
      return this;
    }
  }

}
