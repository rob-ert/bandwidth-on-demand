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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.LocalDateTime;

import com.google.common.base.Optional;

import nl.surfnet.bod.web.WebUtils;

public final class PageUtils {

  private PageUtils() {
  }

  public static Optional<LocalDateTime> extractDateTime(String text) {
    Matcher matcher = Pattern.compile(".*(\\d{4}-\\d{2}-\\d{2} \\d{1,2}:\\d{2}:\\d{2}).*").matcher(text);

    if (!matcher.matches()) return Optional.absent();

    return Optional.of(WebUtils.DEFAULT_DATE_TIME_FORMATTER.parseLocalDateTime(matcher.group(1)));
  }

}
