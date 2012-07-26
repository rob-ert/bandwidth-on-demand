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
package nl.surfnet.bod.pages.noc;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

import nl.surfnet.bod.pages.AbstractFormPage;

public class EditPhysicalPortPage extends AbstractFormPage {

  @FindBy(id = "_nocLabel_id")
  private WebElement nocLabelInput;

  @FindBy(id = "_managerLabel_id")
  private WebElement managerLabelInput;

  @FindBy(id = "_c_PhysicalPort_physicalResourceGroup")
  private WebElement physicalResourceGroupSelect;

  public EditPhysicalPortPage(RemoteWebDriver driver) {
    super(driver);
  }

  public static EditPhysicalPortPage get(RemoteWebDriver driver) {
    EditPhysicalPortPage page = new EditPhysicalPortPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public void sendNocLabel(String nocLabel) {
    nocLabelInput.clear();
    nocLabelInput.sendKeys(nocLabel);
  }

  public void selectPhysicalResourceGroup(String physicalResourceGroup) {
    new Select(physicalResourceGroupSelect).selectByVisibleText(physicalResourceGroup);
  }

  public void sendManagerLabel(String managerLabel) {
    managerLabelInput.clear();
    managerLabelInput.sendKeys(managerLabel);
  }

  public String getManagerLabel() {
    return managerLabelInput.getAttribute("value");
  }
}
