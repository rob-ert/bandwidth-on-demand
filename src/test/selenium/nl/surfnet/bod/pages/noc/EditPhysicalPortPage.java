package nl.surfnet.bod.pages.noc;

import nl.surfnet.bod.pages.AbstractFormPage;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

public class EditPhysicalPortPage extends AbstractFormPage {

  @FindBy(id = "_nocLabel_id")
  private WebElement nocLabelInput;

  @FindBy(id = "_managerLabel_id")
  private WebElement managerLabelInput;

  @FindBy(id = "_c_PhysicalPort_physicalResourceGroup")
  private WebElement physicalResourceGroupSelect;

  public static EditPhysicalPortPage get(RemoteWebDriver driver) {
    EditPhysicalPortPage page = new EditPhysicalPortPage();
    PageFactory.initElements(driver, page);

    return page;
  }

  public void sendNocLabel(String nocLabel) {
    nocLabelInput.clear();
    nocLabelInput.sendKeys(nocLabel);
  }

  public void selectPhysicalResourceGroup(String physicalResourceGroup) {
    new Select(physicalResourceGroupSelect).selectByVisibleText(physicalResourceGroup);
  }

  public void sendManagerLabel(String managerLabel) {
    managerLabelInput.clear();
    managerLabelInput.sendKeys(managerLabel);
  }
}
