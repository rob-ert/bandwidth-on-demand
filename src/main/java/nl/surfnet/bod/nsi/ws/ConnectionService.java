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
package nl.surfnet.bod.nsi.ws;

import java.util.UUID;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

import nl.surfnet.bod.repo.ConnectionRepo;
import nl.surfnet.bod.repo.VirtualPortRepo;
import nl.surfnet.bod.repo.VirtualResourceGroupRepo;
import nl.surfnet.bod.service.ReservationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

public abstract class ConnectionService {

  private static final String URN_UUID = "urn:uuid:";

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Resource
  private ConnectionRepo connectionRepo;

  @Resource
  private VirtualPortRepo virtualPortRepo;

  @Resource
  private VirtualResourceGroupRepo virtualResourceGroupRepo;

  /*
   * This holds the web service request context which includes all the original
   * HTTP information, including the JAAS authentication and authorization
   * information.
   */
  @Resource
  private WebServiceContext webServiceContext;

  @Autowired
  private ReservationService reservationService;

  protected boolean isValidId(final String id) {
    return StringUtils.hasText(id);
  }

  public static String getCorrelationId() {
    return URN_UUID + UUID.randomUUID().toString();
  }

  protected Logger getLog() {
    return log;
  }

  protected final WebServiceContext getWebServiceContext() {
    return webServiceContext;
  }

  protected  ReservationService getReservationService() {
    return reservationService;
  }

  protected ConnectionRepo getConnectionRepo() {
    return connectionRepo;
  }

  protected VirtualPortRepo getVirtualPortRepo() {
    return virtualPortRepo;
  }

  protected VirtualResourceGroupRepo getVirtualResourceGroupRepo() {
    return virtualResourceGroupRepo;
  }

  static {
    System.setProperty("com.sun.xml.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace", "false");
  }

}
