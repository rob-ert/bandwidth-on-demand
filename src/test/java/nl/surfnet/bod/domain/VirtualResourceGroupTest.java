package nl.surfnet.bod.domain;

import static org.junit.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


import java.util.Arrays;

import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;

import org.junit.Before;
import org.junit.Test;

public class VirtualResourceGroupTest {

  private String surfConnextGroupName;
  private VirtualResourceGroup virtualResourceGroup;
  private Reservation reservation;
  private VirtualPort virtualPort;

  @Before
  public void setUp() {
    
    
    surfConnextGroupName="TestGroup";
    reservation = new ReservationFactory().create();
    virtualPort = new VirtualPortFactory().create();

    virtualResourceGroup = new VirtualResourceGroupFactory().create();
  }

  @Test
  public void test() {
    virtualResourceGroup = new VirtualResourceGroupFactory().setSurfConnextGroupName(surfConnextGroupName).addReservations(reservation).addVirtualPorts(virtualPort)
        .create();

    assertThat(virtualResourceGroup.getSurfConnextGroupName(), is(surfConnextGroupName));
    assertEquals(Arrays.asList(reservation), virtualResourceGroup.getReservations());
    assertEquals(Arrays.asList(virtualPort), virtualResourceGroup.getVirtualPorts());
  }

}
