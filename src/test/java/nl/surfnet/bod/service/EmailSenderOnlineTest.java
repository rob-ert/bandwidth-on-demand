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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import nl.surfnet.bod.domain.ActivationEmailLink;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.VirtualPortRequestLink;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.support.ActivationEmailLinkFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.VirtualPortRequestLinkFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;
import nl.surfnet.bod.web.security.RichUserDetails;

@RunWith(MockitoJUnitRunner.class)
public class EmailSenderOnlineTest {

  @InjectMocks
  private EmailSenderOnline subject;

  @Mock
  private MailSender mailSenderMock;

  @Mock
  private LogEventService logEventService;

  @Captor
  private ArgumentCaptor<SimpleMailMessage> messageCaptor;

  @Before
  public void setUp() {
    subject.setExternalBodUrl("http://host/context");
  }

  @Test
  public void mailMessageShouldContainUrlWithNameAndUUID() {
    ActivationEmailLink<PhysicalResourceGroup> activationEmailLink = new ActivationEmailLinkFactory<PhysicalResourceGroup>()
        .create();

    subject.setFromAddress("test@example.com");
    subject.sendActivationMail(activationEmailLink);

    verify(mailSenderMock).send(messageCaptor.capture());

    SimpleMailMessage message = messageCaptor.getValue();

    assertThat(message.getSubject(), containsString(activationEmailLink.getSourceObject().getName()));
    assertThat(message.getText(), containsString(activationEmailLink.getUuid()));

    assertThat(message.getTo().length, is(1));
    assertThat(message.getTo()[0], is(activationEmailLink.getSourceObject().getManagerEmail()));
    assertThat(message.getFrom(), containsString("test@example.com"));
    assertThat(message.getBcc(), nullValue());
    assertThat(message.getCc(), nullValue());
    assertThat(message.getReplyTo(), nullValue());
  }

  @Test
  public void virtualPortRequestMessage() {
    RichUserDetails user = new RichUserDetailsFactory().create();
    PhysicalResourceGroup pGroup = new PhysicalResourceGroupFactory().create();
    VirtualResourceGroup vGroup = new VirtualResourceGroupFactory().create();
    Integer bandwidth = 1000;
    String requestMessage = "I would like to have a port.";

    VirtualPortRequestLink link = new VirtualPortRequestLinkFactory().setPhysicalResourceGroup(pGroup)
        .setVirtualResourceGroup(vGroup).setMessage(requestMessage).setMinBandwidth(bandwidth).create();

    subject.sendVirtualPortRequestMail(user, link);

    verify(mailSenderMock).send(messageCaptor.capture());

    SimpleMailMessage message = messageCaptor.getValue();

    assertThat(message.getReplyTo(), is(user.getEmail()));
    assertThat(message.getTo()[0], is(pGroup.getManagerEmail()));
    assertThat(message.getSubject(), containsString(user.getDisplayName()));
    assertThat(message.getText(), containsString("Institute: " + pGroup.getInstitute().getName()));
    assertThat(message.getText(), containsString("Reason: " + requestMessage));
    assertThat(message.getText(), containsString("Bandwidth: " + bandwidth));
  }
  

  @Test
  public void virtualPortRequestNullReplyToAddress() {
    RichUserDetails user = new RichUserDetailsFactory().setEmail(null).create();
    PhysicalResourceGroup pGroup = new PhysicalResourceGroupFactory().create();
    VirtualResourceGroup vGroup = new VirtualResourceGroupFactory().create();
    Integer bandwidth = 1000;
    String requestMessage = "I would like to have a port.";

    VirtualPortRequestLink link = new VirtualPortRequestLinkFactory().setPhysicalResourceGroup(pGroup)
        .setVirtualResourceGroup(vGroup).setMessage(requestMessage).setMinBandwidth(bandwidth).create();

    subject.sendVirtualPortRequestMail(user, link);

    verify(mailSenderMock).send(messageCaptor.capture());

    SimpleMailMessage message = messageCaptor.getValue();

    assertThat(message.getReplyTo(), isEmptyOrNullString());
  }

  @Test
  public void errorMailMessage() {
    RichUserDetails user = new RichUserDetailsFactory().setDisplayname("Truus Visscher").setEmail("truus@visscher.nl")
        .create();
    Throwable throwable = new RuntimeException("Something went terrible wrong");
    HttpServletRequest request = mock(HttpServletRequest.class);

    when(request.getRequestURL()).thenReturn(new StringBuffer("http://bod.dlp.surfnet.nl/error"));

    subject.setBodTeamMailAddress("bodteam@surfnet.nl");

    subject.sendErrorMail(user, throwable, request);

    verify(mailSenderMock).send(messageCaptor.capture());

    SimpleMailMessage message = messageCaptor.getValue();

    assertThat(message.getTo()[0], is("bodteam@surfnet.nl"));
    assertThat(message.getText(), containsString("Truus Visscher (truus@visscher.nl)"));
    assertThat(message.getText(), containsString("http://bod.dlp.surfnet.nl/error"));
    assertThat(message.getText(), containsString("java.lang.RuntimeException"));
    assertThat(message.getSubject(), containsString("Something went terrible wrong"));
  }
}