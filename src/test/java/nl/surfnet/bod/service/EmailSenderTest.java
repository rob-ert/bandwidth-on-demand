package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import nl.surfnet.bod.domain.ActivationEmailLink;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.support.ActivationEmailLinkFactory;

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
public class EmailSenderTest {

  @InjectMocks
  private EmailSenderOnline subject;

  @Mock
  private MailSender mailSenderMock;
  
  
  @Before
  public void setUp() {
    subject.setExternalBodUrl("http://host/context");
  }

  @Captor
  private ArgumentCaptor<SimpleMailMessage> messageCaptor;

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
    assertThat(message.getFrom(), is("test@example.com"));
    assertThat(message.getBcc(), nullValue());
    assertThat(message.getCc(), nullValue());
    assertThat(message.getReplyTo(), nullValue());
  }
}