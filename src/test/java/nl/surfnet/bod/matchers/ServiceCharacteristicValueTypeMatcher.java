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
