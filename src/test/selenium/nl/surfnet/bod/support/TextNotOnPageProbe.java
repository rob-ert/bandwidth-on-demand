/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
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
