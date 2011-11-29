package nl.surfnet.bod.support;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class TextOnPageProbe implements Probe {
  private final WebDriver webDriver;
  private final String expectedText;
  private boolean satisfied;
  private final By locator;

  public TextOnPageProbe(WebDriver webDriver, By locator, String textPresent) {
    this.webDriver = webDriver;
    this.locator = locator;
    this.expectedText = textPresent;
  }

  @Override
  public void sample() {
    try {
      List<WebElement> elements = webDriver.findElements(locator);
      for (WebElement element : elements) {
        boolean textFound = element.getText().contains(expectedText);
        if (textFound) {
          this.satisfied = true;
          break;
        }
      }
    }
    catch (NoSuchElementException e) {
      this.satisfied = false;
    }
  }

  @Override
  public boolean isSatisfied() {
    return satisfied;
  }

  @Override
  public String message() {
    return String.format("Expected page to contain '%s' in element '%s' but does not.", this.expectedText,
        this.locator.toString());
  }
}
