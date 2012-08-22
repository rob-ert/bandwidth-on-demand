package nl.surfnet.bod.support;

import org.openqa.selenium.WebElement;

public class TextWithinWebElement implements Probe {

  private final WebElement element;
  private final String text;

  private boolean satisfied;

  public TextWithinWebElement(WebElement element, String text) {
     this.element = element;
     this.text = text;
  }

  @Override
  public void sample() {
    satisfied = element.getText().contains(text);
  }

  @Override
  public boolean isSatisfied() {
    return satisfied;
  }

  @Override
  public String message() {
    return String.format("Expected to find %s in %s, but could not", text, element.getText());
  }

}
