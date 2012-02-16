package nl.surfnet.bod.pages.physical;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

public class EditPhysicalResoruceGroupPage extends NewPhysicalResourceGroupPage {

  private EditPhysicalResoruceGroupPage(WebDriver driver) {
    super(driver);
  }

  public static EditPhysicalResoruceGroupPage get(RemoteWebDriver driver) {
    EditPhysicalResoruceGroupPage page = new EditPhysicalResoruceGroupPage(driver);
    PageFactory.initElements(driver, page);
    return page;
  }

}
