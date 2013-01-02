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
package nl.surfnet.bod.idd;

import java.util.Arrays;
import java.util.Collection;

import javax.annotation.PostConstruct;

import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.idd.generated.Klanten;
import nl.surfnet.bod.idd.generated.Klantnamen;

import org.apache.axis.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public class IddOfflineClient implements IddClient {

  private static final String STATIC_RESPONSE_FILE = "/idd_response.xml";

  private final Logger logger = LoggerFactory.getLogger(IddOfflineClient.class);

  public IddOfflineClient(String username, String password, String endPoint) {
    // arguments are ignored but gives the same constructor as the iddLiveClient
  }

  public IddOfflineClient() {
  }

  @SuppressWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "Called by IoC container")
  @PostConstruct
  private void init() {
    logger.info("USING OFFLINE IDD CLIENT!");
  }

  @Override
  public Collection<Institute> getInstitutes() {
    Message message = getStaticMessage();

    Klanten[] klantnamen = extractKlantNamen(message);

    return IddUtils.transformKlanten(Arrays.asList(klantnamen), true);
  }

  private Klanten[] extractKlantNamen(Message message) {
    Klantnamen result;
    try {
      result = (Klantnamen) message.getSOAPEnvelope().getFirstBody().getObjectValue(Klantnamen.class);
      return result.getKlantnamen();
    }
    catch (Exception e) {
      logger.error("Could not load the institutes", e);
      return new Klanten[] {};
    }
  }

  private Message getStaticMessage() {
    return new Message(IddOfflineClient.class.getResourceAsStream(STATIC_RESPONSE_FILE));
  }

}
