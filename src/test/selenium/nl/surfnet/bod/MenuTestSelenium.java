package nl.surfnet.bod;

import nl.surfnet.bod.support.TestExternalSupport;

import org.junit.Before;
import org.junit.Test;

public class MenuTestSelenium extends TestExternalSupport {

  @Before
  public void onSetup() {
    // Just what this test needs... :-)
    new ReservationTestSelenium().setup();
  }

  @Test
  public void shouldNavigateMenus() {
    getNocDriver().verifyMenu();
    getManagerDriver().verifyMenu();
    getUserDriver().verifyMenu();
  }

}
