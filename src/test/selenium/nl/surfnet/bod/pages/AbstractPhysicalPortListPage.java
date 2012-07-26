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
package nl.surfnet.bod.pages;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

public class AbstractPhysicalPortListPage extends AbstractListPage {

  public AbstractPhysicalPortListPage(RemoteWebDriver driver) {
    super(driver);
  }

  public WebElement verifyPhysicalPortWasAllocated(String nmsPortId, String label) {

    return findRow(nmsPortId, label);
  }

  public void verifyPhysicalPortHasEnabledUnallocateIcon(String nmsPortId, String label) {
    WebElement row = verifyPhysicalPortWasAllocated(nmsPortId, label);

    try {
      row.findElement(By.cssSelector("span.disabled-icon"));
      assertThat("PhysicalPort should not contain disabled unallocate Icon", false);
    }
    catch (NoSuchElementException e) {
      // Expected
    }
  }

  public void verifyPhysicalPortHasDisabledUnallocateIcon(String nmsPortId, String label, String toolTipText) {

    WebElement row = verifyPhysicalPortWasAllocated(nmsPortId, label);

    WebElement unAllocateElement = row.findElement(By.cssSelector("span.disabled-icon"));
    String deleteTooltip = unAllocateElement.getAttribute("data-original-title");

    assertThat(deleteTooltip, containsString(toolTipText));
  }

  public void verifyPhysicalPortIsNotOnUnallocatedPage(String nmsPortId, String label) {
    try {
      verifyPhysicalPortWasAllocated(nmsPortId, label);
      assertThat("PhysicalPort should not be listed on unAllocated page", false);
    }
    catch (NoSuchElementException e) {
      // Expected
    }
  }

}
