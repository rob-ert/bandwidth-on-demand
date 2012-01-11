package nl.surfnet.bod.opendrac;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.nortel.appcore.app.drac.security.LoginToken;

/**
 * This test only works against a default OpenDRAC (with standard admin pwd)
 * with the simulator and the 6 simulated NE's
 * 
 * @author robert
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/spring/bod-opendrac-test.xml")
public class NrbServiceTest {

  @Autowired
  @Qualifier("nrbService")
  private NrbService nrbService;

  private LoginToken loginToken;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    new AnnotationConfigApplicationContext().scan("nl.surfnet.bod.opendrac");
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    loginToken = nrbService.getLoginToken("admin", "292c2cdcb5f669a8");
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testGetAllNetworkElements() throws Exception {
    assertEquals(6, nrbService.getAllNetworkElements(loginToken).size());
  }

  @Test
  public void testGetAllFacilities() throws Exception {
    assertEquals(82, nrbService.getAllFacilities(loginToken).size());
  }

}
