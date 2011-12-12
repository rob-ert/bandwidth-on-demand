package nl.surfnet.bod.pages.virtualresourcegroup;

import java.util.List;

import nl.surfnet.bod.web.VirtualResourceGroupController;
import nl.surfnet.bod.web.WebUtils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ListVirtualResourceGroupPage {

  private static final String PAGE =  VirtualResourceGroupController.PAGE_URL_PREFIX  + VirtualResourceGroupController.PAGE_URL;
  private final RemoteWebDriver driver;

  @FindBy(css = "table.zebra-striped tbody")
  private WebElement table;

  public ListVirtualResourceGroupPage(RemoteWebDriver driver) {
    this.driver = driver;
  }

  public static ListVirtualResourceGroupPage get(RemoteWebDriver driver, String baseUrl) {
    driver.get(baseUrl + PAGE);
    return get(driver);
  }

  public static ListVirtualResourceGroupPage get(RemoteWebDriver driver) {
    ListVirtualResourceGroupPage page = new ListVirtualResourceGroupPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public String getTable() {
    return table.getText();
  }

  public void deleteByName(String name) {
    List<WebElement> rows = table.findElements(By.tagName("tr"));

    for (WebElement row : rows) {
      if (row.getText().contains(name)) {
        WebElement deleteButton = row.findElement(By.cssSelector("input[type=image]"));
        deleteButton.click();
        driver.switchTo().alert().accept();
        return;
      }
    }

    throw new AssertionError(String.format("Virtual resource group with name '%s' not found", name));
  }
}
