package nl.surfnet.bod.pages.manager;

import nl.surfnet.bod.pages.AbstractListPage;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

public class ListPhysicalPortsPage extends AbstractListPage {

  private static final String PAGE =  "/manager/physicalports";

  public ListPhysicalPortsPage(RemoteWebDriver driver) {
    super(driver);
  }

  public static ListPhysicalPortsPage get(RemoteWebDriver driver) {
    ListPhysicalPortsPage page = new ListPhysicalPortsPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public static ListPhysicalPortsPage get(RemoteWebDriver driver, String baseUrl) {
    driver.get(baseUrl + PAGE);
    return get(driver);
  }

  public EditPhysicalPortPage edit(String networkElementPk) {
    editRow(networkElementPk);

    return EditPhysicalPortPage.get(driver);
  }

  public void newVirtualPort(String networkElementPk) {
    findRow(networkElementPk).findElement(By.cssSelector("a img[alt~=Create]")).click();
  }

}
