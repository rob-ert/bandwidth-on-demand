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
package nl.surfnet.bod.pages.physicalresourcegroup;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ListPhysicalResourceGroupPage {

  private static final String PAGE = "noc/physicalresourcegroups";
  private final RemoteWebDriver driver;

  @FindBy(css = "table.zebra-striped tbody")
  private WebElement table;

  public ListPhysicalResourceGroupPage(RemoteWebDriver driver) {
    this.driver = driver;
  }

  public static ListPhysicalResourceGroupPage get(RemoteWebDriver driver, String urlUnderTest) {
    driver.get(urlUnderTest + PAGE);
    return get(driver);
  }

  public static ListPhysicalResourceGroupPage get(RemoteWebDriver driver) {
    ListPhysicalResourceGroupPage page = new ListPhysicalResourceGroupPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public String getTable() {
    return table.getText();
  }

  public void deleteByName(String name) {
    List<WebElement> rows = table.findElements(By.tagName("tr"));

    for (WebElement row : rows) {
      if (row.getText().contains(name)) {
        WebElement deleteButton = row.findElement(By.cssSelector("input[type=image]"));
        deleteButton.click();
        driver.switchTo().alert().accept();
        return;
      }
    }

    throw new AssertionError(String.format("Physical resource group with name '%s' not found", name));
  }
}
