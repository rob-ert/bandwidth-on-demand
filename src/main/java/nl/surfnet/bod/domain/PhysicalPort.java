package nl.surfnet.bod.domain;

import javax.persistence.Column;
import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import org.hibernate.validator.constraints.NotEmpty;

@Entity
public class PhysicalPort {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Version
  private Integer version;

  @NotEmpty
  @Column(unique = true, nullable = false)
  private String name;

  @Nullable
  private String displayName;

  @ManyToOne
  private PhysicalResourceGroup physicalResourceGroup;

  public Long getId() {
    return this.id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public Integer getVersion() {
    return this.version;
  }

  public void setVersion(final Integer version) {
    this.version = version;
  }

  public String getName() {
    return this.name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public PhysicalResourceGroup getPhysicalResourceGroup() {
    return this.physicalResourceGroup;
  }

  public void setPhysicalResourceGroup(final PhysicalResourceGroup physicalResourceGroup) {
    this.physicalResourceGroup = physicalResourceGroup;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Id: ").append(getId()).append(", ");
    sb.append("Name: ").append(getName()).append(", ");
    sb.append("PhysicalResourceGroup: ").append(getPhysicalResourceGroup()).append(", ");
    sb.append("Version: ").append(getVersion());
    return sb.toString();
  }

  public void setDisplayName(final String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

}
