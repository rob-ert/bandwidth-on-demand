package nl.surfnet.bod.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import org.hibernate.validator.constraints.NotEmpty;

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

  @NotEmpty
  @Column(unique = true, nullable = false)
  private String name;

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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  /**
   * TODO remove
   * 
   * Convenience getter, since nesting bean properties is not support in the
   * view.
   * 
   * @return String name of the {@link PhysicalPort}, empty string if
   *         physicalPort is null
   */
  public String getPhysicalPortName() {
    return physicalPort == null ? "" : physicalPort.getName();
  }

  /**
   * TODO remove
   * 
   * Convenience getter, since nesting bean properties is not support in the
   * view.
   * 
   * @return String name of the {@link PhysicalPort}, empty string if
   *         physicalPort is null
   */
  public String getPhysicalResourceGroupName() {
    return physicalPort == null ? "" : physicalPort.getPhysicalResourceGroup() == null ? "" : physicalPort
        .getPhysicalResourceGroup().getName();
  }

}
