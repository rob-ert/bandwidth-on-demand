package nl.surfnet.bod.search;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import nl.surfnet.bod.domain.PhysicalPort;

public class PhysicalPortIndexAndSearchTest extends AbstractIndexAndSearch<PhysicalPort> {

  public PhysicalPortIndexAndSearchTest() {
    super(PhysicalPort.class);
  }

  @Before
  public void setUp() {
    initEntityManager();
  }

  @After
  public void tearDown() {
    closeEntityManager();
  }

  @Test
  public void testIndexAndSearch() throws Exception {

    List<PhysicalPort> physicalPorts = getSearchQuery("ut");
    // nothing indexed so nothing should be found
    assertThat(physicalPorts.size(), is(0));

    index();

    physicalPorts = getSearchQuery("gamma");
    // (N.A.)
    assertThat(physicalPorts.size(), is(0));

    physicalPorts = getSearchQuery("ut");
    // (UT One, UT Two)
    assertThat(physicalPorts.size(), is(2));

    physicalPorts = getSearchQuery("Ut");
    // (UT One, UT Two)
    assertThat(physicalPorts.size(), is(2));

    physicalPorts = getSearchQuery("Mock");
    // (All available (4) PP's)
    assertThat(physicalPorts.size(), is(4));

    physicalPorts = getSearchQuery("ETH-1-13-4");
    // (Noc label 4)
    assertThat(physicalPorts.size(), is(1));
    assertThat(physicalPorts.get(0).getNocLabel(), equalTo("Noc 4 label"));

    physicalPorts = getSearchQuery("OME");
    // (Mock_Ut002A_OME01_ETH-1-2-4, Mock_Ut001A_OME01_ETH-1-2-1)
    assertThat(physicalPorts.size(), is(2));
    assertThat(physicalPorts.get(0).getNocLabel(), equalTo("Mock_Ut002A_OME01_ETH-1-2-1"));
    assertThat(physicalPorts.get(1).getNocLabel(), equalTo("Mock_Ut001A_OME01_ETH-1-2-2"));

    physicalPorts = getSearchQuery("ETH-1-");
    // (All available (4) PP's)
    assertThat(physicalPorts.size(), is(4));
    assertThat(physicalPorts.get(0).getNocLabel(), equalTo("Mock_Ut002A_OME01_ETH-1-2-1"));
    assertThat(physicalPorts.get(1).getNocLabel(), equalTo("Mock_Ut001A_OME01_ETH-1-2-2"));
    assertThat(physicalPorts.get(2).getNocLabel(), equalTo("Noc 3 label"));
    assertThat(physicalPorts.get(3).getNocLabel(), equalTo("Noc 4 label"));

    physicalPorts = getSearchQuery("1");
    // (All available (4) PP's)
    assertThat(physicalPorts.size(), is(4));
    assertThat(physicalPorts.get(0).getNocLabel(), equalTo("Mock_Ut002A_OME01_ETH-1-2-1"));
    assertThat(physicalPorts.get(1).getNocLabel(), equalTo("Mock_Ut001A_OME01_ETH-1-2-2"));
    assertThat(physicalPorts.get(2).getNocLabel(), equalTo("Noc 3 label"));
    assertThat(physicalPorts.get(3).getNocLabel(), equalTo("Noc 4 label"));

    physicalPorts = getSearchQuery("1de");
    // Mock_port 1de verdieping toren1a
    assertThat(physicalPorts.size(), is(1));
    assertThat(physicalPorts.get(0).getBodPortId(), equalTo("Mock_port 1de verdieping toren1a"));

    physicalPorts = getSearchQuery("2de");
    // Mock_port 2de verdieping toren1b
    assertThat(physicalPorts.size(), is(1));
    assertThat(physicalPorts.get(0).getBodPortId(), equalTo("Mock_port 2de verdieping toren1b"));

    // physicalPorts = getSearchQuery("Noc 3");
    // assertThat(physicalPorts.size(), is(1));
    // assertThat(physicalPorts.get(0).getNocLabel(), equalTo("Noc 3 label"));

  }

}