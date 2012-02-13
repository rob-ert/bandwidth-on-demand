package nl.surfnet.bod.service;

import nl.surfnet.bod.domain.ActivationEmailLink;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.web.security.RichUserDetails;

public interface EmailSender {

  void sendActivationMail(ActivationEmailLink<PhysicalResourceGroup> activationEmailLink);

  void sendVirtualPortRequestMail(RichUserDetails from, PhysicalResourceGroup pGroup, VirtualResourceGroup vGroup,
      String requestMessage);

}
