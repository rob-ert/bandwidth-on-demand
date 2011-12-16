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
package nl.surfnet.bod.nbi;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import nl.surfnet.bod.nbi.generated.InventoryResponse;
import nl.surfnet.bod.nbi.generated.TerminationPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class NbiOfflineClient implements NbiClient {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private Unmarshaller unmarshaller;

  @PostConstruct
  protected void init() throws JAXBException {
    unmarshaller = JAXBContext.newInstance("nl.surfnet.bod.nbi.generated").createUnmarshaller();
    log.info("USING OFFLINE NBI CLIENT!");
  }

  @Override
  public List<TerminationPoint> findAllPorts() {
    try {
      InputStream stream = getOfflineResponseFile();
      InventoryResponse inventoryResponse = (InventoryResponse) unmarshaller.unmarshal(stream);

      return Lists.transform(inventoryResponse.getTerminationPoint(), new Function<TerminationPoint, TerminationPoint>() {
        @Override
        public TerminationPoint apply(TerminationPoint input) {
          String oldName = input.getPortDetail().getName();
          input.getPortDetail().setName(oldName + "_dummy");
          return input;
        }
      });
    }
    catch (JAXBException e) {
      log.error("Could not load termination points from file", e);
      return Collections.emptyList();
    }
  }

  private InputStream getOfflineResponseFile() {
    return NbiOfflineClient.class.getResourceAsStream("/nbi_response.xml");
  }

}
