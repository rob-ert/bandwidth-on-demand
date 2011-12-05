package nl.surfnet.bod.domain;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;

import org.junit.Before;
import org.junit.Test;

public class VirtualResourceGroupTest {

  private VirtualResourceGroup virtualResourceGroup;
  private Reservation reservation;
  private VirtualPort virtualPort;

  @Before
  public void setUp() {
    reservation = new ReservationFactory().create();
    virtualPort = new VirtualPortFactory().create();

    virtualResourceGroup = new VirtualResourceGroupFactory().create();

  }

  @Test
  public void test() {
    virtualResourceGroup = new VirtualResourceGroupFactory().addReservations(reservation).addVirtualPorts(virtualPort)
        .create();

    assertEquals(Arrays.asList(reservation), virtualResourceGroup.getReservations());
    assertEquals(Arrays.asList(virtualPort), virtualResourceGroup.getVirtualPorts());
  }

}
