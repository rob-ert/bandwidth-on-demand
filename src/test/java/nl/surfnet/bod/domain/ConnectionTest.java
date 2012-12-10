package nl.surfnet.bod.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import nl.surfnet.bod.support.ConnectionFactory;
import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;

import org.junit.Test;


public class ConnectionTest {

  @Test
  public void getAdminGroupsWhenConnectionHasNoReservation() {
    Connection subject = new ConnectionFactory().setReservation(null).create();

    assertThat(subject.getAdminGroups(), hasSize(0));
  }

  @Test
  public void getAdminGroupsShouldHaveReservationsAdminGroups() {
    VirtualResourceGroup vrg = new VirtualResourceGroupFactory().create();
    VirtualPort source = new VirtualPortFactory().setPhysicalPortAdminGroup("urn:managers1").setVirtualResourceGroup(vrg).create();
    VirtualPort destination = new VirtualPortFactory().setPhysicalPortAdminGroup("urn:managers2").setVirtualResourceGroup(vrg).create();
    Reservation reservation = new ReservationFactory().setSourcePort(source).setDestinationPort(destination).create();
    Connection subject = new ConnectionFactory().setReservation(reservation).create();

    assertThat(subject.getAdminGroups(), hasSize(3));
  }
}
