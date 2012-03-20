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

import nl.surfnet.bod.domain.ActivationEmailLink;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.web.manager.ActivationEmailController;
import nl.surfnet.bod.web.manager.VirtualPortController;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.util.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class EmailSenderOnline implements EmailSender {

  private static final String ACTIVATION_BODY = //
      "Dear ICT Manager,\n\n" //
      + "Please click the link to activate this email adres for physical resource group: %s\n\n"
      + "Kind regards,\n" //
      + "The bandwidth on Demand Application team";

  private static final String VIRTUAL_PORT_REQUEST_BODY = //
        "Dear ICT Manager,\n\n" //
      + "You have received a new Virtual Port Request.\n\n" //
      + "Who: %s (%s)\n" //
      + "Physical Resource Group: %s\n" //
      + "Virtual Resource Group: %s\n" //
      + "Minimum Bandwidth: %d Mbit/s\n" //
      + "Reason: %s\n\n" //
      + "Click on the following link %s to create the virtual port.\n\n" //
      + "Kind regards,\n" //
      + "The Bandwidth on Demand Application team";

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Value("${mail.fromAddress}")
  private String fromAddress;

  @Value("${external.bod.url}")
  private String externalBodUrl;

  @Autowired
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
  public void sendActivationMail(ActivationEmailLink<PhysicalResourceGroup> activationEmailLink) {
    String bodyText = String.format(ACTIVATION_BODY, generateActivationUrl(activationEmailLink).toExternalForm());

    SimpleMailMessage mail = new MailMessageBuilder()
        .withTo(activationEmailLink.getToEmail())
        .withSubject("[BoD] Activation mail for Physical Resource Group " + activationEmailLink.getSourceObject().getName())
        .withBodyText(bodyText).create();

    send(mail);
  }

  @Override
  public void sendVirtualPortRequestMail(RichUserDetails from, PhysicalResourceGroup pGroup,
      UserGroup userGroup, Integer bandwidth, String requestMessage) {
    String link = String.format(externalBodUrl + VirtualPortController.PAGE_URL + "/create?team=%s&pgroup=%d&bandwidth=%s",
        userGroup.getId(), pGroup.getId(), bandwidth);

    SimpleMailMessage mail = new MailMessageBuilder()
        .withTo(pGroup.getManagerEmail())
        .withReplyTo(from.getEmail())
        .withSubject(String.format("[BoD] A Virtual Port Request for %s", pGroup.getInstitute().getName()))
        .withBodyText(
            String.format(VIRTUAL_PORT_REQUEST_BODY, from.getDisplayName(), from.getEmail(), pGroup.getInstitute()
                .getName(), userGroup.getName(), bandwidth, requestMessage, link)).create();

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
      mailMessage.setFrom(fromAddress);
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

    public MailMessageBuilder withBodyText(String body) {
      this.text = body;
      return this;
    }
  }

}
