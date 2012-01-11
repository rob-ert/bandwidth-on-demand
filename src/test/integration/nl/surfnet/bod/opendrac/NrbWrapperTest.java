package nl.surfnet.bod.opendrac;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.security.ClientLoginType;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.nrb.NrbInterface;

/**
 * This test only works against a default OpenDRAC (with standard admin pwd)
 * with the simulator and the 6 simulated NE's
 * 
 * @author robert
 * 
 */
public class NrbWrapperTest {

  private final NrbWrapper nrbWrapper = new NrbWrapper();

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testGetNrbInterface() throws Exception {
    final NrbInterface nrbInterface = nrbWrapper.getNrbInterface();
    assertNotNull(nrbInterface);
  }

  @Test
  public void testGetAllNetworkElements() throws Exception {
    final NrbInterface nrbInterface = nrbWrapper.getNrbInterface();
    final LoginToken token = nrbInterface.login(ClientLoginType.INTERNAL_LOGIN, "admin", "myDrac".toCharArray(), null,
        null, null);
    final List<NetworkElementHolder> allNetworkElements = nrbInterface.getAllNetworkElements(token);
    assertNotNull(allNetworkElements);
    assertEquals(6, allNetworkElements.size());
  }

  @Test
  public void testGetAllFacilities() throws Exception {
    final NrbInterface nrbInterface = nrbWrapper.getNrbInterface();
    final LoginToken token = nrbInterface.login(ClientLoginType.INTERNAL_LOGIN, "admin", "myDrac".toCharArray(), null,
        null, null);
    final List<NetworkElementHolder> allNetworkElements = nrbInterface.getAllNetworkElements(token);
    final List<Facility> facilities = new ArrayList<Facility>();
    for (NetworkElementHolder holder : allNetworkElements) {
      facilities.addAll(nrbInterface.getFacilities(token, holder.getId()));
    }
    assertEquals(82, facilities.size());
  }

}
