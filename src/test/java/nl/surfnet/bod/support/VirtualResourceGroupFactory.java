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
  private String name;
  private String surfConnextGroupName;

  private Collection<VirtualPort> virtualPorts = new ArrayList<VirtualPort>();
  private Collection<Reservation> reservations = new ArrayList<Reservation>();

  public VirtualResourceGroup create() {
    VirtualResourceGroup vRGroup = new VirtualResourceGroup();

    vRGroup.setId(id);
    vRGroup.setVersion(version);
    vRGroup.setName(name);
    vRGroup.setSurfConnextGroupName(surfConnextGroupName);
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

  public VirtualResourceGroupFactory setName(String name) {
    this.name = name;
    return this;
  }

  public VirtualResourceGroupFactory setSurfConnextGroupName(String surfConnextGroupName) {
    this.surfConnextGroupName = surfConnextGroupName;
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
