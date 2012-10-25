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
package nl.surfnet.bod.web.tag;

import org.springframework.util.StringUtils;

import com.google.common.base.Optional;

public final class Functions {

  private Functions() {
  }

  public static String translateNewLineBr(String input) {
    return StringUtils.replace(input, "\n", "<br/>");
  }

  public static <T> String getOr(Optional<T> optional, T otherwise) {
    return optional.or(otherwise).toString();
  }

  public static String get(Optional<?> optional) {
    return optional.get().toString();
  }
}
