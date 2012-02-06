package nl.surfnet.bod.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.joda.time.LocalDateTime;

@Entity
public class ActivationEmailLink {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Version
  private Integer version;

  @NotNull
  @Column(nullable = false)
  private String uuid;

  @NotNull
  @Type(type = "org.joda.time.contrib.hibernate.PersistentLocalDateTime")
  @Column(nullable = false)
  private LocalDateTime creationDateTime;

  @ManyToOne(optional = false)
  private PhysicalResourceGroup physicalResourceGroup;

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

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public LocalDateTime getCreationDateTime() {
    return creationDateTime;
  }

  public void setCreationDateTime(LocalDateTime creationDateTime) {
    this.creationDateTime = creationDateTime;
  }

  public PhysicalResourceGroup getPhysicalResourceGroup() {
    return physicalResourceGroup;
  }

  public void setPhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup) {
    this.physicalResourceGroup = physicalResourceGroup;
  }

}
