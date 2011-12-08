package nl.surfnet.bod.domain;

import static org.junit.Assert.assertEquals;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;

import org.junit.Before;
import org.junit.Test;

public class VirtualPortTest {

  private static final String PHYSICAL_PORT_NAME = "portName";
  private PhysicalPort physicalPort;
  private VirtualResourceGroup virtualResourceGroup;

  @Before
  public void setUp() {
    physicalPort = new PhysicalPortFactory().setName(PHYSICAL_PORT_NAME).create();
    virtualResourceGroup = new VirtualResourceGroupFactory().create();
  }

  @Test
  public void testSetters() {
    VirtualPort vPort = new VirtualPortFactory().setName("vPortName").setPhysicalPort(physicalPort).setVirtualResourceGroup(virtualResourceGroup). create();

    assertEquals(vPort.getPhysicalPort(), physicalPort);
    assertEquals(vPort.getVirtualResourceGroup(), virtualResourceGroup);
  }
}
