package nl.surfnet.bod.pages.virtual;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ListVirtualPortPage {

  private final RemoteWebDriver driver;

  @FindBy(css = "table.zebra-striped tbody")
  private WebElement table;

  public ListVirtualPortPage(RemoteWebDriver driver) {
    this.driver = driver;
  }

  public static ListVirtualPortPage get(RemoteWebDriver driver) {
    ListVirtualPortPage page = new ListVirtualPortPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public String getTable() {
    return table.getText();
  }
}
