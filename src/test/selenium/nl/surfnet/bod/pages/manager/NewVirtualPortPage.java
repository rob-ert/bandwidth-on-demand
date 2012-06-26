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
package nl.surfnet.bod.pages.manager;

import nl.surfnet.bod.pages.AbstractFormPage;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.UnexpectedTagNameException;

import com.google.common.base.Strings;

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
