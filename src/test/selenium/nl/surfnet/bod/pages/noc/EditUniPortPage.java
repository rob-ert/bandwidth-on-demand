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

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

import nl.surfnet.bod.pages.AbstractFormPage;

public class EditUniPortPage extends AbstractFormPage {

  @FindBy(id = "_nocLabel_id")
  private WebElement nocLabelInput;

  @FindBy(id = "_managerLabel_id")
  private WebElement managerLabelInput;

  @FindBy(id = "_c_PhysicalPort_physicalResourceGroup")
  private WebElement physicalResourceGroupSelect;

  public EditUniPortPage(RemoteWebDriver driver) {
    super(driver);
  }

  public static EditUniPortPage get(RemoteWebDriver driver) {
    EditUniPortPage page = new EditUniPortPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public void sendNocLabel(String nocLabel) {
    clearAndSend(nocLabelInput, nocLabel);
  }

  public void selectPhysicalResourceGroup(String physicalResourceGroup) {
    new Select(physicalResourceGroupSelect).selectByVisibleText(physicalResourceGroup);
  }

  public void sendManagerLabel(String managerLabel) {
    clearAndSend(managerLabelInput, managerLabel);
  }

  public String getManagerLabel() {
    return managerLabelInput.getAttribute("value");
  }
}