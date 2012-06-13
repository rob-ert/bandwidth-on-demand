package nl.surfnet.bod;

import nl.surfnet.bod.support.TestExternalSupport;

import org.junit.Before;
import org.junit.Test;

public class MovePhysicalPortTestSelenium extends TestExternalSupport {

  private static final String INSTITUTE_SURF = "SURFnet Netwerk";
  private static final String INSTITUTE_UU = "Universiteit Utrecht";

  @Before
  public void setup() {
    getNocDriver().createNewPhysicalResourceGroup(INSTITUTE_SURF, ICT_MANAGERS_GROUP, "test@example.com");
    getWebDriver().clickLinkInLastEmail();
    getManagerDriver().switchToNoc();

    getNocDriver().createNewPhysicalResourceGroup(INSTITUTE_UU, ICT_MANAGERS_GROUP_2, "test@example.com");
    getWebDriver().clickLinkInLastEmail();
    getManagerDriver().switchToNoc();

    getNocDriver().linkPhysicalPort(NMS_PORT_ID_1, "First port", INSTITUTE_SURF);
    getNocDriver().linkPhysicalPort(NMS_PORT_ID_2, "Second port", INSTITUTE_UU);

    getNocDriver().switchToUser();
    getUserDriver().requestVirtualPort("selenium-users");
    getUserDriver().selectInstituteAndRequest(INSTITUTE_SURF, 1200, "port 1");
    getWebDriver().clickLinkInLastEmail();
    getManagerDriver().createVirtualPort("First port");

    getManagerDriver().switchToUser();
    getUserDriver().requestVirtualPort("selenium-users");
    getUserDriver().selectInstituteAndRequest(INSTITUTE_UU, 1200, "port 2");
    getWebDriver().clickLinkInLastEmail();
    getManagerDriver().createVirtualPort("Second port");

    getManagerDriver().switchToUser();
    getUserDriver().createNewReservation("First reservation");
    getUserDriver().createNewReservation("Second reservation");
  }

  @Test
  public void moveAPhysicalPort() {
    getUserDriver().switchToNoc();

    getNocDriver().movePhysicalPort("First port");

    getNocDriver().verifyMovePage(NMS_PORT_ID_1, INSTITUTE_SURF, 1, 2, 2);

    getNocDriver().movePhysicalPortChooseNewPort(NMS_PORT_ID_3);

    getNocDriver().verifyMoveResultPage(2);

    getNocDriver().verifyHasReservations(4);
  }

}
