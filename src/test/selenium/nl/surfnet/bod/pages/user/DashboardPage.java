/**
 * Copyright (c) 2012, 2013 SURFnet BV
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
package nl.surfnet.bod.pages.user;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;

import nl.surfnet.bod.pages.AbstractListPage;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class DashboardPage extends AbstractListPage {

  private static final String PAGE = "/user";

  public DashboardPage(RemoteWebDriver driver) {
    super(driver);
  }

  public static DashboardPage get(RemoteWebDriver driver) {
    DashboardPage page = new DashboardPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public static DashboardPage get(RemoteWebDriver driver, String urlUnderTest) {
    driver.get(urlUnderTest + PAGE);
    return get(driver);
  }

  public List<String> getTeams() {
    return Lists.newArrayList(Iterables.transform(getRows(), new Function<WebElement, String>() {
      @Override
      public String apply(WebElement row) {
        return row.findElements(By.tagName("td")).get(0).getText();
      }
    }));
  }

  public void selectInstitute(String team) {
    try {
      getDriver().findElementByLinkText(team).click();
    } catch (NoSuchElementException e) {
      clickRowIcon("icon-envelope", team);
    }
  }

  /**
   * Asserts the number of menu items, if it fails don't just update the number,
   * but add new test cases for the new menu
   */
  public void verifyNumberOfMenuItems() {
    assertThat(getCountMenuItems(), is(6));
  }

  public void verifyMenuReservations() {
    clickMenuLink("Reservations");
    ListReservationPage page = ListReservationPage.get(getDriver());
    page.verifyIsCurrentPage();
    page.verifyHasDefaultTimeZone();
  }

  public void verifyMenuVirtualPorts() {
    clickMenuLink("Virtual");
    ListVirtualPortPage page = ListVirtualPortPage.get(getDriver());
    page.verifyIsCurrentPage();
    page.verifyHasDefaultTimeZone();
  }

  public void verifyMenuLogEvents() {
    clickMenuLink("Log");
    ListLogEventsPage page = ListLogEventsPage.get(getDriver());
    page.verifyIsCurrentPage();
    page.verifyHasDefaultTimeZone();
  }

  public void verifyMenuAdvanced() {
    clickMenuLink("Advanced");
  }

  public AdvancedPage clickAdvancedMenuLink(){
    clickMenuLink("Advanced");
    AdvancedPage page = AdvancedPage.get(getDriver());
    return page;
  }

  public ListVirtualPortPage clickVirtualPortsLink(){
    clickMenuLink("Virtual Ports");
    return ListVirtualPortPage.get(getDriver());
  }

  public void verifyMenuOverview() {
    clickMenuLink("Overview");
    verifyIsCurrentPage();
    verifyHasNoTimeZone();
  }

  public void verifyMenuReport() {
    clickMenuLink("Report");
    ReportPage page = ReportPage.get(getDriver());
    page.verifyIsCurrentPage();
  }

  public void verifyIsCurrentPage() {
    super.verifyIsCurrentPage(PAGE);
  }
}