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

import java.util.List;

import nl.surfnet.bod.pages.AbstractFormPage;
import nl.surfnet.bod.support.Probes;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class NewPhysicalResourceGroupPage extends AbstractFormPage {

  private static final String PAGE = "noc/physicalresourcegroups/create";

  private final Probes probes;

  @FindBy(css = "input[name='instituteId_search']")
  private WebElement instituteInput;

  @FindBy(css = "input[name='adminGroup']")
  private WebElement adminGroupInput;

  @FindBy(css = "input[name='managerEmail']")
  private WebElement managerEmailInput;

  public NewPhysicalResourceGroupPage(RemoteWebDriver driver) {
    super(driver);
    this.probes = new Probes(driver);
  }

  public static NewPhysicalResourceGroupPage get(RemoteWebDriver driver, String host) {
    driver.get(host + PAGE);
    NewPhysicalResourceGroupPage page = new NewPhysicalResourceGroupPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public void sendInstitute(String institute) {
    instituteInput.clear();
    instituteInput.sendKeys(institute);

    probes.assertTextPresent(By.className("as-results"), institute);

    List<WebElement> results = getDriver().findElement(By.className("as-list")).findElements(By.className("as-result-item"));
    for (WebElement result : results) {
      if (result.getText().equals(institute)) {
        result.click();
        return;
      }
    }

    throw new NoSuchElementException("Could not find institute for name " + institute);
  }

  public void sendAdminGroup(String adminGroup) {
    adminGroupInput.clear();
    adminGroupInput.sendKeys(adminGroup);
  }

  public void sendEmail(String email) {
    managerEmailInput.clear();
    managerEmailInput.sendKeys(email);
  }

}
