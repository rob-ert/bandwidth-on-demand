/**
 * Copyright (c) 2012, SURFnet BV
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
package nl.surfnet.bod.support;

import static nl.surfnet.bod.support.BodWebDriver.URL_UNDER_TEST;
import nl.surfnet.bod.pages.appmanager.DashboardPage;
import nl.surfnet.bod.pages.appmanager.ShibbolethAttributesPage;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

public class BodAppManagerWebDriver extends AbstractBoDWebDriver<DashboardPage> {

  private final RemoteWebDriver driver;

  public BodAppManagerWebDriver(RemoteWebDriver driver) {
    this.driver = driver;
  }

  public void verifyRefreshInstitutesLink() {
    DashboardPage dashboardPage = DashboardPage.get(driver, URL_UNDER_TEST);
    WebElement refreshInstitutesLink = dashboardPage.findDataItem("institutes");
    refreshInstitutesLink.click();

    dashboardPage.verifyIsCurrentPage();
    dashboardPage.verifyOneInfoMessage("Institutes");
  }

  public void verifyReIndexSearchDatabaseLink() {
    DashboardPage dashboardPage = DashboardPage.get(driver, URL_UNDER_TEST);
    WebElement reindexDatabaseLink = dashboardPage.findDataItem("database");
    reindexDatabaseLink.click();

    dashboardPage.verifyIsCurrentPage();
    dashboardPage.verifyOneInfoMessage("database");
  }

  public void verifyShibbolethAttributesLink() {
    DashboardPage dashboardPage = DashboardPage.get(driver, URL_UNDER_TEST);
    WebElement shibbolethAttributesLink = dashboardPage.findDataItem("shibboleth");
    shibbolethAttributesLink.click();

    ShibbolethAttributesPage shibbolethAttributesPage = ShibbolethAttributesPage.get(driver, URL_UNDER_TEST);
    shibbolethAttributesPage.verifyIsCurrentPage();
  }

  public void verifyMenu() {
    DashboardPage dashboardPage = DashboardPage.get(driver, URL_UNDER_TEST);
    dashboardPage.verifyNumberOfMenuItems();
    dashboardPage.verifyMenuOverview();
  }

  @Override
  protected DashboardPage getDashboardPage() {
    return DashboardPage.get(driver, URL_UNDER_TEST);
  }

  public void verifyDevelopmentLinkAmount(int amount) {
    DashboardPage dashboardPage = DashboardPage.get(driver, URL_UNDER_TEST);
    dashboardPage.verifyDevelopmentLinkAmount(amount);
    dashboardPage.verifyIsCurrentPage();
  }

  public void verifyDevelopmentLinkRefreshMessage() {
    DashboardPage dashboardPage = DashboardPage.get(driver, URL_UNDER_TEST);
    dashboardPage.verifyDevelopmentLinkRefreshMessage();
    dashboardPage.verifyIsCurrentPage();
  }

  public void verifyDevelopmentLinkRefreshRoles() {
    DashboardPage dashboardPage = DashboardPage.get(driver, URL_UNDER_TEST);
    dashboardPage.verifyDevelopmentLinkRefreshRoles();
    dashboardPage.verifyIsCurrentPage();
  }

}
