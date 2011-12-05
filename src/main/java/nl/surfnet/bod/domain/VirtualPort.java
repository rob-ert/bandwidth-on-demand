package nl.surfnet.bod.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

/**
 * Entity which represents a VirtualPort which is mapped to a
 * {@link PhysicalPort} and is related to a {@link VirtualResourceGroup}
 * 
 * @author Franky
 * 
 */
@Entity
public class VirtualPort {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Version
  private Integer version;

  @ManyToOne
  private VirtualResourceGroup virtualResourceGroup;

  @ManyToOne
  private PhysicalPort physicalPort;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public VirtualResourceGroup getVirtualResourceGroup() {
    return virtualResourceGroup;
  }

  public void setVirtualResourceGroup(VirtualResourceGroup virtualResourceGroup) {
    this.virtualResourceGroup = virtualResourceGroup;
  }

  public PhysicalPort getPhysicalPort() {
    return physicalPort;
  }

  public void setPhysicalPort(PhysicalPort physicalPort) {
    this.physicalPort = physicalPort;
  }

}
