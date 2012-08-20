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

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.NsiRequestDetails;

import org.ogf.schemas.nsi._2011._10.connection.provider.ConnectionProviderPort;

public interface ConnectionServiceProvider extends ConnectionProviderPort {

  String URN_OGF = "urn:ogf:network";
  String NETWORK_ID = "surfnet.nl";
  String URN_PROVIDER_NSA = URN_OGF + ":nsa:" + NETWORK_ID;
  String URN_STP = URN_OGF + ":stp:" + NETWORK_ID;
  String URN_GLOBAL_RESERVATION_ID = "urn:nl:surfnet:diensten:bod";

  void reserveConfirmed(Connection connection, NsiRequestDetails requestDetails);

  void reserveFailed(Connection connection, NsiRequestDetails requestDetails);

  void provisionConfirmed(Connection connection, NsiRequestDetails requestDetails);

  void provisionFailed(Connection connection, NsiRequestDetails requestDetails);

  void terminateConfirmed(Connection connection, NsiRequestDetails requestDetails);

  void terminateFailed(Connection connection, NsiRequestDetails requestDetails);
}
