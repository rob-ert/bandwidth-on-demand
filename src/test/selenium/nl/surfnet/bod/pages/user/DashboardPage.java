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
package nl.surfnet.bod.pages.user;

import java.util.List;

import nl.surfnet.bod.pages.AbstractListPage;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DashboardPage extends AbstractListPage {

  private static final String PAGE = "/user";

  @FindBy(css = ".navbar")
  private WebElement navBar;

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
      getDriver().findElementByPartialLinkText(team).click();
    }
    catch (NoSuchElementException e) {
      clickRowIcon("icon-envelope", team);
    }
  }

  /**
   * Asserts the number of menu items, if it fails don't just update the number,
   * but add new test cases for the new menu
   */
  public void verifyNumberOfMenuItems() {
    assertThat(getCountMenuItems(), is(5));
  }

  public void verifyMenuReservations() {
    navBar.findElement(By.xpath(".//a[contains(text(), 'Reservations')]")).click();
    ListReservationPage page = ListReservationPage.get(getDriver());
    page.verifyIsCurrentPage();
    page.verifyHasDefaultTimeZone();
  }

  public void verifyMenuVirtualPorts() {
    navBar.findElement(By.xpath(".//a[contains(text(), 'Virtual')]")).click();
    ListVirtualPortPage page = ListVirtualPortPage.get(getDriver());
    page.verifyIsCurrentPage();
    page.verifyHasDefaultTimeZone();
  }

  public void verifyMenuLogEvents() {
    navBar.findElement(By.xpath(".//a[contains(text(), 'Log')]")).click();
    ListLogEventsPage page = ListLogEventsPage.get(getDriver());
    page.verifyIsCurrentPage();
    page.verifyHasDefaultTimeZone();
  }

  /**
   * TODO page not finished yet, so nothing to assert
   */
  public void verifyMenuNSI() {
    navBar.findElement(By.xpath(".//a[contains(text(), 'NSI')]"));
  }

  public void verifyMenuOverview() {
    getMenuBar().findElement(By.xpath(".//a[contains(text(), 'Overview')]")).click();
    verifyIsCurrentPage();
    verifyHasNoTimeZone();
  }

  public void verifyIsCurrentPage() {
    super.verifyIsCurrentPage(PAGE);
  }
}
