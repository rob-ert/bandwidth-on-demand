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

import static org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType.*;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import oasis.names.tc.saml._2_0.assertion.AttributeStatementType;
import oasis.names.tc.saml._2_0.assertion.AttributeType;

import org.joda.time.LocalDateTime;
import org.ogf.schemas.nsi._2011._10.connection._interface.GenericAcknowledgmentType;
import org.ogf.schemas.nsi._2011._10.connection._interface.ProvisionRequestType;
import org.ogf.schemas.nsi._2011._10.connection._interface.QueryRequestType;
import org.ogf.schemas.nsi._2011._10.connection._interface.ReleaseRequestType;
import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;
import org.ogf.schemas.nsi._2011._10.connection._interface.TerminateRequestType;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.requester.ConnectionRequesterPort;
import org.ogf.schemas.nsi._2011._10.connection.requester.ConnectionServiceRequester;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericFailedType;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericRequestType;
import org.ogf.schemas.nsi._2011._10.connection.types.ObjectFactory;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryFailedType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReservationInfoType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReserveConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceExceptionType;
import org.slf4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.base.Function;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.nsi.ws.ConnectionService;

@Service("nsiProvider_v1_sc")
@WebService(serviceName = "ConnectionServiceProvider",
    portName = "ConnectionServiceProviderPort",
    endpointInterface = "org.ogf.schemas.nsi._2011._10.connection.provider.ConnectionProviderPort",
    targetNamespace = "http://schemas.ogf.org/nsi/2011/10/connection/provider")
public class ConnectionServiceProvider extends ConnectionService {

  @Resource(name = "nsaProviderUrns")
  private List<String> nsaProviderUrns;

  private final Logger log = getLog();

  public static final String BOD_URN_POSTFIX = "urn:nl:surfnet:diensten:bod:";

  private final ConnectionRequesterPort connectionServiceRequesterPort = getConnectionRequesterPort();

  private final Function<Connection, Reservation> TO_RESERVATION = //
  new Function<Connection, Reservation>() {
    @Override
    public Reservation apply(final Connection connection) {

      final Reservation reservation = new Reservation();

      reservation.setBandwidth(connection.getDesiredBandwidth());
      reservation.setEndDateTime(new LocalDateTime(connection.getEndTime()));
      reservation.setStartDateTime(new LocalDateTime(connection.getStartTime()));
      reservation.setUserCreated(connection.getRequesterNsa());

      final String sourceStpId = connection.getPath().getSourceSTP().getStpId();
      final String destinationStpId = connection.getPath().getDestSTP().getStpId();

      final VirtualPort sourcePort = getVirtualPortRepo().findByManagerLabel(sourceStpId);
      final VirtualPort destinationPort = getVirtualPortRepo().findByManagerLabel(destinationStpId);

      reservation.setSourcePort(sourcePort);
      reservation.setDestinationPort(destinationPort);
      reservation.setVirtualResourceGroup(sourcePort.getVirtualResourceGroup());
      reservation.setName(connection.getGlobalReservationId());

      return reservation;
    }
  };

  private static final Function<ReserveRequestType, Connection> TO_CONNECTION = //
  new Function<ReserveRequestType, Connection>() {
    @Override
    public Connection apply(ReserveRequestType reserveRequestType) {

      final Connection connection = new Connection();

      connection.setConnectionId(reserveRequestType.getReserve().getReservation().getConnectionId());
      connection.setCurrentState(INITIAL);
      connection.setDescription(null);

      connection.setDesiredBandwidth(reserveRequestType.getReserve().getReservation().getServiceParameters()
          .getBandwidth().getDesired());
      connection.setEndTime(reserveRequestType.getReserve().getReservation().getServiceParameters().getSchedule()
          .getEndTime().toGregorianCalendar().getTime());

      connection.setGlobalReservationId(reserveRequestType.getReserve().getReservation().getGlobalReservationId());
      connection.setMaximumBandwidth(reserveRequestType.getReserve().getReservation().getServiceParameters()
          .getBandwidth().getMaximum());
      connection.setMinimumBandwidth(reserveRequestType.getReserve().getReservation().getServiceParameters()
          .getBandwidth().getMinimum());

      connection.setPath(reserveRequestType.getReserve().getReservation().getPath());
      connection.setProviderNsa(reserveRequestType.getReserve().getProviderNSA());

      connection.setReplyTo(reserveRequestType.getReplyTo());
      connection.setRequesterNsa(reserveRequestType.getReserve().getRequesterNSA());
      connection.setReservationId(null);
      connection.setServiceParameters(reserveRequestType.getReserve().getReservation().getServiceParameters());

      connection.setStartTime(reserveRequestType.getReserve().getReservation().getServiceParameters().getSchedule()
          .getStartTime().toGregorianCalendar().getTime());

      connection.setDescription(reserveRequestType.getReserve().getReservation().getDescription());

      return connection;
    }
  };

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

