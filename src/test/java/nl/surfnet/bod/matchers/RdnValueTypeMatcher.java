package nl.surfnet.bod.matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.tmforum.mtop.fmw.xsd.nam.v1.RelativeDistinguishNameType;

public class RdnValueTypeMatcher {

  public static org.hamcrest.Matcher<RelativeDistinguishNameType> hasRdn(final String type,
      final String value) {

    return new TypeSafeMatcher<RelativeDistinguishNameType>() {
      @Override
      public void describeTo(Description description) {
        description
            .appendText("Type and value of a rdn should match");
      }

      @Override
      protected boolean matchesSafely(RelativeDistinguishNameType item) {
        return item.getType().equals(type) && item.getValue().equals(value);
      }

    };
  }
}
