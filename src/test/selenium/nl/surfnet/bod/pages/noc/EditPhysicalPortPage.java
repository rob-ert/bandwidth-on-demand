package nl.surfnet.bod.pages.noc;

import nl.surfnet.bod.pages.AbstractFormPage;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class EditPhysicalPortPage extends AbstractFormPage {

  @FindBy(id = "_nocLabel_id")
  private WebElement nocLabelInput;

  public static EditPhysicalPortPage get(RemoteWebDriver driver) {
    EditPhysicalPortPage page = new EditPhysicalPortPage();
    PageFactory.initElements(driver, page);

    return page;
  }

  public void sendNocLabel(String nocLabel) {
    nocLabelInput.clear();
    nocLabelInput.sendKeys(nocLabel);
  }
}
