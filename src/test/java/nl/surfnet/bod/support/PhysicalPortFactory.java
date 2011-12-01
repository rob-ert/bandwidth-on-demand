package nl.surfnet.bod.support;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;

public class PhysicalPortFactory {

  private Long id;
  private String name;
  private PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().create();
  private Integer version;

  public PhysicalPort create() {
    PhysicalPort port = new PhysicalPort();
    port.setName(name);
    port.setId(id);
    port.setVersion(version);
    port.setPhysicalResourceGroup(physicalResourceGroup);

    return port;
  }

  public PhysicalPortFactory setId(Long id) {
    this.id = id;
    return this;
  }

  public PhysicalPortFactory setVersion(Integer version) {
    this.version = version;
    return this;
  }

  public PhysicalPortFactory setName(String name) {
    this.name = name;
    return this;
  }
}
