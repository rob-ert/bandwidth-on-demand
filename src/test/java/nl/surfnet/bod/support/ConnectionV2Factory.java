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
package nl.surfnet.bod.support;

import java.util.UUID;

import com.google.common.base.Optional;

import nl.surfnet.bod.domain.ConnectionV2;
import nl.surfnet.bod.domain.NsiV2RequestDetails;
import nl.surfnet.bod.domain.ProtectionType;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.nsi.v2.ConnectionsV2;
import nl.surfnet.bod.util.XmlUtils;

import org.joda.time.DateTime;
import org.ogf.schemas.nsi._2013._07.connection.types.LifecycleStateEnumType;
import org.ogf.schemas.nsi._2013._07.connection.types.ProvisionStateEnumType;
import org.ogf.schemas.nsi._2013._07.connection.types.ReservationConfirmCriteriaType;
import org.ogf.schemas.nsi._2013._07.connection.types.ReservationStateEnumType;
import org.ogf.schemas.nsi._2013._07.connection.types.ScheduleType;
import org.ogf.schemas.nsi._2013._07.services.point2point.P2PServiceBaseType;
import org.ogf.schemas.nsi._2013._07.services.types.StpType;


public class ConnectionV2Factory {

  private String requesterNsa = "nsa:requester:surfnet.nl";
  private String providerNsa = "nsa:surfnet.nl";
  private String connectionId = UUID.randomUUID().toString();
  private Reservation reservation = new ReservationFactory().create();
  private String globalReservationId = UUID.randomUUID().toString();
  private ProtectionType protectionType = ProtectionType.PROTECTED;
  private Long id = 0L;
  private String description = "";
  private P2PServiceBaseType path = new P2PServiceBaseType().withSourceSTP(new StpType().withNetworkId("networkId").withLocalId("source")).withDestSTP(new StpType().withNetworkId("networkId").withLocalId("dest"));
  private ReservationStateEnumType reservationState = ReservationStateEnumType.RESERVE_START;
  private ProvisionStateEnumType provisionState;
  private LifecycleStateEnumType lifecycleState;
  private boolean dataPlaneActive;
  private int reserveVersion = 0;
  private Optional<Integer> committedVersion = Optional.absent();
  private NsiV2RequestDetails initialReserveRequestDetails = new NsiV2RequestDetailsFactory().create();
  private int reserveHeldTimeoutValue = 1200;
  private Optional<DateTime> reserveHeldTimeout = Optional.absent();

  public ConnectionV2 create() {
    ConnectionV2 connection = new ConnectionV2();

    connection.setId(id);
    connection.setDesiredBandwidth(path.getCapacity());
    connection.setRequesterNsa(requesterNsa);
    connection.setProviderNsa(providerNsa);
    connection.setConnectionId(connectionId);
    connection.setReservation(reservation);
    connection.setGlobalReservationId(globalReservationId);
    connection.setProtectionType(protectionType);
    connection.setDescription(description);
    ReservationConfirmCriteriaType criteria = new ReservationConfirmCriteriaType()
        .withVersion(reserveVersion)
        .withSchedule(new ScheduleType()
            .withStartTime(XmlUtils.toGregorianCalendar(reservation.getStartDateTime()))
            .withEndTime(XmlUtils.toGregorianCalendar(reservation.getEndDateTime())));
    ConnectionsV2.addPointToPointService(criteria.getAny(), path);
    connection.setCriteria(criteria);

    connection.setReservationState(reservationState);
    connection.setProvisionState(provisionState);
    connection.setDataPlaneActive(dataPlaneActive);
    connection.setLifecycleState(lifecycleState);

    connection.setReserveVersion(reserveVersion);
    connection.setCommittedVersion(committedVersion);

    connection.setInitialReserveRequestDetails(initialReserveRequestDetails);
    connection.setLastReservationRequestDetails(initialReserveRequestDetails);

    connection.setReserveHeldTimeoutValue(reserveHeldTimeoutValue);
    connection.setReserveHeldTimeout(reserveHeldTimeout);

    reservation.setConnectionV2(connection);

    return connection;
  }

  public ConnectionV2Factory setReservation(Reservation reservation) {
    this.reservation = reservation;
    return this;
  }

  public ConnectionV2Factory setDestinationStpId(String networkId, String localId) {
    this.path.getDestSTP().withNetworkId(networkId).withLocalId(localId);
    return this;
  }

  public ConnectionV2Factory setSourceStpId(String networkId, String localId) {
    this.path.getSourceSTP().withNetworkId(networkId).withLocalId(localId);
    return this;
  }

  public ConnectionV2Factory setProtectionType(ProtectionType protectionType) {
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
    this.path.setCapacity(desiredBandwidth);
    return this;
  }

  public ConnectionV2Factory withNoIds() {
    this.id = null;
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

  public ConnectionV2Factory setDataPlaneActive(boolean dataPlaneActive) {
    this.dataPlaneActive = dataPlaneActive;
    return this;
  }

  public ConnectionV2Factory setLifecycleState(LifecycleStateEnumType lifecycleState) {
    this.lifecycleState = lifecycleState;
    return this;
  }

  public ConnectionV2Factory setReserveVersion(int reserveVersion) {
    this.reserveVersion = reserveVersion;
    return this;
  }

  public ConnectionV2Factory setReserveHeldTimeoutValue(int reserveHeldTimeoutValue) {
    this.reserveHeldTimeoutValue = reserveHeldTimeoutValue;
    return this;
  }

  public ConnectionV2Factory setGlobalReservationId(String globalReservationId) {
    this.globalReservationId = globalReservationId;
    return this;
  }

  public ConnectionV2Factory committed(int committedVersion) {
    this.reservationState = ReservationStateEnumType.RESERVE_START;
    this.provisionState = ProvisionStateEnumType.RELEASED;
    this.lifecycleState = LifecycleStateEnumType.CREATED;
    this.reserveVersion = committedVersion;
    this.committedVersion  = Optional.of(committedVersion);
    return this;
  }
}
