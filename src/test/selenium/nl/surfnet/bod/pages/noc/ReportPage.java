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

import nl.surfnet.bod.pages.AbstractListPage;

import org.junit.Assert;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

public class ReportPage extends AbstractListPage {

  private static final String PAGE = "/noc/report";

  public ReportPage(RemoteWebDriver driver) {
    super(driver);
  }

  public static ReportPage get(RemoteWebDriver driver) {
    ReportPage page = new ReportPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public static ReportPage get(RemoteWebDriver driver, String urlUnderTest) {
    driver.get(urlUnderTest + PAGE);
    return get(driver);
  }

  public void verifyAmountOfFailedReservationRequests() {
    WebElement row =  findRow("Failed");
    String text = row.getText();
    
  //*[@id="idReport"]/tbody/tr[1]/td[3]/table/tbody/tr[1]/td[2]

  }

  public void verifyAmountOfSucceededReservationRequests() {
    Assert.fail("not implemented");

  }

  public void verifyAmountOfProtectedReservations() {

  }

  public void verifyAmountOfUnProtectedReservations() {

  }

  public void verifyAmountOfRedundantReservations() {

  }

  public void verifyAmountOfNSIReservations() {

  }

  public void verifyAmountOfGUIReservations() {

  }

  public void verifyAmountOfSucceedReservations() {

  }

  public void verifyAmountOfCancelledReservations() {

  }

  public void verifyAmountOfFailedReservations() {

  }
}