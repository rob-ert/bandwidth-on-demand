/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
