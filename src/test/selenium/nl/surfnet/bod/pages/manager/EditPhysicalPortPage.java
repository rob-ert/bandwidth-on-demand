package nl.surfnet.bod.pages.manager;

import nl.surfnet.bod.pages.AbstractFormPage;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class EditPhysicalPortPage extends AbstractFormPage {

  @FindBy(id = "_managerLabel_id")
  private WebElement managerLabelInput;

  public static EditPhysicalPortPage get(RemoteWebDriver driver) {
    EditPhysicalPortPage page = new EditPhysicalPortPage();
    PageFactory.initElements(driver, page);

    return page;
  }

  public void sendMagerLabel(String managerLabel) {
    managerLabelInput.clear();
    managerLabelInput.sendKeys(managerLabel);
  }
}
