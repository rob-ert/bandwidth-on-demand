package nl.surfnet.bod.matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class NumberOfLinesMatchers {

  public static org.hamcrest.Matcher<String> hasLines(final int lines) {
    return new TypeSafeMatcher<String>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("should countain " + lines);
      }

      @Override
      protected boolean matchesSafely(String input) {
        return input.split("\n").length == lines;
      }
    };
  }
}
