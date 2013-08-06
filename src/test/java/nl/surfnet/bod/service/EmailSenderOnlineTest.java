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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;

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
    ActivationEmailLink activationEmailLink = new ActivationEmailLinkFactory()
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
    Long bandwidth = 1000L;
    String requestMessage = "I would like to have a port.";

    VirtualPortRequestLink link = new VirtualPortRequestLinkFactory().setPhysicalResourceGroup(pGroup)
        .setVirtualResourceGroup(vGroup).setMessage(requestMessage).setMinBandwidth(bandwidth).create();

    subject.sendVirtualPortRequestMail(user, link);

    verify(mailSenderMock).send(messageCaptor.capture());

    SimpleMailMessage message = messageCaptor.getValue();

    assertThat(message.getReplyTo(), is(user.getEmail().get()));
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
    Long bandwidth = 1000L;
    String requestMessage = "I would like to have a port.";

    VirtualPortRequestLink link = new VirtualPortRequestLinkFactory().setPhysicalResourceGroup(pGroup)
        .setVirtualResourceGroup(vGroup).setMessage(requestMessage).setMinBandwidth(bandwidth).create();

    subject.sendVirtualPortRequestMail(user, link);

    verify(mailSenderMock).send(messageCaptor.capture());

    SimpleMailMessage message = messageCaptor.getValue();

    assertThat(message.getReplyTo(), isEmptyOrNullString());
  }
}
