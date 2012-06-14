/**
 * Copyright (c) 2011, SURFnet bv, The Netherlands
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   - Neither the name of the SURFnet bv, The Netherlands nor the names of
 *     its contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL SURFnet bv, The Netherlands BE LIABLE FOR
 * AND DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *
 */
package nl.surfnet.bod.nsi.ws.v1sc;

import static org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType.*;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Resource;
import javax.jws.WebService;
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
import org.ogf.schemas.nsi._2011._10.connection.types.GenericFailedType;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericRequestType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryFailedType;
import org.ogf.schemas.nsi._2011._10.connection.types.ReservationInfoType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceExceptionType;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.nsi.ws.ConnectionService;

@Service("nsiProvider_v1_sc")
@WebService(serviceName = "ConnectionServiceProvider",
    portName = "ConnectionServiceProviderPort",
    endpointInterface = "org.ogf.schemas.nsi._2011._10.connection.provider.ConnectionProviderPort",
    targetNamespace = "http://schemas.ogf.org/nsi/2011/10/connection/provider",
    wsdlLocation = "/WEB-INF/wsdl/nsi/ogf_nsi_connection_provider_v1_0.wsdl")
public class ConnectionServiceProvider extends ConnectionService {

  private final Logger log = getLog();

  private Long delayBeforeResponseSend = 2000L;

  @Resource(name = "nsaProviderUrns")
  private List<String> nsaProviderUrns;

  private List<String> nsaChildren = ImmutableList.of("urn:ogf:network:nsa:child1", "urn:ogf:network:nsa:child2",
      "urn:ogf:network:nsa:child3");

  private static final Function<Connection, Reservation> TO_RESERVATION = //
  new Function<Connection, Reservation>() {
    @Override
    public Reservation apply(final Connection connection) {

      final Reservation reservation = new Reservation();

      reservation.setBandwidth(connection.getDesiredBandwidth());
      // reservation.setDestinationPort(destinationPort);
      reservation.setEndDateTime(new LocalDateTime(connection.getEndTime()));
      // reservation.setFailedMessage(failedMessage);
      // reservation.setName(name);
      // reservation.setReservationId(reservationId);
      // reservation.setSourcePort(sourcePort);
      reservation.setStartDateTime(new LocalDateTime(connection.getStartTime()));
      // reservation.setStatus(reservationStatus);
      reservation.setUserCreated(connection.getRequesterNsa());

      // @NotNull
      // @ManyToOne(optional = false)
      // private VirtualPort sourcePort;
      //
      // @NotNull
      // @ManyToOne(optional = false)
      // private VirtualPort destinationPort;
      //
      // @Type(type = "org.joda.time.contrib.hibernate.PersistentLocalDateTime")
      // private LocalDateTime startDateTime;
      //
      // @Type(type = "org.joda.time.contrib.hibernate.PersistentLocalDateTime")
      // private LocalDateTime endDateTime;
      //
      // @Column(nullable = false)
      // private String userCreated;
      //
      // @NotNull
      // @Column(nullable = false)
      // private Integer bandwidth;
      //
      // @Basic
      // private String reservationId;
      //
      // @NotNull
      // @Column(nullable = false)
      // @Type(type = "org.joda.time.contrib.hibernate.PersistentLocalDateTime")
      // private LocalDateTime creationDateTime;
      //
      //
      //
      // final Reservation reservation = new Reservation();
      // reservation.set
      // reservation.set
      // reservation.set
      // reservation.set
      // reservation.set
      // reservation.set
      // reservation.set
      // reservation.set

      return null;
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
      final Map<String, Object> requestContext = ((BindingProvider) connectionServiceRequesterPort).getRequestContext();

      // TODO: get credentials from reservation request
      requestContext.put(BindingProvider.USERNAME_PROPERTY, "nsi");
      requestContext.put(BindingProvider.PASSWORD_PROPERTY, "nsi123");
      requestContext.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

      requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, connection.getReplyTo());
      connectionServiceRequesterPort.reserveFailed(new Holder<String>(connection.getConnectionId()), reservationFailed);
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
    log.info("reserveRequest.getReserve().getProviderNSA(): "+reserveRequest.getReserve().getProviderNSA());
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
    connection.setCurrentState(INITIAL);
    connection = getconnectionRepo().save(connection);
    log.info("connection: "+connection);

    log.debug("Received reservation request with id: {}", connection.getConnectionId());
    
    if (!isValidCorrelationId(connection.getConnectionId())) {
      connection = getconnectionRepo().findOne(connection.getId());
      connection.setCurrentState(CLEANING);
      connection = getconnectionRepo().save(connection);
      throw new ServiceException("SVC0001", getInvalidParameterServiceException("correlationId"));
    }

    connection = getconnectionRepo().findOne(connection.getId());
    connection.setCurrentState(RESERVING);
    connection = getconnectionRepo().save(connection);
    
    System.out.println(getconnectionRepo().findAll());

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
      connection = getconnectionRepo().findOne(connection.getId());
      connection.setCurrentState(CLEANING);
      connection = getconnectionRepo().save(connection);
      throw new ServiceException("SVC0001", getInvalidParameterServiceException("providerNSA"));
    }

    /*
     * Get the connectionId from the reservation as we will use this to
     * serialize related requests.
     */

    // Route this message to the appropriate actor for processing.

    // for now always fail the reservation
    forceFailed(connection);

    /*
     * We successfully sent the message for processing so acknowledge it back to
     * the requesting NSA. We hope this returns before the confirmation makes it
     * back to the requesting NSA.
     */
    final GenericAcknowledgmentType genericAcknowledgment = new GenericAcknowledgmentType();
    genericAcknowledgment.setCorrelationId(reservationRequest.getCorrelationId());

    log.info("Returning GenericAcknowledgmentType with id: {}", genericAcknowledgment.getCorrelationId());
    return genericAcknowledgment;
  }

  /**
   * @param correlationId
   * @param nsaRequester
   */
  private void forceFailed(final Connection con) {
    
    log.info("Searching for: {}", con.getConnectionId());
    
    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        Connection connection = getconnectionRepo().findByConnectionId(con.getConnectionId());
        
        final List<Connection> connections = getconnectionRepo().findAll();
        System.out.println(connections);
        
        System.out.println(connection);
        connection.setCurrentState(CLEANING);
        connection = getconnectionRepo().save(connection);
        sendReservationFailed(connection);
        for (final String nsaChild : nsaChildren) {
          sendTerminateToChildNsa(connection.getConnectionId(), nsaChild, connection.getRequesterNsa());
        }
        sendTerminatToNrm(connection.getConnectionId());

        // FIXME: Do I have to delete the connection or keep it for archiving
        // purposes?
        getconnectionRepo().delete(connection);

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
