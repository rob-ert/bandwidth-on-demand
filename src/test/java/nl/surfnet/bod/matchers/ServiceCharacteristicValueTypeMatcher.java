package nl.surfnet.bod.matchers;

import static nl.surfnet.bod.web.WebUtils.not;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmforum.mtop.fmw.xsd.nam.v1.RelativeDistinguishNameType;
import org.tmforum.mtop.sb.xsd.svc.v1.ServiceCharacteristicValueType;

public class ServiceCharacteristicValueTypeMatcher {
  private static final Logger logger = LoggerFactory.getLogger(ServiceCharacteristicValueTypeMatcher.class);

  public static org.hamcrest.Matcher<ServiceCharacteristicValueType> hasServiceCharacteristic(final String name,
      final String value) {

    return new TypeSafeMatcher<ServiceCharacteristicValueType>() {
      @Override
      public void describeTo(Description description) {
        description
            .appendText("Name and value of a serviceCharacteristic should match, while the size of the Rdn must be one. Check log for details");
      }

      @Override
      protected boolean matchesSafely(ServiceCharacteristicValueType item) {
        RelativeDistinguishNameType rdn = item.getSscRef().getRdn().get(0);

        boolean result = item.getSscRef().getRdn().size() == 1
            && "SSC".equals(rdn.getType())
            && name.equals(rdn.getValue()) && value.equals(item.getValue());

        if (not(result)) {
          logger.warn("Expected name [{}] and value [{}], but was: [{}] and [{}]", name, value, rdn.getValue(), item
              .getValue());
        }

        return result;
      }

    };
  }
}
