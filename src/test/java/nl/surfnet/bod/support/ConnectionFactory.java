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
package nl.surfnet.bod.support;

import java.util.UUID;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.Reservation;

import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.ogf.schemas.nsi._2011._10.connection.types.PathType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceParametersType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceTerminationPointType;

public class ConnectionFactory {

  private int desiredBandwidth;
  private String requesterNSA = "nsa:requester:surfnet.nl";
  private String providerNSA = "nsa:surfnet.nl";
  private String connectionId = UUID.randomUUID().toString();
  private String sourceStpId = "source port";
  private String destinationStpId = "destination port";
  private ConnectionStateType currentState = ConnectionStateType.INITIAL;
  private Reservation reservation = new ReservationFactory().create();
  private String globalReservationId = UUID.randomUUID().toString();
  private ServiceParametersType serviceParameters = new ServiceParametersType();

  public Connection create() {
    Connection connection = new Connection();

    connection.setDesiredBandwidth(desiredBandwidth);
    connection.setRequesterNsa(requesterNSA);
    connection.setProviderNsa(providerNSA);
    connection.setConnectionId(connectionId);
    connection.setSourceStpId(sourceStpId);
    connection.setDestinationStpId(destinationStpId);
    connection.setCurrentState(currentState);
    connection.setReservation(reservation);
    connection.setGlobalReservationId(globalReservationId);
    connection.setServiceParameters(serviceParameters);

    ServiceTerminationPointType sourceStp = new ServiceTerminationPointType();
    sourceStp.setStpId(sourceStpId);
    ServiceTerminationPointType dstStp = new ServiceTerminationPointType();
    dstStp.setStpId(destinationStpId);

    PathType pathType = new PathType();
    pathType.setDestSTP(dstStp);
    pathType.setSourceSTP(sourceStp);
    connection.setPath(pathType);

    return connection;
  }

  public ConnectionFactory setReservation(Reservation reservation) {
    this.reservation = reservation;
    return this;
  }

  public ConnectionFactory setDestinationStpId(String destinationStpId) {
    this.destinationStpId = destinationStpId;
    return this;
  }

  public ConnectionFactory setSourceStpId(String sourceStpId) {
    this.sourceStpId = sourceStpId;
    return this;
  }

  public ConnectionFactory setConnectionId(String connectionId) {
    this.connectionId = connectionId;
    return this;
  }

  public ConnectionFactory setProviderNSA(String providerNSA) {
    this.providerNSA = providerNSA;
    return this;
  }

  public ConnectionFactory setRequesterNSA(String requesterNSA) {
    this.requesterNSA = requesterNSA;
    return this;
  }

  public ConnectionFactory setDesiredBandwidth(int desiredBandwidth) {
    this.desiredBandwidth = desiredBandwidth;
    return this;
  }

  public ConnectionFactory setCurrentState(ConnectionStateType currentState) {
    this.currentState = currentState;
    return this;
  }
}
