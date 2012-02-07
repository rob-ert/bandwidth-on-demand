package nl.surfnet.bod.support;

import java.util.concurrent.atomic.AtomicLong;

import nl.surfnet.bod.domain.ActivationEmailLink;
import nl.surfnet.bod.domain.PhysicalResourceGroup;

public class ActivationEmailLinkFactory<T> {

  private static final AtomicLong COUNTER = new AtomicLong();
  private Long id = COUNTER.incrementAndGet();

  private PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().create();
  private boolean activate = false;
  private boolean emailSent = true;
  private boolean valid = true;

  public ActivationEmailLink<T> create() {
    ActivationEmailLink<T> link = new ActivationEmailLink<T>(physicalResourceGroup);
    link.setId(id);

    if (activate) {
      link.activate();
    }

    if (emailSent) {
      link.emailWasSent();
    }
        
    return link;
  }
  
  public ActivationEmailLinkFactory<T> setActivate(boolean activate) {
    this.activate = activate;
    return this;
  }

  public ActivationEmailLinkFactory<T> setEmailSent(boolean sent) {
    this.emailSent = sent;
    return this;  
  }

  public ActivationEmailLinkFactory<T> setPhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup) {
    this.physicalResourceGroup = physicalResourceGroup;
    return this;
  }

}
