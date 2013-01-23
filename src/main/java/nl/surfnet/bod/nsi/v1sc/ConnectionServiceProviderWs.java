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

import static com.google.common.base.Preconditions.checkNotNull;
import static nl.surfnet.bod.nsi.ConnectionServiceProviderErrorCodes.PAYLOAD.NOT_IMPLEMENTED;
import static nl.surfnet.bod.nsi.v1sc.ConnectionServiceProviderFunctions.RESERVE_REQUEST_TO_CONNECTION;
import static org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType.TERMINATED;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.Holder;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.oauth.NsiScope;
import nl.surfnet.bod.nsi.ConnectionServiceProviderErrorCodes;
import nl.surfnet.bod.repo.ConnectionRepo;
import nl.surfnet.bod.service.ConnectionService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import oasis.names.tc.saml._2_0.assertion.AttributeStatementType;
import oasis.names.tc.saml._2_0.assertion.AttributeType;

import org.ogf.schemas.nsi._2011._10.connection._interface.*;
import org.ogf.schemas.nsi._2011._10.connection.provider.ConnectionProviderPort;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryFailedType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryOperationType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceExceptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.annotations.VisibleForTesting;
import com.sun.xml.ws.developer.SchemaValidation;

@Service("connectionServiceProviderWs_v1sc")
@WebService(serviceName = "ConnectionServiceProvider",
  portName = "ConnectionServiceProviderPort",
  endpointInterface = "org.ogf.schemas.nsi._2011._10.connection.provider.ConnectionProviderPort",
  targetNamespace = "http://schemas.ogf.org/nsi/2011/10/connection/provider")
@SchemaValidation
public class ConnectionServiceProviderWs implements ConnectionProviderPort {

  static final String SVC0003_ALREADY_EXISTS = "SVC0003";
  static final String SVC0005_INVALID_CREDENTIALS = "SVC0005";
  static final String SVC0001_INVALID_PARAM = "SVC0001";

  private final Logger log = LoggerFactory.getLogger(ConnectionServiceProviderWs.class);

  @Resource
  private ConnectionRepo connectionRepo;

  @Resource
  private VirtualPortService virtualPortService;

  @Resource
  private ConnectionService connectionService;

  @Resource(name = "nsaProviderUrns")
  private final List<String> nsaProviderUrns = new ArrayList<>();

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
    validateOAuthScope(NsiScope.RESERVE);

    log.info("Received a NSI reserve request connectionId {}", reservationRequest.getReserve().getReservation().getConnectionId());

    Connection connection = RESERVE_REQUEST_TO_CONNECTION.apply(reservationRequest);

    final NsiRequestDetails requestDetails = new NsiRequestDetails(reservationRequest.getReplyTo(), reservationRequest
        .getCorrelationId());

    reserve(connection, requestDetails, Security.getUserDetails());

