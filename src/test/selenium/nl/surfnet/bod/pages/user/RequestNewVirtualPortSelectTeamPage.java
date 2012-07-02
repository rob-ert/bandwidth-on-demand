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

import nl.surfnet.bod.pages.AbstractPage;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public final class RequestNewVirtualPortSelectTeamPage extends AbstractPage {

  @FindBy(id="_all")
  private WebElement radioButton;

  private RequestNewVirtualPortSelectTeamPage(RemoteWebDriver driver) {
    super(driver);
  }

  public static RequestNewVirtualPortSelectTeamPage get(RemoteWebDriver driver) {
    RequestNewVirtualPortSelectTeamPage page = new RequestNewVirtualPortSelectTeamPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public RequestNewVirtualPortRequestPage selectTeam(String team) {

    radioButton.click();
    getDriver().findElementByLinkText(team).click();

    return RequestNewVirtualPortRequestPage.get(getDriver());
  }

}
