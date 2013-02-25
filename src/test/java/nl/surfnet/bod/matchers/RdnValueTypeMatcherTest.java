package nl.surfnet.bod.matchers;

import static org.hamcrest.MatcherAssert.assertThat;
import nl.surfnet.bod.nbi.mtosi.MtosiUtils;

import org.junit.Test;
import org.tmforum.mtop.fmw.xsd.nam.v1.RelativeDistinguishNameType;

public class RdnValueTypeMatcherTest {

  @Test
  public void shouldMatch() {
    RelativeDistinguishNameType rdn = MtosiUtils.createRdn("type", "value");

    assertThat(rdn, RdnValueTypeMatcher.hasTypeValuePair("type", "value"));
  }

  @Test(expected = AssertionError.class)
  public void shouldNotMatch() {
    RelativeDistinguishNameType rdn = MtosiUtils.createRdn("type", "bla");

    assertThat(rdn, RdnValueTypeMatcher.hasTypeValuePair("type", "value"));
  }

}
