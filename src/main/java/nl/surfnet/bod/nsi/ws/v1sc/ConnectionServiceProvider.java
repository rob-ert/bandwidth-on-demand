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
package nl.surfnet.bod.nsi.ws.v1sc;

import static org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType.CLEANING;
import static org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType.INITIAL;
import static org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType.RESERVING;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import nl.surfnet.bod.nsi.StateMachine;
import nl.surfnet.bod.nsi.ws.ConnectionService;
import oasis.names.tc.saml._2_0.assertion.AttributeStatementType;
import oasis.names.tc.saml._2_0.assertion.AttributeType;

import org.ogf.schemas.nsi._2011._10.connection._interface.*;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.requester.ConnectionRequesterPort;
import org.ogf.schemas.nsi._2011._10.connection.requester.ConnectionServiceRequester;
import org.ogf.schemas.nsi._2011._10.connection.types.*;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;

@Service("nsiProvider_v1_sc")
@WebService(
  serviceName = "ConnectionServiceProvider",
  portName = "ConnectionServiceProviderPort",
  endpointInterface = "org.ogf.schemas.nsi._2011._10.connection.provider.ConnectionProviderPort",
  targetNamespace = "http://schemas.ogf.org/nsi/2011/10/connection/provider",
  wsdlLocation = "/WEB-INF/wsdl/nsi/ogf_nsi_connection_provider_v1_0.wsdl")
public class ConnectionServiceProvider extends ConnectionService {

  private final Logger log = getLog();

  private Long delayBeforeResponseSend = 2000L;

  @Resource(name = "nsaProviderUrns")
  private List<String> nsaProviderUrns;

  @Resource(name = "simpelStateMachine")
  private StateMachine stateMachine;

  private List<String> nsaChildren = ImmutableList.of(
      "urn:ogf:network:nsa:child1",
      "urn:ogf:network:nsa:child2",
      "urn:ogf:network:nsa:child3");

  private ServiceExceptionType getInvalidParameterServiceException(final String attributeName) {
    final ServiceExceptionType serviceException = new ServiceExceptionType();
    serviceException.setErrorId("SVC0001");
    serviceException.setText("Invalid or missing parameter");

    final AttributeType attribute = new AttributeType();
    attribute.setName(attributeName);
    attribute.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:basic");

    final AttributeStatementType attributeStatement = new AttributeStatementType();
    attributeStatement.getAttributeOrEncryptedAttribute().add(attribute);

    serviceException.setVariables(attributeStatement);
    return serviceException;
  }

