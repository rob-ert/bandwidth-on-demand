package nl.surfnet.bod.pages.user;

import nl.surfnet.bod.pages.AbstractFormPage;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class EditVirtualPortPage extends AbstractFormPage {

  @FindBy(id = "_userLabel_id")
  private WebElement userLabelInput;

  public static EditVirtualPortPage get(RemoteWebDriver driver) {
    EditVirtualPortPage page = new EditVirtualPortPage();
    PageFactory.initElements(driver, page);

    return page;
  }

  public void sendUserLabel(String newLabel) {
    userLabelInput.clear();
    userLabelInput.sendKeys(newLabel);
  }
}
