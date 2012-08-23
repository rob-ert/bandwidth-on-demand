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
