/**
 * Copyright (c) 2012, SURFnet BV
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

import nl.surfnet.bod.pages.AbstractPage;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

public final class MovePhysicalPortPage extends AbstractPage {

  @FindBy(id = "_c_new_physicalport")
  private WebElement newPhysicalPort;

  @FindBy(css = "input[type=submit]")
  private WebElement moveButton;

  @FindBy(id = "nms_port_id")
  private WebElement nmsPortIdDiv;

  @FindBy(id = "institute_id")
  private WebElement instituteDiv;

  @FindBy(id = "nrvp_id")
  private WebElement nrVirutalPortsDiv;

  @FindBy(id = "nrar_id")
  private WebElement nrActiveReservationsDiv;

  @FindBy(id = "nrr_id")
  private WebElement nrReservationsDiv;

  private MovePhysicalPortPage(RemoteWebDriver driver) {
    super(driver);
  }

  public static MovePhysicalPortPage get(RemoteWebDriver driver) {
    MovePhysicalPortPage page = new MovePhysicalPortPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public void selectNewPhysicalPort(String elementId) {
    new Select(newPhysicalPort).selectByValue(elementId);
  }

  public void movePort() {
    moveButton.click();
  }

  public String getNmsPortId() {
    return getOutputText(nmsPortIdDiv);
  }

  public String getInstituteName() {
    return getOutputText(instituteDiv);
  }

  public Integer getNumberOfVirtualPorts() {
    return Integer.valueOf(getOutputText(nrVirutalPortsDiv));
  }

  public Integer getNumberOfReservations() {
    return Integer.valueOf(getOutputText(nrReservationsDiv));
  }

  public Integer getNumberOfActiveReservations() {
    return Integer.valueOf(getOutputText(nrActiveReservationsDiv));
  }

  private String getOutputText(WebElement element) {
    return element.findElement(By.tagName("output")).getText();
  }
}
