package nl.surfnet.bod.support;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class TextNotOnPageProbe implements Probe {
  private final WebDriver webDriver;
  private final String notExpectedText;
  private boolean satisfied;
  private final By locator;

  public TextNotOnPageProbe(WebDriver webDriver, By locator, String textNotPresent) {
    this.webDriver = webDriver;
    this.locator = locator;
    this.notExpectedText = textNotPresent;
  }

  @Override
  public void sample() {
    try {
      List<WebElement> elements = webDriver.findElements(locator);
      if (elements.isEmpty()) {
        this.satisfied = true;
        return;
      }

      for (WebElement element : elements) {
        boolean textFound = element.getText().contains(notExpectedText);
        if (!textFound) {
          this.satisfied = true;
          break;
        }
      }
    }
    catch (NoSuchElementException e) {
      this.satisfied = true;
    }
  }

  @Override
  public boolean isSatisfied() {
    return satisfied;
  }

  @Override
  public String message() {
    return String.format("Expected page to NOT contain '%s' in element but it does.", this.notExpectedText,
        this.locator.toString());
  }
}
