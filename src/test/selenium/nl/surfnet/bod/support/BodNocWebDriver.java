package nl.surfnet.bod.support;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import nl.surfnet.bod.pages.noc.*;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.remote.RemoteWebDriver;

public class BodNocWebDriver {

  private final RemoteWebDriver driver;

  public BodNocWebDriver(RemoteWebDriver driver) {
    this.driver = driver;
  }

  /* **************************************** */
  /*        Physical Resource Group           */
  /* **************************************** */

  public void createNewPhysicalResourceGroup(String institute, String adminGroup, String email) throws Exception {
    NewPhysicalResourceGroupPage page = NewPhysicalResourceGroupPage.get(driver, BodWebDriver.URL_UNDER_TEST);
    page.sendInstitute(institute);
    page.sendAdminGroup(adminGroup);
    page.sendEmail(email);

    page.save();
  }
  public void deletePhysicalGroup(String institute) {
    ListPhysicalResourceGroupPage page = ListPhysicalResourceGroupPage.get(driver, BodWebDriver.URL_UNDER_TEST);

    page.delete(institute);
  }

  public void editPhysicalResoruceGroup(String institute, String finalEmail) {
    ListPhysicalResourceGroupPage page = ListPhysicalResourceGroupPage.get(driver);

    EditPhysicalResourceGroupPage editPage = page.edit(institute);
    editPage.sendEmail(finalEmail);
    editPage.save();
  }

  public void verifyGroupWasCreated(String institute, String adminGroup, String email) {
    verifyGroupExists(institute, adminGroup, email, "FALSE");
  }

  public void verifyGroupExists(String institute, String adminGroup, String email, String status) {
    ListPhysicalResourceGroupPage page = ListPhysicalResourceGroupPage.get(driver, BodWebDriver.URL_UNDER_TEST);

    page.findRow(institute, adminGroup, email, status);
  }

  public void verifyGroupWasDeleted(String institute, String adminGroup, String email) {
    ListPhysicalResourceGroupPage page = ListPhysicalResourceGroupPage.get(driver);

    try {
      page.findRow(institute, adminGroup, email);
      fail("The physical resource group was not deleted");
    }
    catch (NoSuchElementException e) {
      // expected
    }
  }

  public void verifyPhysicalResourceGroupIsActive(String institute, String adminGroup, String email) {
    verifyGroupExists(institute, adminGroup, email, "TRUE");
  }

  /* ******************************************** */
  /*                Physical ports                */
  /* ******************************************** */

  public void linkPhysicalPort(String networkElementPk, String nocLabel) {
    ListUnallocatedPortsPage listPage = ListUnallocatedPortsPage.get(driver, BodWebDriver.URL_UNDER_TEST);

    EditPhysicalPortPage editPage = listPage.edit(networkElementPk);
    editPage.sendNocLabel(nocLabel);
    editPage.save();
  }

  public void verifyPhysicalPortWasAllocated(String networkElementPk, String nocLabel) {
    ListAllocatedPortsPage page = ListAllocatedPortsPage.get(driver, BodWebDriver.URL_UNDER_TEST);

    page.findRow(networkElementPk, nocLabel);
  }

  public void unlinkPhysicalPort(String networkElementPk) {
    ListAllocatedPortsPage page = ListAllocatedPortsPage.get(driver, BodWebDriver.URL_UNDER_TEST);

    page.unlinkPhysicalPort(networkElementPk);
  }

  public void verifyPhysicalPortWasUnlinked(String networkElementPk) {
    ListAllocatedPortsPage page = ListAllocatedPortsPage.get(driver);

    assertTrue(page.containsAnyItems());

    try {
      page.findRow(networkElementPk);
      fail("The physical port was not unlinked");
    }
    catch (NoSuchElementException e) {
      // expected
    }
  }
}
