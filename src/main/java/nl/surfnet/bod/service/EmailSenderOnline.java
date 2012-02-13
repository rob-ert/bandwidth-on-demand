package nl.surfnet.bod.service;

import java.net.MalformedURLException;
import java.net.URL;

import nl.surfnet.bod.domain.ActivationEmailLink;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.web.manager.ActivationEmailController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.util.StringUtils;

public class EmailSenderOnline implements EmailSender {

  @Value("${mail.fromAddress}")
  private String fromAddress;

  @Value("${external.bod.url}")
  private String externalBodUrl;

  @Autowired
  private MailSender mailSender;

  /*
   * (non-Javadoc)
   * 
   * @see
   * nl.surfnet.bod.service.EmailSender#sendActivationMail(nl.surfnet.bod.domain
   * .ActivationEmailLink)
   */
  @Override
  public void sendActivationMail(ActivationEmailLink<PhysicalResourceGroup> activationEmailLink) {
    SimpleMailMessage activationMessage = new SimpleMailMessage();
    activationMessage.setTo(activationEmailLink.getToEmail());
    activationMessage.setFrom(fromAddress);
    activationMessage.setSubject("Activation mail for Physical Resource Group "
        + activationEmailLink.getSourceObject().getName());

    String body = String.format("Please click the link to activate this email adres for physical resource group: %s",
        generateActivationUrl(activationEmailLink).toExternalForm());

    activationMessage.setText(body);

    sendMessage(activationMessage);
  }

  void sendMessage(SimpleMailMessage activationMessage) {
    mailSender.send(activationMessage);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * nl.surfnet.bod.service.EmailSender#sendVirtualPortRequestMail(java.lang
   * .String, java.lang.String)
   */
  @Override
  public void sendVirtualPortRequestMail(String to, String from) {
    SimpleMailMessage mail = new SimpleMailMessage();
    mail.setTo(to);
    mail.setReplyTo(from);
    mail.setFrom(fromAddress);
    mail.setText("hallo...");
  }

  private URL generateActivationUrl(ActivationEmailLink<PhysicalResourceGroup> activationEmailLink) {
    if (!StringUtils.endsWithIgnoreCase(externalBodUrl, "/")) {
      externalBodUrl = externalBodUrl + "/";
    }

    try {
      return new URL(String.format(externalBodUrl + "%s/%s", ActivationEmailController.ACTIVATION_MANAGER_PATH,
          activationEmailLink.getUuid()));
    }
    catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  public void setFromAddress(String fromAddress) {
    this.fromAddress = fromAddress;
  }

  public void setExternalBodUrl(String externalBodUrl) {
    this.externalBodUrl = externalBodUrl;
  }

}
