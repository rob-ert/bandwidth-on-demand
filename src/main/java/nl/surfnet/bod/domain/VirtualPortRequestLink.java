package nl.surfnet.bod.domain;

import java.util.UUID;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.joda.time.LocalDateTime;

@Entity
public class VirtualPortRequestLink {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Version
  private Integer version;

  @NotNull
  @Column(nullable = false)
  private String uuid = UUID.randomUUID().toString();;

  private String requestor;

  @NotNull
  @Type(type = "org.joda.time.contrib.hibernate.PersistentLocalDateTime")
  private LocalDateTime requestDateTime;

  @NotNull
  @ManyToOne
  private VirtualResourceGroup virtualResourceGroup;

  @NotNull
  @ManyToOne
  private PhysicalResourceGroup physicalResourceGroup;

  private String message;

  private Integer minBandwidth;

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

  public VirtualResourceGroup getVirtualResourceGroup() {
    return virtualResourceGroup;
  }

  public void setVirtualResourceGroup(VirtualResourceGroup virtualResourceGroup) {
    this.virtualResourceGroup = virtualResourceGroup;
  }

  public PhysicalResourceGroup getPhysicalResourceGroup() {
    return physicalResourceGroup;
  }

  public void setPhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup) {
    this.physicalResourceGroup = physicalResourceGroup;
  }

  public LocalDateTime getRequestDateTime() {
    return requestDateTime;
  }

  public void setRequestDateTime(LocalDateTime requestDateTime) {
    this.requestDateTime = requestDateTime;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Integer getMinBandwidth() {
    return minBandwidth;
  }

  public void setMinBandwidth(Integer minBandwidth) {
    this.minBandwidth = minBandwidth;
  }

  public String getRequestor() {
    return requestor;
  }

  public void setRequestor(String requestor) {
    this.requestor = requestor;
  }
}
