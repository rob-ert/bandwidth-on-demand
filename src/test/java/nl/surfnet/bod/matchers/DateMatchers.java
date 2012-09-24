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
package nl.surfnet.bod.matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.joda.time.DateTime;

public class DateMatchers {

  private DateMatchers() {
  };

  public static org.hamcrest.Matcher<DateTime> isAfterNow() {
    return new TypeSafeMatcher<DateTime>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("should be after").appendValue(DateTime.now());
      }

      @Override
      protected boolean matchesSafely(DateTime other) {
        return other.isAfter(DateTime.now());
      }
    };
  }
}
