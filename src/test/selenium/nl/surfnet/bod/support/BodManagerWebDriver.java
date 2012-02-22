package nl.surfnet.bod.support;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import nl.surfnet.bod.pages.manager.EditPhysicalPortPage;
import nl.surfnet.bod.pages.manager.ListPhysicalPortsPage;
import nl.surfnet.bod.pages.manager.NewVirtualPortPage;

import org.openqa.selenium.remote.RemoteWebDriver;

public class BodManagerWebDriver {

  private final RemoteWebDriver driver;

  public BodManagerWebDriver(RemoteWebDriver driver) {
    this.driver = driver;
  }

  public void verifyManagerLabelChanged(String networkElementPk, String managerLabel) {
    ListPhysicalPortsPage listPage = ListPhysicalPortsPage.get(driver);

    listPage.findRow(networkElementPk, managerLabel);
  }

  public void createNewVirtualPortForPhysicalPort(String networkElementPk) {
    ListPhysicalPortsPage listPage = ListPhysicalPortsPage.get(driver);

    listPage.newVirtualPort(networkElementPk);
  }

  public void verifyPhysicalPortSelected(String managerLabel) {
    NewVirtualPortPage page = NewVirtualPortPage.get(driver);

    String portName = page.getSelectedPhysicalPort();

    assertThat(portName, is(managerLabel));
  }

  public void changeManagerLabelOfPhyiscalPort(String networkElementPk, String managerLabel) {
    ListPhysicalPortsPage page = ListPhysicalPortsPage.get(driver, BodWebDriver.URL_UNDER_TEST);

    EditPhysicalPortPage editPage = page.edit(networkElementPk);

    editPage.sendMagerLabel(managerLabel);
    editPage.save();
  }
}
