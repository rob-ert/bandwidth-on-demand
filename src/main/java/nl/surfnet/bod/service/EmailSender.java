package nl.surfnet.bod.service;

import java.net.MalformedURLException;
import java.net.URL;

import nl.surfnet.bod.domain.ActivationEmailLink;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.repo.ActivationEmailLinkRepo;
import nl.surfnet.bod.web.manager.ActivationEmailController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class EmailSender {

  @Value("${mail.fromAddress}")
  private String fromAddress;

  @Autowired
  private MailSender mailSender;

  @Autowired
  private ActivationEmailLinkRepo activationEmailLinkRepo;

  public void sendActivationMail(ActivationEmailLink<PhysicalResourceGroup> activationEmailLink) {
    SimpleMailMessage activationMessage = new SimpleMailMessage();
    activationMessage.setTo(activationEmailLink.getToEmail());
    activationMessage.setFrom(fromAddress);
    activationMessage.setSubject("Activation mail for Physical Resource Group "
        + activationEmailLink.getSourceObject().getName());

    URL activationUrl = generateActivationUrl(activationEmailLink);

    StringBuffer text = new StringBuffer(
        "Please click the link below to activate this email adres for physical resource group: ");
    text.append(activationUrl.toExternalForm());

    activationMessage.setText(text.toString());

    try {
      mailSender.send(activationMessage);
      activationEmailLink.emailWasSent();
      activationEmailLinkRepo.save(activationEmailLink);
    }
    catch (MailException exc) {
      throw new RuntimeException(exc);
    }
  }

  public void sendVirtualPortRequestMail() {
    // TODO AvD
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

}
