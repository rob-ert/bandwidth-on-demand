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

import org.ogf.schemas.nsi._2013._04.connection.types.ProvisionStateEnumType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReservationStateEnumType;

import nl.surfnet.bod.domain.ConnectionV2;
import nl.surfnet.bod.domain.Reservation;


public class ConnectionV2Factory {

  private int desiredBandwidth;
  private String requesterNsa = "nsa:requester:surfnet.nl";
  private String providerNsa = "nsa:surfnet.nl";
  private String connectionId = UUID.randomUUID().toString();
  private String sourceStpId = "networkId:source";
  private String destinationStpId = "networkId:dest";
  private Reservation reservation = new ReservationFactory().create();
  private final String globalReservationId = UUID.randomUUID().toString();
  private String protectionType = "PROTECTED";
  private Long id = 0L;
  private Integer version = 0;
  private String description = "";
  private ReservationStateEnumType reservationState;
  private ProvisionStateEnumType provisionState;

  public ConnectionV2 create() {
    ConnectionV2 connection = new ConnectionV2();

    connection.setId(id);
    connection.setVersion(version);
    connection.setDesiredBandwidth(desiredBandwidth);
    connection.setRequesterNsa(requesterNsa);
    connection.setProviderNsa(providerNsa);
    connection.setConnectionId(connectionId);
    connection.setReservationState(reservationState);
    connection.setProvisionState(provisionState);
    connection.setReservation(reservation);
    connection.setGlobalReservationId(globalReservationId);
//    connection.setServiceParameters(serviceParameters);
    connection.setProtectionType(protectionType);
    connection.setDescription(description);


    connection.setSourceStpId(sourceStpId);
    connection.setDestinationStpId(destinationStpId);

    return connection;
  }

  public ConnectionV2Factory setReservation(Reservation reservation) {
    this.reservation = reservation;
    return this;
  }

  public ConnectionV2Factory setDestinationStpId(String networkId, String localId) {
    this.destinationStpId = networkId + ":" + localId;
    return this;
  }

  public ConnectionV2Factory setSourceStpId(String networkId, String localId) {
    this.sourceStpId = networkId + ":" + localId;
    return this;
  }

  public ConnectionV2Factory setProtectionType(String protectionType) {
    this.protectionType = protectionType;
    return this;
  }

  public ConnectionV2Factory setConnectionId(String connectionId) {
    this.connectionId = connectionId;
    return this;
  }

  public ConnectionV2Factory setProviderNsa(String providerNsa) {
    this.providerNsa = providerNsa;
    return this;
  }

  public ConnectionV2Factory setRequesterNsa(String requesterNsa) {
    this.requesterNsa = requesterNsa;
    return this;
  }

  public ConnectionV2Factory setDesiredBandwidth(int desiredBandwidth) {
    this.desiredBandwidth = desiredBandwidth;
    return this;
  }

  public ConnectionV2Factory withNoIds() {
    this.id = null;
    this.version = null;

    return this;
  }

  public ConnectionV2Factory setId(Long id) {
    this.id = id;
    return this;
  }

  public ConnectionV2Factory setDescription(String description) {
    this.description = description;
    return this;
  }

  public ConnectionV2Factory setReservationState(ReservationStateEnumType reservationState) {
    this.reservationState = reservationState;
    return this;
  }

  public ConnectionV2Factory setProvisionState(ProvisionStateEnumType provisionState) {
    this.provisionState = provisionState;
    return this;
  }
}
