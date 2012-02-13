package nl.surfnet.bod.service;

import java.net.MalformedURLException;
import java.net.URL;

import nl.surfnet.bod.domain.ActivationEmailLink;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.web.manager.ActivationEmailController;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

@Service
public class EmailSender {

  private static final String ACTIVATION_BODY =
      "Please click the link to activate this email adres for physical resource group: %s";

  private static final String VIRTUAL_PORT_REQUEST_BODY =
      "Dear ICT Manager,\n\n"
      + "You have received a new Virtual Port Request.\n\n"
      + "Who: %s (%s)\n"
      + "Physical Resource Group: %s\n"
      + "Virtual Resource Group: %s\n"
      + "Reason: %s\n\n"
      + "Click on the following link %s to create the virtual port";

  @Value("${mail.fromAddress}")
  private String fromAddress;

  @Autowired
  private MailSender mailSender;

  public void sendActivationMail(ActivationEmailLink<PhysicalResourceGroup> activationEmailLink) {
    String bodyText = String.format(ACTIVATION_BODY,
        generateActivationUrl(activationEmailLink).toExternalForm());

    SimpleMailMessage mail = new MailMessageBuilder()
      .withTo(activationEmailLink.getToEmail())
      .withSubject("Activation mail for Physical Resource Group " + activationEmailLink.getSourceObject().getName())
      .withBodyText(bodyText)
      .create();

    mailSender.send(mail);
  }

  public void sendVirtualPortRequestMail(RichUserDetails from,
      PhysicalResourceGroup pGroup, VirtualResourceGroup vGroup, String requestMessage) {
    String link = String.format(
        "http://localhost:8082/bod/manager/virtualports/create?vgroup=%d&pgroup=%d", vGroup.getId(), pGroup.getId());

    SimpleMailMessage mail = new MailMessageBuilder()
      .withTo(pGroup.getManagerEmail())
      .withReplyTo(from.getEmail())
      .withSubject("A Virtual Port Request")
      .withBodyText(
          String.format(VIRTUAL_PORT_REQUEST_BODY,
              from.getDisplayName(), from.getEmail(), pGroup.getInstitute().getName(), vGroup.getName(), requestMessage, link))
      .create();

    mailSender.send(mail);
  }

  private URL generateActivationUrl(ActivationEmailLink<PhysicalResourceGroup> activationEmailLink) {
    try {
      // FIXME AvD localhost will fail!!
      return new URL(String.format("http://localhost:8082/bod%s/%s", ActivationEmailController.ACTIVATION_MANAGER_PATH,
          activationEmailLink.getUuid()));
    }
    catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  protected void setFromAddress(String fromAddress) {
    this.fromAddress = fromAddress;
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

    public MailMessageBuilder withReplyTo(String replyTo) {
      this.replyTo = replyTo;
      return this;
    }

    public MailMessageBuilder withSubject(String subject) {
      this.subject = subject;
      return this;
    }

    public MailMessageBuilder withTo(String to) {
      this.to = to;
      return this;
    }

    public MailMessageBuilder withBodyText(String text) {
      this.text = text;
      return this;
    }
  }

}
