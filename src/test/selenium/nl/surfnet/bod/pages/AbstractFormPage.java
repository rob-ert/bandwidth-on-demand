package nl.surfnet.bod.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class AbstractFormPage extends AbstractPage {

  @FindBy(css = "input[type=submit]")
  private WebElement saveButton;

  public void save() {
    saveButton.click();
  }
}
