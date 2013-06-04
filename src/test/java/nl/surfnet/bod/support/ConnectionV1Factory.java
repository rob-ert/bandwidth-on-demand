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
package nl.surfnet.bod.support;

import java.util.UUID;

import nl.surfnet.bod.domain.ConnectionV1;
import nl.surfnet.bod.domain.Reservation;

import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.ogf.schemas.nsi._2011._10.connection.types.PathType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceParametersType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceTerminationPointType;

public class ConnectionV1Factory {

  private int desiredBandwidth;
  private String requesterNsa = "nsa:requester:surfnet.nl";
  private String providerNsa = "nsa:surfnet.nl";
  private String connectionId = UUID.randomUUID().toString();
  private String sourceStpId = "source port";
  private String destinationStpId = "destination port";
  private ConnectionStateType currentState = ConnectionStateType.INITIAL;
  private Reservation reservation = new ReservationFactory().create();
  private final String globalReservationId = UUID.randomUUID().toString();
  private final ServiceParametersType serviceParameters = new ServiceParametersType();
  private String protectionType = "PROTECTED";
  private Long id = 0L;
  private Integer version = 0;
  private String description = "";

  public ConnectionV1 create() {
    ConnectionV1 connection = new ConnectionV1();

    connection.setId(id);
    connection.setVersion(version);
    connection.setDesiredBandwidth(desiredBandwidth);
    connection.setRequesterNsa(requesterNsa);
    connection.setProviderNsa(providerNsa);
    connection.setConnectionId(connectionId);
    connection.setSourceStpId(sourceStpId);
    connection.setDestinationStpId(destinationStpId);
    connection.setCurrentState(currentState);
    connection.setReservation(reservation);
    connection.setGlobalReservationId(globalReservationId);
    connection.setServiceParameters(serviceParameters);
    connection.setProtectionType(protectionType);
    connection.setDescription(description);

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

  public ConnectionV1Factory setReservation(Reservation reservation) {
    this.reservation = reservation;
    return this;
  }

  public ConnectionV1Factory setDestinationStpId(String destinationStpId) {
    this.destinationStpId = destinationStpId;
    return this;
  }

  public ConnectionV1Factory setSourceStpId(String sourceStpId) {
    this.sourceStpId = sourceStpId;
    return this;
  }

  public ConnectionV1Factory setProtectionType(String protectionType) {
    this.protectionType = protectionType;
    return this;
  }

  public ConnectionV1Factory setConnectionId(String connectionId) {
    this.connectionId = connectionId;
    return this;
  }

  public ConnectionV1Factory setProviderNsa(String providerNsa) {
    this.providerNsa = providerNsa;
    return this;
  }

  public ConnectionV1Factory setRequesterNsa(String requesterNsa) {
    this.requesterNsa = requesterNsa;
    return this;
  }

  public ConnectionV1Factory setDesiredBandwidth(int desiredBandwidth) {
    this.desiredBandwidth = desiredBandwidth;
    return this;
  }

  public ConnectionV1Factory setCurrentState(ConnectionStateType currentState) {
    this.currentState = currentState;
    return this;
  }

  public ConnectionV1Factory withNoIds() {
    this.id = null;
    this.version = null;

    return this;
  }

  public ConnectionV1Factory setId(Long id) {
    this.id = id;
    return this;
  }

  public ConnectionV1Factory setDescription(String description) {
    this.description = description;
    return this;
  }
}
