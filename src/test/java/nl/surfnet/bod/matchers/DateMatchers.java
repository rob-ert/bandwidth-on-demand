package nl.surfnet.bod.matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.joda.time.LocalDateTime;

public class DateMatchers {

  public static org.hamcrest.Matcher<LocalDateTime> isAfterNow() {
    return new TypeSafeMatcher<LocalDateTime>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("should be after").appendValue(LocalDateTime.now());
      }

      @Override
      protected boolean matchesSafely(LocalDateTime other) {
        return other.isAfter(LocalDateTime.now());
      }
    };
  }
}
