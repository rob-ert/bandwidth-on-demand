package nl.surfnet.bod.pages.virtualresourcegroup;

import nl.surfnet.bod.support.Probes;
import nl.surfnet.bod.web.VirtualResourceGroupController;
import nl.surfnet.bod.web.WebUtils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class NewVirtualResourceGroupPage {

  private static final String PAGE = VirtualResourceGroupController.PAGE_URL_PREFIX
      + VirtualResourceGroupController.PAGE_URL + WebUtils.CREATE;

  private final Probes probes;

  @FindBy(id = "_surfConnextGroupName_id")
  private WebElement surfConnextGroupNameInput;

  @FindBy(css = "input[type='submit']")
  private WebElement saveButton;

  @FindBy(id = "_surfConnextGroupName_error_id")
  private WebElement surfConnextGroupNameError;

  public NewVirtualResourceGroupPage(WebDriver driver) {
    this.probes = new Probes(driver);
  }

  public static NewVirtualResourceGroupPage get(RemoteWebDriver driver, String baseUrl) {
    driver.get(baseUrl + PAGE);
    NewVirtualResourceGroupPage page = new NewVirtualResourceGroupPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public static NewVirtualResourceGroupPage get(RemoteWebDriver driver) {
    NewVirtualResourceGroupPage page = new NewVirtualResourceGroupPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public void sendSurfConnextGroupName(String name) {
    surfConnextGroupNameInput.clear();
    surfConnextGroupNameInput.sendKeys(name);
  }

  public void save() {
    saveButton.click();
  }

  public boolean hasErrorSurfConnextGroupName() {
    return surfConnextGroupNameError.isDisplayed();
  }

}
