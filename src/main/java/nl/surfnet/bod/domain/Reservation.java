package nl.surfnet.bod.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

/**
 * Entity which represents a Reserveration for a specific connection between a
 * source and a destination point on a specific moment in time.
 * 
 * @author Franky
 * 
 */
@Entity
public class Reservation {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Version
  private Integer version;

  private String surfConnextGroupId;

  @ManyToOne
  private VirtualResourceGroup virtualResourceGroup;

  @Enumerated(EnumType.STRING)
  private ReservationStatus reservationStatus;

  @ManyToOne
  private VirtualPort sourcePort;

  @ManyToOne
  private VirtualPort endPort;

  @Temporal(TemporalType.TIMESTAMP)
  private Date startTimeStamp;

  @Temporal(TemporalType.TIMESTAMP)
  private Date endTimeStamp;

  private String user;

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

  public VirtualPort getSourcePort() {
    return sourcePort;
  }

  public void setSourcePort(VirtualPort sourcePort) {
    this.sourcePort = sourcePort;
  }

  public VirtualPort getEndPort() {
    return endPort;
  }

  public void setEndPort(VirtualPort endPort) {
    this.endPort = endPort;
  }

  public Date getStartTimeStamp() {
    return startTimeStamp;
  }

  public void setStartTimeStamp(Date startTimeStamp) {
    this.startTimeStamp = startTimeStamp;
  }

  public Date getEndTimeStamp() {
    return endTimeStamp;
  }

  public void setEndTimeStamp(Date endTimeStamp) {
    this.endTimeStamp = endTimeStamp;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  /**
   * TODO Convenience method since bean notation is not allowed in view
   * 
   */
  public String getVirtualResourceGroupName() {
    return this.virtualResourceGroup == null ? "" : this.virtualResourceGroup.getName();
  }
}
