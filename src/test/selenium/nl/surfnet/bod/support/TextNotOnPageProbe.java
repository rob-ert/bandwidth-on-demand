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