    return createGenericAcknowledgment(requestDetails.getCorrelationId());
  }

  protected void reserve(Connection connection, NsiRequestDetails request, RichUserDetails richUserDetails)
      throws ServiceException {
    validateConnection(connection, richUserDetails);
    connectionService.reserve(connection, request, false, richUserDetails);
  }

  private void validateConnection(Connection connection, RichUserDetails richUserDetails) throws ServiceException {
    try {
      validateProviderNsa(connection.getProviderNsa());
      validateConnectionId(connection.getConnectionId());
      validatePort(connection.getSourceStpId(), "sourceSTP", richUserDetails);
      validatePort(connection.getDestinationStpId(), "destSTP", richUserDetails);
    }
    catch (ServiceException e) {
      connection.setCurrentState(TERMINATED);
      connectionRepo.save(connection);
      throw e;
    }
  }

  private void validateProviderNsa(String providerNsa) throws ServiceException {
    if (nsaProviderUrns.contains(providerNsa)) {
      return;
    }

    log.warn("ProviderNsa '{}' is not accepted", providerNsa);

    throw createInvalidParameterServiceException("providerNSA");
  }

  private void validateConnectionId(String connectionId) throws ServiceException {
    if (StringUtils.hasText(connectionId)) {
      if (connectionRepo.findByConnectionId(connectionId) != null) {
        log.warn("ConnectionId {} was not unique", connectionId);
        throw createAlreadyExistsServiceException("connectionId");
      }
    }
    else {
      log.warn("ConnectionId was empty", connectionId);
      throw createInvalidParameterServiceException("connectionId");
    }
  }

  private void validatePort(String stpId, String attribute, RichUserDetails user) throws ServiceException {
    final VirtualPort port = virtualPortService.findByNsiStpId(stpId);

    if (port == null) {
      log.warn("Could not find a virtual port for stpId '{}'", stpId);
      throw createInvalidParameterServiceException(attribute);
    }

    if (!user.getUserGroupIds().contains(port.getVirtualResourceGroup().getAdminGroup())) {
      log.warn("User has no rights on virtual port with stpId '{}'", stpId);
      throw createInvalidOrMissingUserCredentialsException(attribute);
    }
  }

  @Override
  public GenericAcknowledgmentType provision(ProvisionRequestType parameters) throws ServiceException {
    validateOAuthScope(NsiScope.PROVISION);
    validateProviderNsa(parameters.getProvision().getProviderNSA());

    final Connection connection = getConnectionOrFail(parameters.getProvision().getConnectionId());

    log.info("Received provision request for connection: {}", connection);

    NsiRequestDetails requestDetails = new NsiRequestDetails(parameters.getReplyTo(), parameters.getCorrelationId());
    connectionService.provision(connection.getId(), requestDetails);

    return createGenericAcknowledgment(requestDetails.getCorrelationId());
  }


  @Override
  public GenericAcknowledgmentType release(ReleaseRequestType parameters) throws ServiceException {
    validateOAuthScope(NsiScope.RELEASE);

    ServiceExceptionType exceptionType = new ServiceExceptionType();
    exceptionType.setErrorId(NOT_IMPLEMENTED.getId());
    exceptionType.setText(NOT_IMPLEMENTED.getText());

    throw new ServiceException("Not supported", exceptionType);
  }

  @Override
  public GenericAcknowledgmentType terminate(TerminateRequestType parameters) throws ServiceException {
    log.info("Received a NSI terminate request for connectionId '{}'", parameters.getTerminate().getConnectionId());

    validateOAuthScope(NsiScope.TERMINATE);
    validateProviderNsa(parameters.getTerminate().getProviderNSA());

    Connection connection = getConnectionOrFail(parameters.getTerminate().getConnectionId());

    NsiRequestDetails requestDetails = new NsiRequestDetails(parameters.getReplyTo(), parameters.getCorrelationId());

    connectionService.asyncTerminate(connection.getId(), parameters.getTerminate().getRequesterNSA(),
        requestDetails, Security.getUserDetails());

    return createGenericAcknowledgment(requestDetails.getCorrelationId());
  }

  @Override
  public GenericAcknowledgmentType query(QueryRequestType parameters) throws ServiceException {
    validateOAuthScope(NsiScope.QUERY);
    validateProviderNsa(parameters.getQuery().getProviderNSA());

    NsiRequestDetails requestDetails = new NsiRequestDetails(parameters.getReplyTo(), parameters.getCorrelationId());
    List<String> connectionIds = parameters.getQuery().getQueryFilter().getConnectionId();
    List<String> globalReservationIds = parameters.getQuery().getQueryFilter().getGlobalReservationId();
    QueryOperationType operation = parameters.getQuery().getOperation();

    log.info("Received NSI query request connectionIds: {}, globalReservationIds: {}", connectionIds, globalReservationIds);

    if (connectionIds.isEmpty() && globalReservationIds.isEmpty()) {
      connectionService.asyncQueryAllForRequesterNsa(requestDetails,
          operation, parameters.getQuery().getRequesterNSA(), parameters.getQuery().getProviderNSA());
    }
    else {
      connectionService.asyncQueryConnections(requestDetails, connectionIds, globalReservationIds,
          operation, parameters.getQuery().getRequesterNSA(), parameters.getQuery().getProviderNSA());
    }

    return createGenericAcknowledgment(parameters.getCorrelationId());
  }

  @Override
  public void queryConfirmed(Holder<String> correlationId, QueryConfirmedType queryConfirmed) throws ServiceException {
    // This is an incoming response. But for now we don't do any queries.
    throw new UnsupportedOperationException("Not implemented yet.");
  }

  @Override
  public void queryFailed(Holder<String> correlationId, QueryFailedType queryFailed) throws ServiceException {
    // This is an incoming response. But for now we don't do any queries.
    throw new UnsupportedOperationException("Not implemented yet.");
  }

  @VisibleForTesting
  protected void addNsaProvider(String provider) {
    this.nsaProviderUrns.add(provider);
  }

  private ServiceException createInvalidParameterServiceException(final String attributeName) {
    return createServiceException(attributeName, SVC0001_INVALID_PARAM, "Invalid or missing parameter");
  }

  private ServiceException createInvalidOrMissingUserCredentialsException(final String attributeName) {
    return createServiceException(attributeName, SVC0005_INVALID_CREDENTIALS, "Invalid or missing user credentials");
  }

  private ServiceException createAlreadyExistsServiceException(String attributeName) {
    return createServiceException(attributeName, SVC0003_ALREADY_EXISTS, "Schedule already exists for connectionId");
  }

  private ServiceException createServiceException(final String attributeName, final String errorCode,
      final String errorMessage) {
    final ServiceExceptionType serviceExceptionType = new ServiceExceptionType();
    serviceExceptionType.setErrorId(errorCode);
    serviceExceptionType.setText(errorMessage);

    final AttributeType attribute = new AttributeType();
    attribute.setName(attributeName);
    attribute.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:basic");

    final AttributeStatementType attributeStatement = new AttributeStatementType();
    attributeStatement.getAttributeOrEncryptedAttribute().add(attribute);

    serviceExceptionType.setVariables(attributeStatement);

    return new ServiceException(errorCode, serviceExceptionType);
  }

  private Connection getConnectionOrFail(String connectionId) throws ServiceException {
    final Connection connection = connectionRepo.findByConnectionId(connectionId);
    if (connection == null) {
      throw createInvalidParameterServiceException("connectionId");
    }
    return connection;
  }

  private GenericAcknowledgmentType createGenericAcknowledgment(final String correlationId) {
    final GenericAcknowledgmentType genericAcknowledgmentType = new GenericAcknowledgmentType();
    genericAcknowledgmentType.setCorrelationId(correlationId);
    return genericAcknowledgmentType;
  }

  private void validateOAuthScope(NsiScope scope) throws ServiceException {
    if (!Security.getUserDetails().getNsiScopes().contains(scope)) {
      log.warn("OAuth access token not valid for {}", scope);
      throw createServiceException(ConnectionServiceProviderErrorCodes.SECURITY.MISSING_GRANTED_SCOPE);
    }
  }

  private ServiceException createServiceException(ConnectionServiceProviderErrorCodes.SECURITY error) {
    final ServiceExceptionType serviceExceptionType = new ServiceExceptionType();
    serviceExceptionType.setErrorId(error.getId());
    serviceExceptionType.setText(error.getText());

    return new ServiceException("403", serviceExceptionType);
  }

  static {
    // Don't show full stack trace in soap result if an exception occurs
    System.setProperty("com.sun.xml.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace", "false");
    // System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump",
    // "true");
    // System.setProperty("com.sun.xml.ws.util.pipe.StandaloneTubeAssembler.dump",
    // "true");
    // System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump",
    // "true");
  }

}
