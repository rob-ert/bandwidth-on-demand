/**
 * Copyright (c) 2012, 2013 SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.support;

import java.util.regex.Pattern;

import org.openqa.selenium.WebElement;

public class TextWithinWebElement implements Probe {

  private final WebElement element;
  private final Pattern pattern;

  private boolean satisfied;
  private String regex;
  private String lastTextSample;

  public static TextWithinWebElement forText(WebElement element, String text) {
    return new TextWithinWebElement(element, Pattern.quote(text));
  }

  public static TextWithinWebElement forRegex(WebElement element, String regex) {
    return new TextWithinWebElement(element, regex);
  }

  private TextWithinWebElement(WebElement element, String regex) {
    this.element = element;
    this.regex = regex;
    this.pattern = Pattern.compile(regex);
  }

  @Override
  public void sample() {
    lastTextSample = element.getText();
    satisfied = pattern.matcher(lastTextSample).find();
  }

  @Override
  public boolean isSatisfied() {
    return satisfied;
  }

  @Override
  public String message() {
    return String.format("Expected to find regex '%s' in '%s', but could not", regex, lastTextSample);
  }

}
