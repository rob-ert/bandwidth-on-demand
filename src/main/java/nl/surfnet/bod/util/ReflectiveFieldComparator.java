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
package nl.surfnet.bod.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;

import org.springframework.util.StringUtils;

public class ReflectiveFieldComparator implements Comparator<Object> {

  private final String getter;

  public ReflectiveFieldComparator(final String field) {
    if (StringUtils.hasText(field)) {
      this.getter = "get" + field.substring(0, 1).toUpperCase() + field.substring(1);
    }
    else {
      this.getter = null;
    }
  }

  @SuppressWarnings("unchecked")
  public int compare(Object one, Object two) {
    try {
      if (one != null && two != null) {
        one = one.getClass().getMethod(getter, new Class[0]).invoke(one, new Object[0]);
        two = two.getClass().getMethod(getter, new Class[0]).invoke(two, new Object[0]);
      }
    }
    catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
        | SecurityException e) {
      throw new IllegalArgumentException("Cannot compare " + one + " with " + two + ". Does the getter: " + getter
          + " exists?", e);
    }
    return (one == null) ? -1 : ((two == null) ? 1 : ((Comparable<Object>) one).compareTo(two));
  }

}
