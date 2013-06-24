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
package nl.surfnet.bod.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import com.google.common.base.Optional;

public class OptionalMatchers {

  public static org.hamcrest.Matcher<Optional<?>> isPresent() {
    return new TypeSafeMatcher<Optional<?>>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("should be present");
      }

      @Override
      protected boolean matchesSafely(Optional<?> other) {
        return other.isPresent();
      }
    };
  }

  public static org.hamcrest.Matcher<Optional<?>> isAbsent() {
    return new TypeSafeMatcher<Optional<?>>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("should be absent");
      }

      @Override
      protected boolean matchesSafely(Optional<?> other) {
        return !other.isPresent();
      }
    };
  }

  public static <T> org.hamcrest.Matcher<Optional<T>> isPresent(final T value) {
    return new TypeSafeMatcher<Optional<T>>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("should be present");
      }

      @Override
      protected boolean matchesSafely(Optional<T> other) {
        return other.isPresent() && other.get().equals(value);
      }
    };
  }

  public static <T> org.hamcrest.Matcher<Optional<T>> isPresent(final Matcher<T> matcher) {
    return new TypeSafeMatcher<Optional<T>>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("should be present");
      }

      @Override
      protected boolean matchesSafely(Optional<T> other) {
        return other.isPresent() && matcher.matches(other.get());
      }
    };
  }

}
