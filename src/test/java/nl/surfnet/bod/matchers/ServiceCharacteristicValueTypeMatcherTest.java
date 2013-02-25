package nl.surfnet.bod.matchers;

import static org.junit.Assert.assertThat;
import nl.surfnet.bod.nbi.mtosi.MtosiUtils;

import org.junit.Test;
import org.tmforum.mtop.sb.xsd.svc.v1.ServiceCharacteristicValueType;

public class ServiceCharacteristicValueTypeMatcherTest {

  @Test
  public void shouldMatch() {
    ServiceCharacteristicValueType ssc = MtosiUtils.createSscRef("name", MtosiUtils.createNamingAttributeType("SSC",
        "value").getValue());
    assertThat(ssc, ServiceCharacteristicValueTypeMatcher.hasServiceCharacteristic("value", "name"));
  }

  @Test(expected = AssertionError.class)
  public void shouldNotMatch() {
    ServiceCharacteristicValueType ssc = MtosiUtils.createSscRef("name", MtosiUtils.createNamingAttributeType("SSC",
        "bla").getValue());
    assertThat(ssc, ServiceCharacteristicValueTypeMatcher.hasServiceCharacteristic("value", "name"));
  }
}
