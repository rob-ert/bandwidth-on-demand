package nl.surfnet.bod.pages.manager;

import nl.surfnet.bod.pages.AbstractListPage;
import nl.surfnet.bod.support.Probes;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

public class ListPhysicalResourceGroupPage extends AbstractListPage {

  private static final String PAGE = "manager/physicalresourcegroups";
  private final Probes probes;

  public ListPhysicalResourceGroupPage(RemoteWebDriver driver) {
    super(driver);

    probes = new Probes(driver);
  }

  public static ListPhysicalResourceGroupPage get(RemoteWebDriver driver, String urlUnderTest) {
    driver.get(urlUnderTest + PAGE);
    return get(driver);
  }

  public static ListPhysicalResourceGroupPage get(RemoteWebDriver driver) {
    ListPhysicalResourceGroupPage page = new ListPhysicalResourceGroupPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public void showDetailViewForRowAndVerify(String name, String networkElementPk) {
    WebElement row = findRow(name);

    WebElement icon = row.findElement(By.cssSelector("a[class~=icon-arrow-down]"));
    icon.click();

    probes.assertTextPresent(By.className("detailview"), networkElementPk);
  }

}
