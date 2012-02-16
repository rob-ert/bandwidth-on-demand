package nl.surfnet.bod.pages.manager;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class EditPhysicalResourceGroupPage {

  private final RemoteWebDriver driver;

  @FindBy(id = "_manageremail_id")
  private WebElement emailInput;

  private EditPhysicalResourceGroupPage(RemoteWebDriver driver) {
    this.driver = driver;
  }

  public static EditPhysicalResourceGroupPage get(RemoteWebDriver driver) {
    EditPhysicalResourceGroupPage page = new EditPhysicalResourceGroupPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public String getEmailValue() {
    return emailInput.getText();
  }
}
