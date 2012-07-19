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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class TextOnPageProbe implements Probe {
  private final WebDriver webDriver;
  private final String expectedText;
  private final By locator;

  private boolean satisfied;
  private List<WebElement> elements;

  public TextOnPageProbe(WebDriver webDriver, By locator, String textPresent) {
    this.webDriver = webDriver;
    this.locator = locator;
    this.expectedText = textPresent;
  }

  @Override
  public void sample() {
    try {
      elements = webDriver.findElements(locator);
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
    String foundText = Joiner.on(" || ").join(
      Lists.transform(elements, new Function<WebElement, String>() {
        @Override
        public String apply(WebElement input) {
          return input.getText();
        }
      })
    );

    return String.format(
        "Expected element with locator '%s' to contain '%s' but it contains '%s'.",
        this.locator.toString(),
        this.expectedText,
        foundText);
  }
}
