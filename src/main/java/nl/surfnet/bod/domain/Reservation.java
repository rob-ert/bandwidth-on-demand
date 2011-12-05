package nl.surfnet.bod.domain;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

@Entity
public class Reservation {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Version
  private Integer version;
  
  @Basic
  private String surfConnextGroupId;

  @ManyToOne
  private VirtualResourceGroup virtualResourceGroup;

  @Enumerated(EnumType.STRING)
  private ReservationStatus reservationStatus;

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

  public String getSurfConnextGroupId() {
    return surfConnextGroupId;
  }

  public void setSurfConnextGroupId(String surfConnextGroupId) {
    this.surfConnextGroupId = surfConnextGroupId;
  }

  public VirtualResourceGroup getVirtualResourceGroup() {
    return virtualResourceGroup;
  }

  public void setVirtualResourceGroup(VirtualResourceGroup virtualResourceGroup) {
    this.virtualResourceGroup = virtualResourceGroup;
  }

  public ReservationStatus getReservationStatus() {
    return reservationStatus;
  }

  public void setReservationStatus(ReservationStatus reservationStatus) {
    this.reservationStatus = reservationStatus;
  }



}
