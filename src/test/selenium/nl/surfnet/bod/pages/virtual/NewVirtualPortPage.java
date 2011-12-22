package nl.surfnet.bod.pages.virtual;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class NewVirtualPortPage {

  private static final String PAGE =  "/manager/virtualports/create";

  private final RemoteWebDriver driver;

  @FindBy(id = "_name_id")
  private WebElement nameInput;

  @FindBy(id = "_name_id")
  private WebElement virtualResourceGroupSelect;

  @FindBy(id = "_name_id")
  private WebElement physicalResourceGroupSelect;

  @FindBy(id = "_name_id")
  private WebElement physicalPortSelect;

  @FindBy(css = "input[type='submit']")
  private WebElement saveButton;

  public NewVirtualPortPage(RemoteWebDriver driver) {
    this.driver = driver;
  }

  public static NewVirtualPortPage get(RemoteWebDriver driver, String urlUnderTest) {
    driver.get(urlUnderTest + PAGE);
    return get(driver);
  }

  private static NewVirtualPortPage get(RemoteWebDriver driver) {
    NewVirtualPortPage page = new NewVirtualPortPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public void sendName(String name) {
    nameInput.clear();
    nameInput.sendKeys(name);
  }

  public void save() {
    saveButton.click();
  }

}
