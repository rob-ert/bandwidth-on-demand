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
package nl.surfnet.bod.pages;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

public abstract class AbstractLogEventListPage extends AbstractListPage {

  public AbstractLogEventListPage(RemoteWebDriver driver) {
    super(driver);
  }

  public Integer getNumberOfLogEvents() {
    return getRows().size();
  }

  public void logEventShouldBe(DateTime created, String... fields) {

    WebElement row = findRow(fields);

    LocalDateTime logEventCreated = getLocalDateTimeFromRow(row);

    long duration = created.getMillisOfDay() - logEventCreated.getMillisOfDay();
    // Allow an x second margin
    assertThat(duration, lessThan(60000L));
  }
}
