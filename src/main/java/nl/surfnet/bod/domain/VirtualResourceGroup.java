package nl.surfnet.bod.domain;

import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;

/**
 * Entity which represents a List of {@link VirtualPort}s which belong together
 * and to the {@link Reservation}s which are related to this group.
 * 
 * @author Franky
 * 
 */
@Entity
public class VirtualResourceGroup {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Version
  private Integer version;

  @OneToMany(mappedBy = "virtualResourceGroup")
  private Collection<VirtualPort> virtualPorts;

  @OneToMany(mappedBy = "virtualResourceGroup")
  private Collection<Reservation> reservations;

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

  public Collection<VirtualPort> getVirtualPorts() {
    return virtualPorts;
  }

  public void setVirtualPorts(Collection<VirtualPort> virtualPorts) {
    this.virtualPorts = virtualPorts;
  }

  public Collection<Reservation> getReservations() {
    return reservations;
  }

  public void setReservations(Collection<Reservation> reservations) {
    this.reservations = reservations;
  }
}
