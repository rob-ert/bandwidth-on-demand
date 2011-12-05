package nl.surfnet.bod.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;

public class VirtualResourceGroupFactory {

  private Long id;
  private Integer version;

  private Collection<VirtualPort> virtualPorts = new ArrayList<VirtualPort>();
  private Collection<Reservation> reservations = new ArrayList<Reservation>();
  private VirtualResourceGroup vRGroup;

  public VirtualResourceGroup create() {
    vRGroup = new VirtualResourceGroup();

    vRGroup.setId(id);
    vRGroup.setVersion(version);
    vRGroup.setVirtualPorts(virtualPorts);
    vRGroup.setReservations(reservations);

    return vRGroup;
  }

  public VirtualResourceGroupFactory setId(Long id) {
    this.id = id;
    return this;
  }

  public VirtualResourceGroupFactory setVersion(Integer version) {
    this.version = version;
    return this;
  }

  public VirtualResourceGroupFactory addVirtualPorts(VirtualPort... ports) {
    this.virtualPorts.addAll(Arrays.asList(ports));
    return this;
  }

  public VirtualResourceGroupFactory addReservations(Reservation... reservations) {
    this.reservations.addAll(Arrays.asList(reservations));
    return this;
  }

}
