package nl.surfnet.bod.matchers;

import static nl.surfnet.bod.web.WebUtils.not;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmforum.mtop.fmw.xsd.nam.v1.RelativeDistinguishNameType;

public class RdnValueTypeMatcher {
  private static final Logger logger = LoggerFactory.getLogger(RdnValueTypeMatcher.class);

  public static org.hamcrest.Matcher<RelativeDistinguishNameType> hasTypeValuePair(final String type,
      final String value) {

    return new TypeSafeMatcher<RelativeDistinguishNameType>() {
      @Override
      public void describeTo(Description description) {
        description
            .appendText("Type type and value of a RDN should match. Check log for details");
      }

      @Override
      protected boolean matchesSafely(RelativeDistinguishNameType item) {
        boolean result = item.getType().equals(type) && item.getValue().equals(value);

        if (not(result)) {
          logger.warn("Expected type [{}] and value [{}], but was: [{}] and [{}]", type, value, item.getType(), item
              .getValue());
        }

        return result;
      }
    };
  }
}