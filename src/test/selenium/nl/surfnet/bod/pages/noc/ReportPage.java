/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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