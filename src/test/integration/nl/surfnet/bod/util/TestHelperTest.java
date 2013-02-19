package nl.surfnet.bod.util;

import nl.surfnet.bod.util.TestHelper.PropertiesEnvironment;

import org.junit.Test;

public class TestHelperTest {

  @Test
  public void shouldEncryptProperty() {
    PropertiesEnvironment propertiesEnvironment = new TestHelper.PropertiesEnvironment();
    System.err.println(propertiesEnvironment.encryptProperty("value"));
  }
}
