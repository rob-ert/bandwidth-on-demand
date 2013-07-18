/**
 * Copyright (c) 2012, 2013 SURFnet BV
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
package nl.surfnet.bod.idd;

import java.util.Arrays;
import java.util.Collection;

import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.idd.generated.InvoerKlant;
import nl.surfnet.bod.idd.generated.Klanten;
import nl.surfnet.bod.idd.generated.KsrBindingStub;
import nl.surfnet.bod.idd.generated.KsrLocator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("idd")
public class IddLiveClient implements IddClient {

  private static final String IDD_VERSION = "1.09";

  private Logger logger = LoggerFactory.getLogger(IddLiveClient.class);

  private final String username;
  private final String password;
  private final String endPoint;
  private final Integer timeout;

  @Autowired
  public IddLiveClient(@Value("${idd.user}") String username, @Value("${idd.password}") String password,
      @Value("${idd.url}") String endPoint, @Value("${idd.timeout}") Integer timeout) {
    this.username = username;
    this.password = password;
    this.endPoint = endPoint;
    this.timeout = timeout;
  }

  @Override
  public synchronized Collection<Institute> getInstitutes() {
    logger.info("Calling IDD");

    try {
      KsrLocator locator = new KsrLocator();
      locator.setksrPortEndpointAddress(endPoint);

      KsrBindingStub port = (KsrBindingStub) locator.getksrPort();
      port.setTimeout(timeout);
      port.setUsername(username);
      port.setPassword(password);


      Klanten[] klantnamen = port.getKlantList(new InvoerKlant("list", "", IDD_VERSION)).getKlantnamen();

      return IddUtils.transformKlanten(Arrays.asList(klantnamen), true);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