  private void sendReservationFailed(final Connection connection) {
    log.info("Calling reserveFailed on endpoint: {} with id: {}", connection.getReplyTo(),
        connection.getGlobalReservationId());

    final ConnectionServiceRequester requester = new ConnectionServiceRequester();
    final ConnectionRequesterPort connectionServiceRequesterPort = requester.getConnectionServiceRequesterPort();
    final GenericFailedType reservationFailed = new GenericFailedType();
    reservationFailed.setRequesterNSA(connection.getRequesterNsa());
    reservationFailed.setProviderNSA(connection.getProviderNsa());
    reservationFailed.setGlobalReservationId(connection.getGlobalReservationId());
    reservationFailed.setConnectionId(connection.getConnectionId());
    reservationFailed.setConnectionState(connection.getCurrentState());

    // FIXME: What to put into the service exception
    final ServiceExceptionType serviceException = new ServiceExceptionType();
    serviceException.setErrorId("ERROR_ID");
    serviceException.setText("Some text");
    AttributeStatementType values = new AttributeStatementType();
    serviceException.setVariables(values);
    reservationFailed.setServiceException(serviceException);

    try {
      prepareRequestContext(connection, connectionServiceRequesterPort);
      connectionServiceRequesterPort.reserveFailed(new Holder<String>(connection.getConnectionId()), reservationFailed);
    }
    catch (org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException e) {
      log.error("Error: ", e);
    }
  }

  private void sendReserveConfirmed(final Connection connection) {
    log.debug("Calling sendReserveConfirmed on endpoint: {} with id: {}", connection.getReplyTo(),
        connection.getGlobalReservationId());

    final ReserveConfirmedType reserveConfirmedType = new ObjectFactory().createReserveConfirmedType();
    reserveConfirmedType.setRequesterNSA(connection.getRequesterNsa());
    reserveConfirmedType.setProviderNSA(connection.getProviderNsa());

    final ReservationInfoType reservationInfoType = new ObjectFactory().createReservationInfoType();
    reservationInfoType.setConnectionId(connection.getConnectionId());
    reservationInfoType.setDescription(connection.getDescription());
    reservationInfoType.setGlobalReservationId(connection.getGlobalReservationId());
    reservationInfoType.setPath(connection.getPath());
    reservationInfoType.setServiceParameters(connection.getServiceParameters());

    reserveConfirmedType.setReservation(reservationInfoType);

    try {
      prepareRequestContext(connection, connectionServiceRequesterPort);
      connectionServiceRequesterPort.reserveConfirmed(new Holder<String>(connection.getConnectionId()),
          reserveConfirmedType);
    }
    catch (org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException e) {
      log.error("Error: ", e);
    }
  }

  /**
   * @return
   */
  private ConnectionRequesterPort getConnectionRequesterPort() {
    URL url;
    try {
      url = new ClassPathResource("/wsdl/nsi/ogf_nsi_connection_requester_v1_0.wsdl").getURL();
    }
    catch (IOException e) {
      log.error("Error: ", e);
      return null;
    }

    final ConnectionServiceRequester requester = new ConnectionServiceRequester(url, new QName(
        "http://schemas.ogf.org/nsi/2011/10/connection/requester", "ConnectionServiceRequester"));

    return requester.getConnectionServiceRequesterPort();
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

    Connection connection = TO_CONNECTION.apply(reservationRequest);

    if (!StringUtils.hasText(connection.getGlobalReservationId())) {
      connection.setGlobalReservationId(generateGlobalId());
    }

    connection.setCurrentState(INITIAL);
    connection = getConnectionRepo().save(connection);

    log.debug("Received reservation request with id: {}", connection.getConnectionId());

    if (!isValidId(connection.getConnectionId())) {
      connection = getConnectionRepo().findOne(connection.getId());
      connection.setCurrentState(CLEANING);
      connection = getConnectionRepo().save(connection);
      throw new ServiceException("SVC0001", getInvalidParameterServiceException("connectionId"));
    }

    // TODO
    // Validate CorrelationId

    connection = getConnectionRepo().findOne(connection.getId());
    connection.setCurrentState(RESERVING);
    connection = getConnectionRepo().save(connection);

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
      connection = getConnectionRepo().findOne(connection.getId());
      connection.setCurrentState(CLEANING);
      connection = getConnectionRepo().save(connection);
      throw new ServiceException("SVC0001", getInvalidParameterServiceException("providerNSA"));
    }

