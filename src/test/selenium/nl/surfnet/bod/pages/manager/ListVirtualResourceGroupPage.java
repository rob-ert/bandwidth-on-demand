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
package nl.surfnet.bod.pages.manager;

import nl.surfnet.bod.pages.AbstractListPage;
import nl.surfnet.bod.support.Probes;
import nl.surfnet.bod.web.manager.VirtualResourceGroupController;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

public class ListVirtualResourceGroupPage extends AbstractListPage {

  private static final String PAGE =  "/manager/"  + VirtualResourceGroupController.PAGE_URL;
  
  public ListVirtualResourceGroupPage(RemoteWebDriver driver) {
    super(driver);
    setProbes(new Probes(driver));
  }
  
  public static ListVirtualResourceGroupPage get(RemoteWebDriver driver, String baseUrl) {
    driver.get(baseUrl + PAGE);
    return get(driver);
  }

  public static ListVirtualResourceGroupPage get(RemoteWebDriver driver) {
    ListVirtualResourceGroupPage page = new ListVirtualResourceGroupPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

}
