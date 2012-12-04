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
package nl.surfnet.bod.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

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
