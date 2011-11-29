package nl.surfnet.bod.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ListPhysicalResourceGroupPage {

  private static final String PAGE = "noc/physicalresourcegroups";
  private final RemoteWebDriver driver;

  @FindBy(css = "table.zebra-striped tbody")
  private WebElement table;

  public ListPhysicalResourceGroupPage(RemoteWebDriver driver) {
    this.driver = driver;
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

    throw new AssertionError(String.format("Physical resource group with name '%s' not found", name));
  }
}
