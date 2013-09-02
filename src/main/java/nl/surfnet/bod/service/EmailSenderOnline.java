/**
 * Copyright (c) 2012, 2013 SURFnet BV
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
package nl.surfnet.bod.service;

import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import nl.surfnet.bod.domain.ActivationEmailLink;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualPortRequestLink;
import nl.surfnet.bod.domain.VirtualPortRequestLink.RequestStatus;
import nl.surfnet.bod.service.Emails.ActivationEmail;
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

public class EmailSenderOnline implements EmailSender {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Value("${mail.fromAddress}")
  private String fromAddress;

  @Value("${mail.bodTeamAddress}")
  private String bodTeamMailAddress;

  @Value("${bod.external.url}")
  private String externalBodUrl;

  @Resource
  private MailSender mailSender;

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
  public void sendActivationMail(ActivationEmailLink activationEmailLink) {
    String bodyText = ActivationEmail.body(
      generateActivationUrl(activationEmailLink).toExternalForm(), activationEmailLink.getSourceObject().getName());
    String subject = ActivationEmail.subject(activationEmailLink.getSourceObject().getName());

    SimpleMailMessage mail = new MailMessageBuilder().withTo(activationEmailLink.getToEmail()).withSubject(subject)
        .withBodyText(bodyText).create();

    send(mail);
  }

  @Override
  public void sendVirtualPortRequestMail(RichUserDetails from, VirtualPortRequestLink requestLink) {
    String link;
    if (requestLink.getStatus() == RequestStatus.DELETE_REQUESTED) {
      link = String.format(externalBodUrl + VirtualPortController.PAGE_URL + "/delete/%s", requestLink.getUuid());
    }
    else {
      link = String.format(externalBodUrl + VirtualPortController.PAGE_URL + "/create/%s", requestLink.getUuid());
    }
    SimpleMailMessage mail = new MailMessageBuilder().withTo(requestLink.getPhysicalResourceGroup().getManagerEmail())
        .withSubject(VirtualPortRequestMail.subject(from))
        .withBodyText(VirtualPortRequestMail.body(from, requestLink, link)).create();

    if (from.getEmail().isPresent()) {
      mail.setReplyTo(from.getEmail().get());
    }
    else {
      log.warn("User {} has no email address that can be used as the reply-to!", from);
    }

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

  private URL generateActivationUrl(ActivationEmailLink activationEmailLink) {
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