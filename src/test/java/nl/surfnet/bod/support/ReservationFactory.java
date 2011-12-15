package nl.surfnet.bod.support;

import java.util.Date;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;

public class ReservationFactory {

  private Long id;

  private Integer version;

  private String surfConnextGroupId;

  private VirtualResourceGroup vRGroup = new VirtualResourceGroupFactory().create();

  private ReservationStatus reservationStatus = ReservationStatus.NEW;

  private VirtualPort sourcePort;

  private VirtualPort endPort;

  private Date startTimeStamp;

  private Date endTimeStamp;

  private String user;

  public Reservation create() {

    Reservation reservation = new Reservation();

    reservation.setId(id);
    reservation.setVersion(version);
    reservation.setSurfConnextGroupId(surfConnextGroupId);
    reservation.setReservationStatus(reservationStatus);
    reservation.setSourcePort(sourcePort);
    reservation.setEndPort(endPort);
    reservation.setVirtualResourceGroup(vRGroup);
    reservation.setStartDate(startTimeStamp);
    reservation.setStartTime(startTimeStamp);
    reservation.setEndDate(endTimeStamp);
    reservation.setEndTime(endTimeStamp);
    reservation.setUser(user);

    return reservation;
  }

  public ReservationFactory setId(Long id) {
    this.id = id;
    return this;
  }

  public ReservationFactory setVersion(Integer version) {
    this.version = version;
    return this;
  }

  public ReservationFactory setVirtualResourceGroup(VirtualResourceGroup vRGroup) {
    this.vRGroup = vRGroup;
    return this;
  }

  public ReservationFactory setReservationStatus(ReservationStatus status) {
    this.reservationStatus = status;
    return this;
  }

  public ReservationFactory setSurfConnextGroupId(String surfConnextGroupId) {
    this.surfConnextGroupId = surfConnextGroupId;
    return this;
  }

  public ReservationFactory setSourcePort(VirtualPort sourcePort) {
    this.sourcePort = sourcePort;
    return this;
  }

  public ReservationFactory setEndPort(VirtualPort endPort) {
    this.endPort = endPort;
    return this;
  }

  public ReservationFactory setStartTimeStamp(Date startTimeStamp) {
    this.startTimeStamp = startTimeStamp;
    return this;
  }

  public ReservationFactory setEndTimeStamp(Date endTimeStamp) {
    this.endTimeStamp = endTimeStamp;
    return this;
  }

  public ReservationFactory setUser(String user) {
    this.user = user;
    return this;
  }
}