    /*
     * Get the connectionId from the reservation as we will use this to
     * serialize related requests.
     */

    // Route this message to the appropriate actor for processing.

    // for now always fail the reservation
    // forceFailed(connection);

    createReservation(connection, false);
    getConnectionRepo().save(connection);

    /*
     * We successfully sent the message for processing so acknowledge it back to
     * the requesting NSA. We hope this returns before the confirmation makes it
     * back to the requesting NSA.
     */
    final GenericAcknowledgmentType genericAcknowledgment = new GenericAcknowledgmentType();
    genericAcknowledgment.setCorrelationId(reservationRequest.getCorrelationId());

    log.debug("Returning GenericAcknowledgmentType with id: {}", genericAcknowledgment.getCorrelationId());
    return genericAcknowledgment;
  }

  private String generateGlobalId() {
    return BOD_URN_POSTFIX + UUID.randomUUID();
  }

  private void createReservation(final Connection connection, boolean autoProvision) {
    // transform connection to reservation
    final Reservation reservation = TO_RESERVATION.apply(connection);

    // create BoD reservation
    String reservationId = getReservationService().create(reservation, autoProvision);
    connection.setReservationId(reservationId);
    getConnectionRepo().save(connection);

    // call reserveConfirmed on requester nsa
    sendReserveConfirmed(connection);

    //

  }

  public GenericAcknowledgmentType provision(ProvisionRequestType parameters) throws ServiceException {

    final String connectionId = parameters.getProvision().getConnectionId();
    log.debug("Received provision request with id: {}", connectionId);

    final Connection connection = getConnectionRepo().findByConnectionId(connectionId);

    final Reservation reservation = getReservationService().findByReservationId(connection.getReservationId());
    final boolean isActivated = getReservationService().activate(reservation);

    System.out.println(isActivated);

    // / call provisionConfirmed on requester nsa
    if (isActivated) {
      sendProvisionConfirmed(connection);
    }
    else {
      sendProvisionFailed(connection);
    }

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
    ack.setCorrelationId(parameters.getCorrelationId());
    return ack;
  }

  private void sendProvisionFailed(Connection connection) {
    log.debug("Calling sendReserveConfirmed on endpoint: {} with id: {}", connection.getReplyTo(),
        connection.getGlobalReservationId());

    final GenericFailedType generic = new GenericFailedType();

    generic.setProviderNSA(connection.getProviderNsa());
    generic.setRequesterNSA(connection.getRequesterNsa());
    generic.setConnectionId(connection.getConnectionId());
    generic.setGlobalReservationId(connection.getGlobalReservationId());

    try {
      prepareRequestContext(connection, connectionServiceRequesterPort);
      connectionServiceRequesterPort.provisionFailed(new Holder<String>(connection.getConnectionId()), generic);
    }
    catch (org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException e) {
      log.error("Error: ", e);
    }

  }

  /**
   * @param connection
   * @param connectionServiceRequesterPort
   */
  private void prepareRequestContext(Connection connection, final ConnectionRequesterPort connectionServiceRequesterPort) {
    final Map<String, Object> requestContext = ((BindingProvider) connectionServiceRequesterPort).getRequestContext();

    // TODO: get credentials from reservation request
    requestContext.put(BindingProvider.USERNAME_PROPERTY, "nsi");
    requestContext.put(BindingProvider.PASSWORD_PROPERTY, "nsi123");
    requestContext.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

    requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, connection.getReplyTo());
  }

  private void sendProvisionConfirmed(final Connection connection) {

    log.debug("Calling sendReserveConfirmed on endpoint: {} with id: {}", connection.getReplyTo(),
        connection.getGlobalReservationId());

    final GenericConfirmedType generic = new GenericConfirmedType();
    generic.setProviderNSA(connection.getProviderNsa());
    generic.setRequesterNSA(connection.getRequesterNsa());
    generic.setConnectionId(connection.getConnectionId());
    generic.setGlobalReservationId(connection.getGlobalReservationId());

    try {
      prepareRequestContext(connection, connectionServiceRequesterPort);
      connectionServiceRequesterPort.provisionConfirmed(new Holder<String>(connection.getConnectionId()), generic);
    }
    catch (org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException e) {
      log.error("Error: ", e);
    }

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

}
