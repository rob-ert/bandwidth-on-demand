package nl.surfnet.bod.matchers;

import org.hamcrest.Description;
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

}
