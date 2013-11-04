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
package nl.surfnet.bod.pages.noc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import nl.surfnet.bod.pages.AbstractPhysicalPortListPage;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

public abstract class AbstractPhysicalPortListNocPage extends AbstractPhysicalPortListPage {

  public AbstractPhysicalPortListNocPage(RemoteWebDriver driver) {
    super(driver);
  }

  public void verifyPhysicalPortHasDisabledUnallocateIcon(String nmsPortId, String label, String toolTipText) {
    WebElement row = verifyPortWasAllocated(nmsPortId, label);

    WebElement unAllocateElement = row.findElement(By.cssSelector("span.disabled-icon"));
    String deleteTooltip = unAllocateElement.getAttribute("data-original-title");

    assertTrue(unAllocateElement.isDisplayed());
    assertThat(deleteTooltip, containsString(toolTipText));
  }

  public void verifyPhysicalPortHasEnabledUnallocateIcon(String nmsPortId, String label) {
    WebElement row = verifyPortWasAllocated(nmsPortId, label);

    try {
      boolean visible = row.findElement(By.cssSelector("span.disabled-icon")).isDisplayed();

      assertFalse("PhysicalPort row should not contain a visible disabled unallocate icon", visible);
    } catch (NoSuchElementException e) {
      // Expected
    }
  }

}
