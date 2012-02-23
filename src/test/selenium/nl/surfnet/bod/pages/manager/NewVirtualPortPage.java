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

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

public class NewVirtualPortPage extends AbstractFormPage {

  private static final String PAGE =  "/manager/virtualports/create";

  @FindBy(id = "_managerLabel_id")
  private WebElement nameInput;

  @FindBy(id = "_maxBandwidth_id")
  private WebElement maxBandwidthInput;

  @FindBy(id = "_vlanId_id")
  private WebElement vlandIdInput;

  @FindBy(id = "_physicalResourceGroup")
  private WebElement physicalResourceGroupSelect;

  @FindBy(id = "_physicalPort")
  private WebElement physicalPortSelect;

  public static NewVirtualPortPage get(RemoteWebDriver driver, String urlUnderTest) {
    driver.get(urlUnderTest + PAGE);
    return get(driver);
  }

  public static NewVirtualPortPage get(RemoteWebDriver driver) {
    NewVirtualPortPage page = new NewVirtualPortPage();
    PageFactory.initElements(driver, page);

    return page;
  }

  public void sendName(String name) {
    nameInput.clear();
    nameInput.sendKeys(name);
  }

  public void sendMaxBandwidth(String maxBandwidth) {
    maxBandwidthInput.clear();
    maxBandwidthInput.sendKeys(maxBandwidth);
  }

  public void sendVlanId(String vlandId) {
    vlandIdInput.clear();
    vlandIdInput.sendKeys(vlandId);
  }

  public String getSelectedPhysicalResourceGroup() {
    return new Select(physicalResourceGroupSelect).getFirstSelectedOption().getText();
  }

  public String getSelectedPhysicalPort() {
    return new Select(physicalPortSelect).getFirstSelectedOption().getText();
  }

}
