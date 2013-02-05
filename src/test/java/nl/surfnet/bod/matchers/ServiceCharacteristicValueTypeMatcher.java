package nl.surfnet.bod.matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.tmforum.mtop.sb.xsd.svc.v1.ServiceCharacteristicValueType;

public class ServiceCharacteristicValueTypeMatcher {

  public static org.hamcrest.Matcher<ServiceCharacteristicValueType> hasServiceCharacteristic(final String name,
      final String value) {

    return new TypeSafeMatcher<ServiceCharacteristicValueType>() {
      @Override
      public void describeTo(Description description) {
        description
            .appendText("Name and value of a serviceCharacteristic should match, while the size of the Rdn must be one");
      }

      @Override
      protected boolean matchesSafely(ServiceCharacteristicValueType item) {
        return item.getSscRef().getRdn().size() == 1 && "SSC".equals(item.getSscRef().getRdn().get(0).getType())
            && name.equals(item.getSscRef().getRdn().get(0).getValue()) && value.equals(item.getValue());
      }

    };
  }
}
