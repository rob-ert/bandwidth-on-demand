package nl.surfnet.bod.service;

import nl.surfnet.bod.domain.ActivationEmailLink;
import nl.surfnet.bod.domain.PhysicalResourceGroup;

public interface EmailSender {

  void sendActivationMail(ActivationEmailLink<PhysicalResourceGroup> activationEmailLink);

  void sendVirtualPortRequestMail(String to, String from);
  
 void setFromAddress(String fromAddress);

}