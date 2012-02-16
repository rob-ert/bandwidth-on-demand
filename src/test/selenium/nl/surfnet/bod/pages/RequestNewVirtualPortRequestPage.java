package nl.surfnet.bod.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class RequestNewVirtualPortRequestPage {

  private final RemoteWebDriver driver;

  @FindBy(id = "message")
  private WebElement messageTextArea;

  @FindBy(css = "input[type=submit]")
  private WebElement sentRequestButton;

  private RequestNewVirtualPortRequestPage(RemoteWebDriver driver) {
    this.driver = driver;
  }

  public static RequestNewVirtualPortRequestPage get(RemoteWebDriver driver) {
    RequestNewVirtualPortRequestPage page = new RequestNewVirtualPortRequestPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public void sendMessage(String message) {
    messageTextArea.clear();
    messageTextArea.sendKeys(message);
  }
  public void sentRequest() {
    sentRequestButton.click();
  }
}
