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

import static com.google.common.base.Preconditions.checkNotNull;
import static org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType.CLEANING;
import static org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType.INITIAL;
import static org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType.RESERVING;

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
import javax.xml.ws.WebServiceContext;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.nsi.ws.NsiProvider;
import nl.surfnet.bod.repo.ConnectionRepo;
import nl.surfnet.bod.repo.VirtualPortRepo;
import nl.surfnet.bod.repo.VirtualResourceGroupRepo;
import nl.surfnet.bod.service.ReservationService;
import oasis.names.tc.saml._2_0.assertion.AttributeStatementType;
import oasis.names.tc.saml._2_0.assertion.AttributeType;

import org.joda.time.LocalDateTime;
import org.ogf.schemas.nsi._2011._10.connection._interface.*;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.requester.ConnectionRequesterPort;
import org.ogf.schemas.nsi._2011._10.connection.requester.ConnectionServiceRequester;
import org.ogf.schemas.nsi._2011._10.connection.types.*;
import org.ogf.schemas.nsi._2011._10.connection.types.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.base.Function;

@Service("nsiProvider_v1_sc")
@WebService(serviceName = "ConnectionServiceProvider",
    portName = "ConnectionServiceProviderPort",
    endpointInterface = "org.ogf.schemas.nsi._2011._10.connection.provider.ConnectionProviderPort",
    targetNamespace = "http://schemas.ogf.org/nsi/2011/10/connection/provider")
public class ConnectionServiceProvider implements NsiProvider {

  public static final String BOD_URN_POSTFIX = "urn:nl:surfnet:diensten:bod:";

  private static final String URN_UUID = "urn:uuid:";

  static {
    System.setProperty("com.sun.xml.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace", "false");
  }

  private static final Function<ReserveRequestType, Connection> TO_CONNECTION =
    new Function<ReserveRequestType, Connection>() {
      @Override
      public Connection apply(ReserveRequestType reserveRequestType) {

        final Connection connection = new Connection();

        connection.setConnectionId(reserveRequestType.getReserve().getReservation().getConnectionId());
        connection.setCurrentState(INITIAL);
        connection.setDescription(reserveRequestType.getReserve().getReservation().getDescription());

        connection.setStartTime(reserveRequestType.getReserve().getReservation().getServiceParameters().getSchedule()
            .getStartTime().toGregorianCalendar().getTime());
        connection.setEndTime(reserveRequestType.getReserve().getReservation().getServiceParameters().getSchedule()
            .getEndTime().toGregorianCalendar().getTime());

        connection.setGlobalReservationId(reserveRequestType.getReserve().getReservation().getGlobalReservationId());

        connection.setDesiredBandwidth(reserveRequestType.getReserve().getReservation().getServiceParameters()
            .getBandwidth().getDesired());
        connection.setMaximumBandwidth(reserveRequestType.getReserve().getReservation().getServiceParameters()
            .getBandwidth().getMaximum());
        connection.setMinimumBandwidth(reserveRequestType.getReserve().getReservation().getServiceParameters()
            .getBandwidth().getMinimum());

        connection.setPath(reserveRequestType.getReserve().getReservation().getPath());
        connection.setProviderNsa(reserveRequestType.getReserve().getProviderNSA());

        connection.setReplyTo(reserveRequestType.getReplyTo());
        connection.setRequesterNsa(reserveRequestType.getReserve().getRequesterNSA());
        connection.setServiceParameters(reserveRequestType.getReserve().getReservation().getServiceParameters());

        return connection;
      }
    };

  private final Logger logger = LoggerFactory.getLogger(ConnectionServiceProvider.class);

  @Autowired
  private ConnectionRepo connectionRepo;

  @Autowired
  private VirtualPortRepo virtualPortRepo;

  @Autowired
  private VirtualResourceGroupRepo virtualResourceGroupRepo;

  @Resource
  private WebServiceContext webServiceContext;

  @Autowired
  private ReservationService reservationService;

  @Resource(name = "nsaProviderUrns")
  private List<String> nsaProviderUrns;

