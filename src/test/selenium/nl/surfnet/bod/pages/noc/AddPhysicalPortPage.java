package nl.surfnet.bod.pages.noc;

import nl.surfnet.bod.pages.AbstractFormPage;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

public class AddPhysicalPortPage extends AbstractFormPage {

  @FindBy(id = "_nocLabel_id")
  private WebElement nocLabelInput;

  @FindBy(name = "networkElementPk")
  private WebElement portSelect;

  public AddPhysicalPortPage(RemoteWebDriver driver) {
    super(driver);
  }

  public static AddPhysicalPortPage get(RemoteWebDriver driver) {
    AddPhysicalPortPage page = new AddPhysicalPortPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public void selectPort(String port) {
    new Select(portSelect).selectByVisibleText(port);
  }

  public void sendNocLabel(String nocLabel) {
    nocLabelInput.clear();
    nocLabelInput.sendKeys(nocLabel);
  }
}
