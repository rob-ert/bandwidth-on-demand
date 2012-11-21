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
package nl.surfnet.bod.nsi.v1sc;

import static nl.surfnet.bod.nsi.v1sc.ConnectionServiceProviderFunctions.CONNECTION_TO_GENERIC_CONFIRMED;
import static nl.surfnet.bod.nsi.v1sc.ConnectionServiceProviderFunctions.CONNECTION_TO_GENERIC_FAILED;
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

    final ReservationInfoType reservationInfoType = new ObjectFactory().createReservationInfoType();
    reservationInfoType.setConnectionId(connection.getConnectionId());
    reservationInfoType.setDescription(connection.getDescription());
    reservationInfoType.setGlobalReservationId(connection.getGlobalReservationId());

    reservationInfoType.setPath(connection.getPath());
    reservationInfoType.setServiceParameters(connection.getServiceParameters());

    final ReserveConfirmedType reserveConfirmedType = new ObjectFactory().createReserveConfirmedType();
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

    final ServiceExceptionType serviceException = new ServiceExceptionType();
    serviceException.setErrorId("00600");
    serviceException.setText(failedReason.or("Unknown reason"));
    AttributeStatementType values = new AttributeStatementType();
    serviceException.setVariables(values);

    GenericFailedType reservationFailed = CONNECTION_TO_GENERIC_FAILED.apply(connection);
    reservationFailed.setServiceException(serviceException);

    try {
      final ConnectionRequesterPort port = NSI_REQUEST_TO_CONNECTION_REQUESTER_PORT.apply(requestDetails);
      port.reserveFailed(new Holder<>(requestDetails.getCorrelationId()), reservationFailed);
    }
    catch (ServiceException e) {
      log.error("Error: ", e);
    }
  }

  public void provisionFailed(Connection connection, NsiRequestDetails requestDetails) {
    if (connection.getStartTime().isPresent() && (connection.getStartTime().get().isAfterNow())) {
      connection.setCurrentState(ConnectionStateType.SCHEDULED);
    }
    else {
      connection.setCurrentState(ConnectionStateType.RESERVED);
    }
    connectionRepo.save(connection);

    log.info("Sending sendProvisionFailed on endpoint: {} with id: {}", requestDetails.getReplyTo(), connection
        .getConnectionId());

    final GenericFailedType generic = new GenericFailedType();
    generic.setProviderNSA(connection.getProviderNsa());
    generic.setRequesterNSA(connection.getRequesterNsa());
    generic.setConnectionId(connection.getConnectionId());
    generic.setGlobalReservationId(connection.getGlobalReservationId());

    try {
      final ConnectionRequesterPort port = NSI_REQUEST_TO_CONNECTION_REQUESTER_PORT.apply(requestDetails);
      port.provisionFailed(new Holder<>(requestDetails.getCorrelationId()), generic);
    }
    catch (ServiceException e) {
      log.error("Error: ", e);
    }

  }

  public void provisionConfirmed(final Connection connection, NsiRequestDetails requestDetails) {
    connection.setCurrentState(ConnectionStateType.PROVISIONED);
    connectionRepo.save(connection);

    log.info("Sending provisionConfirmed on endpoint: {} with id: {}", requestDetails.getReplyTo(), connection
        .getConnectionId());

    final GenericConfirmedType genericConfirm = CONNECTION_TO_GENERIC_CONFIRMED.apply(connection);

    try {
      final ConnectionRequesterPort port = NSI_REQUEST_TO_CONNECTION_REQUESTER_PORT.apply(requestDetails);
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

    final GenericConfirmedType genericConfirmed = CONNECTION_TO_GENERIC_CONFIRMED.apply(connection);
    try {
      final ConnectionRequesterPort port = NSI_REQUEST_TO_CONNECTION_REQUESTER_PORT.apply(requestDetails.get());
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

    final GenericFailedType genericFailed = CONNECTION_TO_GENERIC_FAILED.apply(connection);

    try {
      final ConnectionRequesterPort port = NSI_REQUEST_TO_CONNECTION_REQUESTER_PORT.apply(requestDetails.get());
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
    final ConnectionRequesterPort port = NSI_REQUEST_TO_CONNECTION_REQUESTER_PORT.apply(requestDetails);
    try {
      port.queryConfirmed(new Holder<>(requestDetails.getCorrelationId()), queryResult);
    }
    catch (ServiceException e) {
      log.error("Error: ", e);
    }
  }

  public void queryFailed() {

  }

}
