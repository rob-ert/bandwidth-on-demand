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
package nl.surfnet.bod.pages;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import nl.surfnet.bod.support.Probes;

import org.joda.time.LocalDateTime;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;

public class AbstractPage {

  private static final String CSS_SELECTOR_TIMEZONE = "header-right";

  private static final String XPATH_MENU_ITEM = ".//a[contains(text(), '%s')]";

  private final RemoteWebDriver driver;

  private final Probes probes;

  @FindBy(id = "alerts")
  private WebElement messagesDiv;

  @FindBy(css = ".user-box .dropdown-toggle")
  private WebElement userBox;

  @FindBy(css = ".navbar")
  private WebElement navBar;

  public AbstractPage(RemoteWebDriver driver) {
    this.driver = driver;
    probes = new Probes(driver);
  }

  public List<String> getInfoMessages() {
    List<WebElement> messageDivs = messagesDiv.findElements(By.className("alert-info"));
    return Lists.transform(messageDivs, new Function<WebElement, String>() {
      @Override
      public String apply(WebElement input) {
        return input.getText();
      }
    });
  }

  public void clickSwitchToDefaultUser() {
    getDriver().findElementByLinkText("RD").click();
  }

  public void clickSwitchToUserHans() {
    getDriver().findElementByLinkText("RH").click();
  }

  public void clickSwitchRole(String... roleNames) {
    userBox.click();

    List<WebElement> roles = userBox.findElements(By.tagName("li"));
    for (WebElement role : roles) {
      if (containsAll(role.getText(), roleNames)) {
        role.click();
        return;
      }
    }

    throw new NoSuchElementException("Could not find role with name " + Joiner.on(", ").join(roleNames) + " in "
        + Joiner.on(", ").join(Iterables.transform(roles, new Function<WebElement, String>() {
          @Override
          public String apply(WebElement input) {
            return input.getText();
          }
        })));
  }

  protected LocalDateTime getLocalDateTimeFromRow(WebElement row) {
    Optional<LocalDateTime> extractedDateTime = PageUtils.extractDateTime(row.getText());
    if (!extractedDateTime.isPresent()) {
      throw new AssertionError("Could not find date time form row: " + row.getText());
    }
    return extractedDateTime.get();
  }

  protected Probes getProbes() {
    return probes;
  }

  protected RemoteWebDriver getDriver() {
    return driver;
  }

  protected WebElement getMenuBar() {
    return navBar;
  }

  protected int getCountMenuItems() {
    return navBar.findElements(By.xpath(".//a")).size();
  }

  /**
   * Verifies that the current page url contains the given page. Tries a number
   * of times, to give the page the time to load.
   *
   * @param pageUrlPart
   */
  protected void verifyIsCurrentPage(String pageUrlPart) {
    for (int i = 0; i < 75; i++) {
      if (getDriver().getCurrentUrl().contains(pageUrlPart)) {
        break;
      }
      else {
        Uninterruptibles.sleepUninterruptibly(150, TimeUnit.MILLISECONDS);
      }
    }
    assertThat(getDriver().getCurrentUrl(), containsString(pageUrlPart));
  }

  protected void clickMenuLink(String link) {
    getMenuBar().findElement(By.xpath(String.format(XPATH_MENU_ITEM, link))).click();
  }

  public void verifyHasDefaultTimeZone() {
    String timeZoneText = getDriver().findElement(By.className(CSS_SELECTOR_TIMEZONE)).getText();
    assertThat(timeZoneText, containsString(TimeZone.getDefault().getID()));
  }

  public void verifyHasNoTimeZone() {
    try {
      getDriver().findElement(By.cssSelector(CSS_SELECTOR_TIMEZONE));
      assertThat("Should not contain timezone", false);
    }
    catch (NoSuchElementException exc) {
      // expected, should not be present
      assertThat("No timezone present", true);
    }
  }

  private boolean containsAll(String input, String[] needles) {
    for (String needle : needles) {
      if (!input.contains(needle)) {
        return false;
      }
    }
    return true;
  }

  public WebElement findDataItem(String label) {
    WebElement definitionList = getDriver().findElementByCssSelector(".dl");

    return definitionList.findElement(By.partialLinkText(label));
  }

  public void verifyOneInfoMessage(String text) {
    List<String> infoMessages = getInfoMessages();
    assertThat(infoMessages, hasSize(1));
    assertThat(infoMessages.get(0), containsString(text));
  }
}
