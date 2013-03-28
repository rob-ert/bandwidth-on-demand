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
package nl.surfnet.bod.nsi.v1sc;

import static nl.surfnet.bod.nsi.v1sc.ConnectionServiceProviderFunctions.CONNECTION_TO_GENERIC_CONFIRMED;
import static nl.surfnet.bod.nsi.v1sc.ConnectionServiceProviderFunctions.NSI_REQUEST_TO_CONNECTION_REQUESTER_PORT;

import javax.annotation.Resource;
import javax.xml.ws.Holder;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.repo.ConnectionRepo;
import oasis.names.tc.saml._2_0.assertion.AttributeStatementType;

import org.ogf.schemas.nsi._2011._10.connection.requester.ConnectionRequesterPort;
import org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;

@Component
public class ConnectionServiceRequesterCallback {

  private final Logger log = LoggerFactory.getLogger(ConnectionServiceRequesterCallback.class);

  @Resource
  private ConnectionRepo connectionRepo;

  public void reserveConfirmed(Connection connection, NsiRequestDetails requestDetails) {
    log.info("Sending a reserveConfirmed on endpoint: {} with id: {}", requestDetails.getReplyTo(), connection
        .getGlobalReservationId());

    connection.setCurrentState(ConnectionStateType.RESERVED);
    connectionRepo.save(connection);

    ReservationInfoType reservationInfoType = new ObjectFactory().createReservationInfoType();
    reservationInfoType.setConnectionId(connection.getConnectionId());
    reservationInfoType.setDescription(connection.getDescription());
    reservationInfoType.setGlobalReservationId(connection.getGlobalReservationId());

    reservationInfoType.setPath(connection.getPath());
    reservationInfoType.setServiceParameters(connection.getServiceParameters());

    ReserveConfirmedType reserveConfirmedType = new ObjectFactory().createReserveConfirmedType();
    reserveConfirmedType.setRequesterNSA(connection.getRequesterNsa());
    reserveConfirmedType.setProviderNSA(connection.getProviderNsa());
    reserveConfirmedType.setReservation(reservationInfoType);

    try {
      ConnectionRequesterPort port = NSI_REQUEST_TO_CONNECTION_REQUESTER_PORT.apply(requestDetails);
      port.reserveConfirmed(new Holder<>(requestDetails.getCorrelationId()), reserveConfirmedType);
    }
    catch (ServiceException e) {
      log.error("Error: ", e);
    }
  }

  public void reserveFailed(final Connection connection, final NsiRequestDetails requestDetails,
      Optional<String> failedReason) {
    log.info("Sending a reserveFailed on endpoint: {} with id: {}", requestDetails.getReplyTo(), connection
        .getGlobalReservationId());

    // skipping the states cleaning and terminating...
    // TODO [AvD] Or do we need to send a Terminate Confirmed?
    connection.setCurrentState(ConnectionStateType.TERMINATED);
    connectionRepo.save(connection);

    ServiceExceptionType serviceException = new ServiceExceptionType();
    serviceException.setErrorId("00600");
    serviceException.setText(failedReason.or("Unknown reason"));
    AttributeStatementType values = new AttributeStatementType();
    serviceException.setVariables(values);

    GenericFailedType reserveFailed = genericFailedForConnection(connection);
    reserveFailed.setServiceException(serviceException);

    try {
      ConnectionRequesterPort port = NSI_REQUEST_TO_CONNECTION_REQUESTER_PORT.apply(requestDetails);
      port.reserveFailed(new Holder<>(requestDetails.getCorrelationId()), reserveFailed);
    }
    catch (ServiceException e) {
      log.error("Error: ", e);
    }
  }

  public void provisionFailedDontUpdateState(Connection connection, NsiRequestDetails requestDetails) {
    log.info("Sending sendProvisionFailed on endpoint: {} with id: {}", requestDetails.getReplyTo(), connection
        .getConnectionId());

    GenericFailedType genericFailed = genericFailedForConnection(connection);

    try {
      ConnectionRequesterPort port = NSI_REQUEST_TO_CONNECTION_REQUESTER_PORT.apply(requestDetails);
      port.provisionFailed(new Holder<>(requestDetails.getCorrelationId()), genericFailed);
    }
    catch (ServiceException e) {
      log.error("Error: ", e);
    }
  }

