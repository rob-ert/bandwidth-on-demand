/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
