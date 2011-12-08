package nl.surfnet.bod.support;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.support.PhysicalPortFactory;

/**
 * Factory for creation of {@link VirtualPort}
 * 
 * @author Franky
 * 
 */
public class VirtualPortFactory {

  private Long id;
  private Integer version;
  private VirtualPort virtualPort;

  private String name;
  private VirtualResourceGroup virtualResourceGroup = new VirtualResourceGroupFactory().create();
  private PhysicalPort physicalPort = new PhysicalPortFactory().create();

  public VirtualPort create() {
    virtualPort = new VirtualPort();

    virtualPort.setId(id);
    virtualPort.setVersion(version);

    virtualPort.setVirtualResourceGroup(virtualResourceGroup);
    virtualPort.setPhysicalPort(physicalPort);

    return virtualPort;
  }

  public VirtualPortFactory setName(String name){
    this.name = name;
    return this;
  }
    
  
  public VirtualPortFactory setVirtualResourceGroup(VirtualResourceGroup virtualResourceGroup) {
    this.virtualResourceGroup = virtualResourceGroup;
    return this;
  }

  public VirtualPortFactory setPhysicalPort(PhysicalPort physicalPort) {
    this.physicalPort = physicalPort;
    return this;
  }
  
  
  

}
