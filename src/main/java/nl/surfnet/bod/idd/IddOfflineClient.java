/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
package nl.surfnet.bod.idd;

import java.util.Arrays;
import java.util.Collection;

import javax.annotation.PostConstruct;

import nl.surfnet.bod.idd.generated.Klanten;
import nl.surfnet.bod.idd.generated.Klantnamen;

import org.apache.axis.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IddOfflineClient implements IddClient {

  private static final String STATIC_RESPONSE_FILE = "/idd_response.xml";

  private final Logger logger = LoggerFactory.getLogger(IddOfflineClient.class);

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    logger.info("USING OFFLINE IDD CLIENT!");
  }

  @Override
  public Collection<Klanten> getKlanten() {
    Message message = getStaticMessage();

    Klanten[] klantnamen = extractKlantNamen(message);

    return Arrays.asList(klantnamen);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Klanten getKlantById(final Long klantId) {
    Klanten matchedKlant = null;

    Collection<Klanten> klanten = getKlanten();

    for (Klanten foundKlant : klanten) {
      if (klantId == foundKlant.getKlant_id()) {
        matchedKlant = foundKlant;
        break;
      }
    }
    return matchedKlant;
  }

  private Klanten[] extractKlantNamen(Message message) {
    Klantnamen result;
    try {
      result = (Klantnamen) message.getSOAPEnvelope().getFirstBody().getObjectValue(Klantnamen.class);
      return result.getKlantnamen();
    } catch (Exception e) {
      logger.error("Could not load the institutes", e);
      return new Klanten[] {};
    }
  }

  private Message getStaticMessage() {
    return new Message(IddOfflineClient.class.getResourceAsStream(STATIC_RESPONSE_FILE));
  }

}
