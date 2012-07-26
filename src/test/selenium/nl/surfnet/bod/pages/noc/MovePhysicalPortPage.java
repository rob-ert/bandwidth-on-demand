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

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

import nl.surfnet.bod.pages.AbstractPage;

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