  private void sendReservationFailed(final String correlationId, final String nsaRequester) {
    log.info("Calling reserveFailed on endpoint: {} with id: {}", nsaRequester, correlationId);

    final ConnectionServiceRequester requester = new ConnectionServiceRequester();
    final ConnectionRequesterPort connectionServiceRequesterPort = requester.getConnectionServiceRequesterPort();
    final GenericFailedType reservationFailed = new GenericFailedType();
    reservationFailed.setRequesterNSA(nsaRequester);

    try {
      final Map<String, Object> requestContext = ((BindingProvider) connectionServiceRequesterPort).getRequestContext();

      // TODO: get credentials from reservation request
      requestContext.put(BindingProvider.USERNAME_PROPERTY, "nsi");
      requestContext.put(BindingProvider.PASSWORD_PROPERTY, "nsi123");
      requestContext.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

      requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, nsaRequester);
      connectionServiceRequesterPort.reserveFailed(new Holder<String>(correlationId), reservationFailed);
    }
    catch (org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException e) {
      log.error("Error: ", e);
    }
  }

  private void sendTerminateToChildNsa(final String correlationId, final String nsaChild, final String nsaRequester) {

    final GenericRequestType genericRequestType = new GenericRequestType();
    genericRequestType.setConnectionId(correlationId);
    genericRequestType.setProviderNSA(nsaChild);
    genericRequestType.setRequesterNSA(nsaRequester);
    // TODO: Get security attribute
    genericRequestType.setSessionSecurityAttr(null);

    final TerminateRequestType terminator = new TerminateRequestType();
    terminator.setCorrelationId(correlationId);
    terminator.setReplyTo(nsaRequester);
    terminator.setTerminate(genericRequestType);
    log.debug("Sendign terminate event to child nsa: {} with id: {}", nsaChild, correlationId);
  }

  private void sendTerminatToNrm(final String correlationId) {
    log.debug("Sendig terminate event to NRM.");
  }

  private boolean isValidProviderNsa(final ReserveRequestType reserveRequest) {
    return nsaProviderUrns == null ? true : nsaProviderUrns.contains(reserveRequest.getReserve().getProviderNSA());
  }

  /**
   * The reservation method processes an NSI reservation request for
   * inter-domain bandwidth. Those parameters required for the request to
   * proceed to a processing actor will be validated, however, all other
   * parameters will be validated in the processing actor.
   *
   * @param parameters
   *          The un-marshaled JAXB object holding the NSI reservation request.
   * @return The GenericAcknowledgmentType object returning the correlationId
   *         sent in the reservation request. We are acknowledging that we have
   *         received the request.
   * @throws ServiceException
   *           if we can determine there is processing error before digging into
   *           the request.
   */
  public GenericAcknowledgmentType reserve(final ReserveRequestType reservationRequest) throws ServiceException {

    if (reservationRequest == null) {
      throw new ServiceException("Invalid reservationRequest received (null)", null);
    }

    final String correlationId = reservationRequest.getCorrelationId();
    log.debug("Received reservation request with id: {}", reservationRequest.getCorrelationId());
    stateMachine.inserOrUpdateState(correlationId, INITIAL);

//    final ReservationInfoType reservation = reservationRequest.getReserve().getReservation();
    if (!isValidCorrelationId(correlationId)) {
      stateMachine.inserOrUpdateState(reservationRequest.getCorrelationId(), CLEANING);
      throw new ServiceException("SVC0001", getInvalidParameterServiceException("correlationId"));
    }

    stateMachine.inserOrUpdateState(correlationId, RESERVING);

    // Build an internal request for this reservation request.

    /*
     * Break out the attributes we need for handling. correlationId is needed
     * for any acknowledgment, confirmation, or failed message.
     */

    /*
     * We will send the confirmation, or failed message back to this location.
     * In the future we may remove this parameter and add a csRequesterEndpoint
     * field to NSA topology.
     */
    final String nsaRequester = reservationRequest.getReplyTo();
    // log.debug("Requester endpoint: {}", requesterEndpoint);

    /*
     * Save the calling NSA security context and pass it along for use during
     * processing of request (when implemented).
     */

    /*
     * Extract the reservation information for use by the actor processing
     * logic.
     */

    /*
     * Extract NSA fields.
     */

    /*
     * Verify that this message was targeting this NSA by looking at the
     * ProviderNSA field. If invalid we will throw an exception.
     */
    if (!isValidProviderNsa(reservationRequest)) {
      stateMachine.inserOrUpdateState(reservationRequest.getCorrelationId(), CLEANING);
      throw new ServiceException("SVC0001", getInvalidParameterServiceException("providerNSA"));
    }

    /*
     * Get the connectionId from the reservation as we will use this to
     * serialize related requests.
     */
//    final String connectionId = reservation.getConnectionId();
    // log.debug("connectionId {}", connectionId);

    // Route this message to the appropriate actor for processing.

    // for now always fail the reservation
    forceFailed(correlationId, nsaRequester);

    /*
     * We successfully sent the message for processing so acknowledge it back to
     * the requesting NSA. We hope this returns before the confirmation makes it
     * back to the requesting NSA.
     */
    final GenericAcknowledgmentType genericAcknowledgment = new GenericAcknowledgmentType();
    genericAcknowledgment.setCorrelationId(correlationId);

    log.info("Returning GenericAcknowledgmentType with id: {}", genericAcknowledgment.getCorrelationId());
    return genericAcknowledgment;
  }

  /**
   * @param correlationId
   * @param nsaRequester
   */
  private void forceFailed(final String correlationId, final String nsaRequester) {
    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        sendReservationFailed(correlationId, nsaRequester);
        stateMachine.inserOrUpdateState(correlationId, CLEANING);
        for (final String nsaChild : nsaChildren) {
          sendTerminateToChildNsa(correlationId, nsaChild, nsaRequester);
        }
        sendTerminatToNrm(correlationId);
        stateMachine.deleteState(correlationId);
      }
    }, delayBeforeResponseSend);
  }

  public GenericAcknowledgmentType provision(ProvisionRequestType parameters) throws ServiceException {

    // Build an internal request for this reservation request.

    /*
     * Break out the attributes we need for handling. correlationId is needed
     * for any acknowledgment, confirmation, or failed message.
     */

    /*
     * We will send the confirmation, or failed message back to this location.
     */

    /*
     * Save the calling NSA security context and pass it along for use during
     * processing of request.
     */

    // Extract the reservation information.

    // Extract NSA fields.

    /*
     * Verify that this message was targeting this NSA by looking at the
     * ProviderNSA field. If invalid we will throw an exception.
     */

    /*
     * Get the connectionId from the reservation as we will use this to
     * serialize related requests.
     */

    // Route this message to the appropriate actor for processing.

    /*
     * We successfully sent the message for processing so acknowledge it back to
     * the sending.
     */
    GenericAcknowledgmentType ack = new GenericAcknowledgmentType();
    ack.setCorrelationId(null);
    return ack;
  }

  public GenericAcknowledgmentType release(ReleaseRequestType parameters) throws ServiceException {

    // Build an internal request for this reservation request.

    /*
     * Break out the attributes we need for handling. correlationId is needed
     * for any acknowledgment, confirmation, or failed message.
     */

    /*
     * We will send the confirmation, or failed message back to this location.
     */

    /*
     * Save the calling NSA security context and pass it along for use during
     * processing of request.
     */

    // Extract the reservation information.

    // Extract NSA fields.

    /*
     * Verify that this message was targeting this NSA by looking at the
     * ProviderNSA field. If invalid we will throw an exception.
     */

    /**
     * Get the connectionId from the reservation as we will use this to
     * serialize related requests.
     */

    // Route this message to the appropriate actor for processing.

    /*
     * We successfully sent the message for processing so acknowledge it back to
     * the sending.
     */
    GenericAcknowledgmentType ack = new GenericAcknowledgmentType();
    ack.setCorrelationId(null);
    return ack;
  }

  public GenericAcknowledgmentType terminate(TerminateRequestType parameters) throws ServiceException {

    // Build an internal request for this reservation request.

    /*
     * Break out the attributes we need for handling. correlationId is needed
     * for any acknowledgment, confirmation, or failed message.
     */

    /*
     * We will send the confirmation, or failed message back to this location.
     */

    /*
     * Save the calling NSA security context and pass it along for use during
     * processing of request.
     */

    // Extract the reservation information.

    // Extract NSA fields.

    /*
     * Verify that this message was targeting this NSA by looking at the
     * ProviderNSA field. If invalid we will throw an exception.
     */

    /*
     * Get the connectionId from the reservation as we will use this to
     * serialize related requests.
     */

    // Route this message to the appropriate actor for processing.

    /*
     * We successfully sent the message for processing so acknowledge it back to
     * the sending.
     */
    GenericAcknowledgmentType ack = new GenericAcknowledgmentType();
    ack.setCorrelationId(null);
    return ack;
  }

  public GenericAcknowledgmentType query(QueryRequestType parameters) throws ServiceException {

    // Build an internal request for this reservation request.

    /*
     * Break out the attributes we need for handling. correlationId is needed
     * for any acknowledgment, confirmation, or failed message.
     */

    /*
     * We will send the confirmation, or failed message back to this location.
     */

    /*
     * Save the calling NSA security context and pass it along for use during
     * processing of request.
     */

    // Extract the query information.

    // We want to route to operation specific provider.

    // Extract NSA fields.

    /*
     * Verify that this message was targeting this NSA by looking at the
     * ProviderNSA field. If invalid we will throw an exception.
     */

    // Route this message to the appropriate actor for processing.

    /*
     * We successfully sent the message for processing so acknowledge it back to
     * the sending.
     */
    GenericAcknowledgmentType ack = new GenericAcknowledgmentType();
    ack.setCorrelationId(null);
    return ack;
  }

  public void queryConfirmed(Holder<String> correlationId, QueryConfirmedType queryConfirmed) throws ServiceException {
    throw new UnsupportedOperationException("Not implemented yet.");
  }

  public void queryFailed(Holder<String> correlationId, QueryFailedType queryFailed) throws ServiceException {
    throw new UnsupportedOperationException("Not implemented yet.");
  }

  void setDelayBeforeResponseSend(long delayInMilis) {
    this.delayBeforeResponseSend = delayInMilis;
  }

}
