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
package nl.surfnet.bod.pages.manager;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.UnexpectedTagNameException;

import com.google.common.base.Strings;

import nl.surfnet.bod.pages.AbstractFormPage;

public class NewVirtualPortPage extends AbstractFormPage {

  @FindBy(id = "_managerLabel_id")
  private WebElement nameInput;

  @FindBy(id = "_maxBandwidth_id")
  private WebElement maxBandwidthInput;

  @FindBy(id = "_vlanId_id")
  private WebElement vlandIdInput;

  @FindBy(id = "_userLabel_id")
  private WebElement userLabelInput;

  @FindBy(id = "_physicalResourceGroup")
  private WebElement physicalResourceGroupSelect;

  @FindBy(id = "_physicalPort")
  private WebElement physicalPortSelect;

  @FindBy(id = "_virtualresourcegroup")
  private WebElement virtualResourceGroupSelect;

  @FindBy(id = "_accept")
  private WebElement acceptRadio;

  @FindBy(id = "_decline")
  private WebElement declineRadio;

  @FindBy(id = "_declineMessage")
  private WebElement declineMessageTextArea;

  public NewVirtualPortPage(RemoteWebDriver driver) {
    super(driver);
  }

  public static NewVirtualPortPage get(RemoteWebDriver driver) {
    NewVirtualPortPage page = new NewVirtualPortPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public void sendName(String name) {
    nameInput.clear();
    nameInput.sendKeys(name);
  }

  public void sendMaxBandwidth(int maxBandwidth) {
    maxBandwidthInput.clear();
    maxBandwidthInput.sendKeys("" + maxBandwidth);
  }

  public void sendVlanId(String vlandId) {
    vlandIdInput.clear();
    vlandIdInput.sendKeys(vlandId);
  }

  public void sendUserLabel(String userLabel) {
    userLabelInput.clear();
    userLabelInput.sendKeys(userLabel);
  }

  public String getSelectedPhysicalResourceGroup() {
    try {
      return new Select(physicalResourceGroupSelect).getFirstSelectedOption().getText();
    }
    catch (UnexpectedTagNameException e) {
      WebElement element = physicalResourceGroupSelect.findElement(By.xpath(".."));
      return element.getText();
    }
  }

  public String getSelectedPhysicalPort() {
    return new Select(physicalPortSelect).getFirstSelectedOption().getText();
  }

  public void selectVirtualResourceGroup(String virtualResourceGroup) {
    if (Strings.isNullOrEmpty(virtualResourceGroup)) {
      return;
    }
    new Select(virtualResourceGroupSelect).selectByVisibleText(virtualResourceGroup);
  }

  public void selectPhysicalResourceGroup(String physicalResourceGroup) {
    if (Strings.isNullOrEmpty(physicalResourceGroup)) {
      return;
    }
    new Select(physicalResourceGroupSelect).selectByVisibleText(physicalResourceGroup);
  }

  public void selectPhysicalPort(String physicalPort) {
    if (Strings.isNullOrEmpty(physicalPort)) {
      return;
    }
    new Select(physicalPortSelect).selectByVisibleText(physicalPort);
  }

  public Integer getBandwidth() {
    String maxBandwidth = maxBandwidthInput.getAttribute("value");
    return Strings.emptyToNull(maxBandwidth) == null ? 0 : Integer.valueOf(maxBandwidth);
  }

  public String getUserLabel() {
    return userLabelInput.getAttribute("value");
  }

  public void accept() {
    acceptRadio.click();
  }

  public void decline() {
    declineRadio.click();
  }

  public void sendDeclineMessage(String message) {
    declineMessageTextArea.clear();
    declineMessageTextArea.sendKeys(message);
  }

}