  public void provisionFailed(Connection connection, NsiRequestDetails requestDetails) {
    connection.setCurrentState(ConnectionStateType.TERMINATED);
    connectionRepo.save(connection);

    provisionFailedDontUpdateState(connection, requestDetails);
  }

  public void provisionConfirmed(final Connection connection, NsiRequestDetails requestDetails) {
    connection.setCurrentState(ConnectionStateType.PROVISIONED);
    connectionRepo.save(connection);

    log.info("Sending provisionConfirmed on endpoint: {} with id: {}", requestDetails.getReplyTo(), connection
        .getConnectionId());

    GenericConfirmedType genericConfirm = CONNECTION_TO_GENERIC_CONFIRMED.apply(connection);

    try {
      ConnectionRequesterPort port = NSI_REQUEST_TO_CONNECTION_REQUESTER_PORT.apply(requestDetails);
      port.provisionConfirmed(new Holder<>(requestDetails.getCorrelationId()), genericConfirm);
    }
    catch (ServiceException e) {
      log.error("Error: ", e);
    }
  }

  public void provisionSucceeded(Connection connection) {
    // no need to inform the requester
    // a provision confirmed is only send when the reservation is running/provisioned
    connection.setCurrentState(ConnectionStateType.AUTO_PROVISION);
    connectionRepo.save(connection);
  }

  public void executionSucceeded(Connection connection) {
    // no need to inform the requester
    connection.setCurrentState(ConnectionStateType.TERMINATED);
    connectionRepo.save(connection);
  }

  public void executionFailed(Connection connection) {
    // no need to inform the requester
    connection.setCurrentState(ConnectionStateType.TERMINATED);
    connectionRepo.save(connection);
  }


  public void scheduleSucceeded(Connection connection) {
    // no need to inform the requester
    connection.setCurrentState(ConnectionStateType.SCHEDULED);
    connectionRepo.save(connection);
  }

  public void terminateConfirmed(Connection connection, Optional<NsiRequestDetails> requestDetails) {
    connection.setCurrentState(ConnectionStateType.TERMINATED);
    connectionRepo.save(connection);

    if (!requestDetails.isPresent()) {
      return;
    }

    GenericConfirmedType genericConfirmed = CONNECTION_TO_GENERIC_CONFIRMED.apply(connection);
    try {
      ConnectionRequesterPort port = NSI_REQUEST_TO_CONNECTION_REQUESTER_PORT.apply(requestDetails.get());
      port.terminateConfirmed(new Holder<>(requestDetails.get().getCorrelationId()), genericConfirmed);
    }
    catch (ServiceException e) {
      log.error("Error: ", e);
    }
  }

  public void terminateFailed(Connection connection, Optional<NsiRequestDetails> requestDetails) {
    if (!requestDetails.isPresent()) {
      return;
    }

    GenericFailedType genericFailed = genericFailedForConnection(connection);

    try {
      ConnectionRequesterPort port = NSI_REQUEST_TO_CONNECTION_REQUESTER_PORT.apply(requestDetails.get());
      port.terminateFailed(new Holder<>(requestDetails.get().getCorrelationId()), genericFailed);
    }
    catch (ServiceException e) {
      log.error("Error: ", e);
    }
  }

  public void terminateTimedOutReservation(Connection connection) {
    // Talked to HansT and this is really the only step we have to take from a NSI perspective.
    connection.setCurrentState(ConnectionStateType.TERMINATED);
    connectionRepo.save(connection);
  }

  public void queryConfirmed(QueryConfirmedType queryResult, NsiRequestDetails requestDetails) {
    ConnectionRequesterPort port = NSI_REQUEST_TO_CONNECTION_REQUESTER_PORT.apply(requestDetails);
    try {
      port.queryConfirmed(new Holder<>(requestDetails.getCorrelationId()), queryResult);
    }
    catch (ServiceException e) {
      log.error("Error: ", e);
    }
  }

  private GenericFailedType genericFailedForConnection(Connection connection) {
    final GenericFailedType generic = new GenericFailedType();
    generic.setProviderNSA(connection.getProviderNsa());
    generic.setRequesterNSA(connection.getRequesterNsa());
    generic.setConnectionId(connection.getConnectionId());
    generic.setGlobalReservationId(connection.getGlobalReservationId());
    generic.setConnectionState(connection.getCurrentState());
    return generic;
  }

}