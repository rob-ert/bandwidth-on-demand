package nl.surfnet.bod.support;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.domain.VirtualResourceGroup;

public class ReservationFactory {

  private Long id;
  private Integer version;
  private VirtualResourceGroup vRGroup = new VirtualResourceGroupFactory().create();
  private ReservationStatus status = ReservationStatus.NEW;
  private Reservation reservation;

  public Reservation create() {

    reservation = new Reservation();

    reservation.setId(id);
    reservation.setVersion(version);
    reservation.setVirtualResourceGroup(vRGroup);
    reservation.setReservationStatus(status);

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
    this.status = status;
    return this;
  }

}
