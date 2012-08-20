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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.joda.time.LocalDateTime;
import org.junit.Test;

import com.google.common.base.Optional;

public class PageUtilsTest {

  @Test
  public void shouldBeAbleToExtractDateWhenHourHasOnlyOneDigit() {
    Optional<LocalDateTime> extractDateTime = PageUtils.extractDateTime("asfaf asdfasdf asdf 2012-07-25 9:28:49 asdf asdf");

    assertThat(extractDateTime.isPresent(), is(true));
  }

  @Test
  public void shouldBeAbleToExtractDateWhenHourHasTwoDigits() {
    Optional<LocalDateTime> extractDateTime = PageUtils.extractDateTime("asfaf asdfasdf asdf 2012-07-25 12:28:49 asdf asdf");

    assertThat(extractDateTime.isPresent(), is(true));
  }

  @Test
  public void shouldBeAbleToExtractADate() {
    Optional<LocalDateTime> extractDateTime = PageUtils.extractDateTime("asfaf asdfasdf asdf 201207a25 1:2849 asdf asdf");

    assertThat(extractDateTime.isPresent(), is(false));
  }
}
