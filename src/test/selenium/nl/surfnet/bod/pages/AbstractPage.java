package nl.surfnet.bod.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class AbstractPage {

  @FindBy(id = "messages")
  private WebElement messagesDiv;

  public List<String> getInfoMessages() {
    List<WebElement> messageDivs = messagesDiv.findElements(By.className("alert-message"));
    return Lists.transform(messageDivs, new Function<WebElement, String>() {
      @Override
      public String apply(WebElement input) {
        return input.findElement(By.tagName("p")).getText();
      }
    });
  }
}
