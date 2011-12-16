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

import java.io.StringReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
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
import org.springframework.beans.factory.annotation.Value;

import com.adventnet.security.authentication.RMIAccessAPI;
import com.adventnet.security.authentication.RMIAccessException;
import com.esm.server.api.oss.OSSHandle;

public class NbiLiveClient implements NbiClient {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Value("${nbi.user}")
  private String username;

  @Value("${nbi.password}")
  private String password;

  @Value("${nbi.url}")
  private String url;

  private JAXBContext jaxbContext;
  private Unmarshaller unMarshaller;
  private OSSHandle ossHandle;

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() throws RemoteException, RMIAccessException, MalformedURLException, NotBoundException,
      JAXBException {
    log.info("Connecting with username {} to: {}", username, url);
    final RMIAccessAPI rmiAccessApi = (RMIAccessAPI) Naming.lookup(url);

    log.info("Looked up EMS RMI access API: {}", rmiAccessApi);
    ossHandle = (OSSHandle) rmiAccessApi.getAPI(username, password, "OSSHandle");
    log.info("Looked up OSS handle: {}", ossHandle);

    jaxbContext = JAXBContext.newInstance("nl.surfnet.bod.nbi.generated");
    unMarshaller = jaxbContext.createUnmarshaller();
  }

  private List<TerminationPoint> findByFilter(final String filter) {
    try {
      log.debug("Retrieving by filter: {}", filter);
      final String allPortsXml = ossHandle.getInventory(username, password, "getResourcesWithAttributes", filter, null);

      log.debug("Retrieved all ports: {}", allPortsXml);
      return ((InventoryResponse) unMarshaller.unmarshal(new StringReader(allPortsXml))).getTerminationPoint();
    }
    catch (Exception e) {
      log.error("Error: ", e);
      return Collections.emptyList();
    }
  }

  @Override
  public List<TerminationPoint> findAllPorts() {
    return findByFilter("type=Port");
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

  public void setUrl(final String url) {
    this.url = url;
  }

}
