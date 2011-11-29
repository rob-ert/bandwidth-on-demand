package nl.surfnet.bod.support;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;

public class PhysicalPortFactory {

  private String name;
  private PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().create();

  public PhysicalPort create() {
    PhysicalPort port = new PhysicalPort();
    port.setName(name);
    port.setPhysicalResourceGroup(physicalResourceGroup);

    return port;
  }

  public PhysicalPortFactory setName(String name) {
    this.name = name;
    return this;
  }
}
