package nl.surfnet.bod.domain;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.persistence.Column;
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

import org.springframework.format.annotation.DateTimeFormat;

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

  @Column(nullable=false)
  private String surfConnextGroupId;

  @ManyToOne
  private VirtualResourceGroup virtualResourceGroup;

  @Enumerated(EnumType.STRING)
  private ReservationStatus reservationStatus;

  @ManyToOne
  private VirtualPort sourcePort;

  @ManyToOne
  private VirtualPort endPort;

  @DateTimeFormat(style = "S-")
  @Temporal(TemporalType.DATE)
  @Column(nullable=false)
  private Date startDate;

  @DateTimeFormat(style = "-S")
  @Temporal(TemporalType.TIME)
  @Column(nullable=false)
  private Date startTime;

  @DateTimeFormat(style = "S-")
  @Temporal(TemporalType.DATE)
  @Column(nullable=false)
  private Date endDate;

  @DateTimeFormat(style = "-S")
  @Temporal(TemporalType.TIME)
  @Column(nullable=false)
  private Date endTime;

  @Column(nullable=false)
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

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public Date getEndTime() {
    return endTime;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
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
