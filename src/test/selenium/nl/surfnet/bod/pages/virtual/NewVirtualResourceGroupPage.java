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
package nl.surfnet.bod.pages.virtual;

import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.manager.VirtualResourceGroupController;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class NewVirtualResourceGroupPage {

  private static final String PAGE = "/manager/"
      + VirtualResourceGroupController.PAGE_URL + WebUtils.CREATE;

  @FindBy(id = "_surfConnextGroupName_id")
  private WebElement surfConnextGroupNameInput;

  @FindBy(id = "_name_id")
  private WebElement nameInput;

  @FindBy(css = "input[type='submit']")
  private WebElement saveButton;

  @FindBy(id = "_name_error_id")
  private WebElement nameError;

  public NewVirtualResourceGroupPage(WebDriver driver) {
  }

  public static NewVirtualResourceGroupPage get(RemoteWebDriver driver, String baseUrl) {
    driver.get(baseUrl + PAGE);
    NewVirtualResourceGroupPage page = new NewVirtualResourceGroupPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public static NewVirtualResourceGroupPage get(RemoteWebDriver driver) {
    NewVirtualResourceGroupPage page = new NewVirtualResourceGroupPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public void sendSurfConnextGroupName(String name) {
    surfConnextGroupNameInput.clear();
    surfConnextGroupNameInput.sendKeys(name);
  }

  public void sendName(String name) {
    nameInput.clear();
    nameInput.sendKeys(name);
  }

  public void save() {
    saveButton.click();
  }

  public boolean hasNameValidationError() {
    return nameError.isDisplayed();
  }

}
