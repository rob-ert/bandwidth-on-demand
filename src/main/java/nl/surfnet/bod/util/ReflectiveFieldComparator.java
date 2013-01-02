/**
 * Copyright (c) 2012, SURFnet BV
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