  private final Function<Connection, Reservation> TO_RESERVATION =
    new Function<Connection, Reservation>() {
      @Override
      public Reservation apply(final Connection connection) {

        final Reservation reservation = new Reservation();

        reservation.setConnection(connection);

        reservation.setName(connection.getDescription());
        reservation.setStartDateTime(new LocalDateTime(connection.getStartTime()));
        reservation.setEndDateTime(new LocalDateTime(connection.getEndTime()));

        String sourceStpId = connection.getPath().getSourceSTP().getStpId();
        String destinationStpId = connection.getPath().getDestSTP().getStpId();
        VirtualPort sourcePort = virtualPortRepo.findByManagerLabel(sourceStpId);
        VirtualPort destinationPort = virtualPortRepo.findByManagerLabel(destinationStpId);

        reservation.setSourcePort(sourcePort);
        reservation.setDestinationPort(destinationPort);
        reservation.setVirtualResourceGroup(sourcePort.getVirtualResourceGroup());

        reservation.setBandwidth(connection.getDesiredBandwidth());
        reservation.setUserCreated(connection.getRequesterNsa());

        return reservation;
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

  private ConnectionRequesterPort getConnectionRequesterPort(Connection connection) {
    URL url;
    try {
      url = new ClassPathResource("/wsdl/nsi/ogf_nsi_connection_requester_v1_0.wsdl").getURL();
    }
    catch (IOException e) {
      logger.error("Error: ", e);
      throw new RuntimeException("Could not find the requester wsdl", e);
    }

    ConnectionRequesterPort port = new ConnectionServiceRequester(
        url,
        new QName("http://schemas.ogf.org/nsi/2011/10/connection/requester", "ConnectionServiceRequester")
      ).getConnectionServiceRequesterPort();

    final Map<String, Object> requestContext = ((BindingProvider) port).getRequestContext();

    // TODO: get credentials from reservation request
//    requestContext.put(BindingProvider.USERNAME_PROPERTY, "nsi");
//    requestContext.put(BindingProvider.PASSWORD_PROPERTY, "nsi123");
//    requestContext.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

    requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, connection.getReplyTo());

    return port;
  }

//  private void sendTerminateToChildNsa(final String correlationId, final String nsaChild, final String nsaRequester) {
//
//    final GenericRequestType genericRequestType = new GenericRequestType();
//    genericRequestType.setConnectionId(correlationId);
//    genericRequestType.setProviderNSA(nsaChild);
//    genericRequestType.setRequesterNSA(nsaRequester);
//
//    // TODO: Get security attribute
//    genericRequestType.setSessionSecurityAttr(null);
//
//    final TerminateRequestType terminator = new TerminateRequestType();
//    terminator.setCorrelationId(correlationId);
//    terminator.setReplyTo(nsaRequester);
//    terminator.setTerminate(genericRequestType);
//    log.debug("Sendign terminate event to child nsa: {} with id: {}", nsaChild, correlationId);
//  }

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
  @Override
  public GenericAcknowledgmentType reserve(final ReserveRequestType reservationRequest) throws ServiceException {
    checkNotNull(reservationRequest);

    Connection connection = TO_CONNECTION.apply(reservationRequest);

    if (!StringUtils.hasText(connection.getGlobalReservationId())) {
      connection.setGlobalReservationId(generateGlobalId());
    }

    logger.debug("Received reservation request connectionId: {}", connection.getConnectionId());

    // TODO validating
    // Validate if connection id is unique (gives db constraint exception now)
    // Validate CorrelationId
    // Validate if virtual ports are known..

    connection = connectionRepo.save(connection);

    validateConnection(connection, reservationRequest);

    createReservation(connection, false);

    /*
     * Save the calling NSA security context and pass it along for use during
     * processing of request (when implemented).
     */

    /*
     * We successfully sent the message for processing so acknowledge it back to
     * the requesting NSA. We hope this returns before the confirmation makes it
     * back to the requesting NSA.
     */

    GenericAcknowledgmentType genericAcknowledgment = new GenericAcknowledgmentType();
    genericAcknowledgment.setCorrelationId(reservationRequest.getCorrelationId());

    logger.debug("Returning acknowledgment for connection {} with correlationId {}",
        connection.getConnectionId(), genericAcknowledgment.getCorrelationId());

    return genericAcknowledgment;
  }

  private void validateConnection(Connection connection, ReserveRequestType reservationRequest) throws ServiceException {
    if (!isValidId(connection.getConnectionId())) {
      connection.setCurrentState(CLEANING);
      connection = connectionRepo.save(connection);
      throw new ServiceException("SVC0001", getInvalidParameterServiceException("connectionId"));
    }

    if (!isValidProviderNsa(reservationRequest)) {
      connection.setCurrentState(CLEANING);
      connection = connectionRepo.save(connection);
      throw new ServiceException("SVC0001", getInvalidParameterServiceException("providerNSA"));
    }
  }

  @Override
  public void reserveConfirmed(final Connection connection) {
    logger.debug("Sending a reserveConfirmed on endpoint: {} with id: {}", connection.getReplyTo(),
        connection.getGlobalReservationId());

    ReserveConfirmedType reserveConfirmedType = new ObjectFactory().createReserveConfirmedType();
    reserveConfirmedType.setRequesterNSA(connection.getRequesterNsa());
    reserveConfirmedType.setProviderNSA(connection.getProviderNsa());

    ReservationInfoType reservationInfoType = new ObjectFactory().createReservationInfoType();
    reservationInfoType.setConnectionId(connection.getConnectionId());
    reservationInfoType.setDescription(connection.getDescription());
    reservationInfoType.setGlobalReservationId(connection.getGlobalReservationId());
    reservationInfoType.setPath(connection.getPath());
    reservationInfoType.setServiceParameters(connection.getServiceParameters());

    reserveConfirmedType.setReservation(reservationInfoType);

    try {
      ConnectionRequesterPort port = getConnectionRequesterPort(connection);
      port.reserveConfirmed(new Holder<String>(connection.getConnectionId()), reserveConfirmedType);
    }
    catch (org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException e) {
      logger.error("Error: ", e);
    }
  }

  @Override
  public void reserveFailed(final Connection connection) {
    logger.debug("Sending a reserveFailed on endpoint: {} with id: {}", connection.getReplyTo(),
        connection.getGlobalReservationId());

    GenericFailedType reservationFailed = new GenericFailedType();
    reservationFailed.setRequesterNSA(connection.getRequesterNsa());
    reservationFailed.setProviderNSA(connection.getProviderNsa());
    reservationFailed.setGlobalReservationId(connection.getGlobalReservationId());
    reservationFailed.setConnectionId(connection.getConnectionId());
    reservationFailed.setConnectionState(connection.getCurrentState());

    // FIXME: What to put into the service exception
    ServiceExceptionType serviceException = new ServiceExceptionType();
    serviceException.setErrorId("ERROR_ID");
    serviceException.setText("Some text");
    AttributeStatementType values = new AttributeStatementType();
    serviceException.setVariables(values);
    reservationFailed.setServiceException(serviceException);

    try {
      ConnectionRequesterPort port = getConnectionRequesterPort(connection);
      port.reserveFailed(new Holder<String>(connection.getConnectionId()), reservationFailed);
    }
    catch (org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException e) {
      logger.error("Error: ", e);
    }
  }

  private String generateGlobalId() {
    return BOD_URN_POSTFIX + UUID.randomUUID();
  }

  private void createReservation(final Connection connection, boolean autoProvision) {
    Reservation reservation = TO_RESERVATION.apply(connection);
    reservation = reservationService.create(reservation, autoProvision);

    connection.setCurrentState(RESERVING);
    connection.setReservation(reservation);
    connectionRepo.save(connection);
  }

  @Override
  public GenericAcknowledgmentType provision(ProvisionRequestType parameters) throws ServiceException {

    String connectionId = parameters.getProvision().getConnectionId();
    logger.debug("Received provision request with id: {}", connectionId);

    Connection connection = connectionRepo.findByConnectionId(connectionId);

    boolean isActivated = reservationService.activate(connection.getReservation());

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
    logger.debug("Calling sendReserveConfirmed on endpoint: {} with id: {}", connection.getReplyTo(),
        connection.getGlobalReservationId());

    final GenericFailedType generic = new GenericFailedType();

    generic.setProviderNSA(connection.getProviderNsa());
    generic.setRequesterNSA(connection.getRequesterNsa());
    generic.setConnectionId(connection.getConnectionId());
    generic.setGlobalReservationId(connection.getGlobalReservationId());

    try {
      ConnectionRequesterPort port = getConnectionRequesterPort(connection);
      port.provisionFailed(new Holder<String>(connection.getConnectionId()), generic);
    }
    catch (org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException e) {
      logger.error("Error: ", e);
    }

  }

  private void sendProvisionConfirmed(final Connection connection) {

    logger.debug("Calling sendReserveConfirmed on endpoint: {} with id: {}", connection.getReplyTo(),
        connection.getGlobalReservationId());

    final GenericConfirmedType generic = new GenericConfirmedType();
    generic.setProviderNSA(connection.getProviderNsa());
    generic.setRequesterNSA(connection.getRequesterNsa());
    generic.setConnectionId(connection.getConnectionId());
    generic.setGlobalReservationId(connection.getGlobalReservationId());

    try {
      ConnectionRequesterPort port = getConnectionRequesterPort(connection);
      port.provisionConfirmed(new Holder<String>(connection.getConnectionId()), generic);
    }
    catch (org.ogf.schemas.nsi._2011._10.connection.requester.ServiceException e) {
      logger.error("Error: ", e);
    }

  }

  @Override
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

  @Override
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

  @Override
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

  @Override
  public void queryConfirmed(Holder<String> correlationId, QueryConfirmedType queryConfirmed) throws ServiceException {
    throw new UnsupportedOperationException("Not implemented yet.");
  }

  @Override
  public void queryFailed(Holder<String> correlationId, QueryFailedType queryFailed) throws ServiceException {
    throw new UnsupportedOperationException("Not implemented yet.");
  }

  protected boolean isValidId(final String id) {
    return StringUtils.hasText(id);
  }

  public static String getCorrelationId() {
    return URN_UUID + UUID.randomUUID().toString();
  }

}