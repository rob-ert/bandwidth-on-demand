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
package nl.surfnet.bod.nsi.v1sc;

import static nl.surfnet.bod.nsi.ConnectionServiceProviderError.CONNECTION_NON_EXISTENT;
import static nl.surfnet.bod.nsi.v1sc.ConnectionServiceProviderFunctions.reserveRequestToConnection;

import java.net.URI;
import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.Holder;

import com.sun.xml.ws.developer.SchemaValidation;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.ConnectionV1;
import nl.surfnet.bod.domain.NsiV1RequestDetails;
import nl.surfnet.bod.domain.oauth.NsiScope;
import nl.surfnet.bod.nsi.ConnectionServiceProviderError;
import nl.surfnet.bod.nsi.NsiHelper;
import nl.surfnet.bod.nsi.v1sc.ConnectionServiceV1.ValidationException;
import nl.surfnet.bod.repo.ConnectionV1Repo;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import oasis.names.tc.saml._2_0.assertion.AttributeStatementType;
import oasis.names.tc.saml._2_0.assertion.AttributeType;

import org.ogf.schemas.nsi._2011._10.connection._interface.GenericAcknowledgmentType;
import org.ogf.schemas.nsi._2011._10.connection._interface.ProvisionRequestType;
import org.ogf.schemas.nsi._2011._10.connection._interface.QueryRequestType;
import org.ogf.schemas.nsi._2011._10.connection._interface.ReleaseRequestType;
import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;
import org.ogf.schemas.nsi._2011._10.connection._interface.TerminateRequestType;
import org.ogf.schemas.nsi._2011._10.connection.provider.ConnectionProviderPort;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryFailedType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryOperationType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceExceptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("connectionServiceProviderWs_v1sc")
@WebService(serviceName = "ConnectionServiceProvider",
  portName = "ConnectionServiceProviderPort",
  endpointInterface = "org.ogf.schemas.nsi._2011._10.connection.provider.ConnectionProviderPort",
  targetNamespace = "http://schemas.ogf.org/nsi/2011/10/connection/provider")
@SchemaValidation
public class ConnectionServiceProviderV1Ws implements ConnectionProviderPort {

  private final Logger log = LoggerFactory.getLogger(ConnectionServiceProviderV1Ws.class);

  @Resource private ConnectionV1Repo connectionRepo;
  @Resource private ConnectionServiceV1 connectionService;
  @Resource private NsiHelper nsiHelper;
  @Resource private Environment bodEnvironment;

  @Override
  public GenericAcknowledgmentType reserve(ReserveRequestType reservationRequest) throws ServiceException {
    checkOAuthScope(NsiScope.RESERVE);

    log.info("Received a NSI v1 reserve request connectionId {}", reservationRequest.getReserve().getReservation().getConnectionId());

    ConnectionV1 connection = reserveRequestToConnection(nsiHelper, bodEnvironment.getDefaultProtectionType()).apply(reservationRequest);

    NsiV1RequestDetails requestDetails = new NsiV1RequestDetails(URI.create(reservationRequest.getReplyTo()), reservationRequest.getCorrelationId());

    reserve(connection, requestDetails, Security.getUserDetails());

    return createGenericAcknowledgment(requestDetails.getCorrelationId());
  }

  protected void reserve(ConnectionV1 connection, NsiV1RequestDetails request, RichUserDetails richUserDetails) throws ServiceException {
    try {
      connectionService.reserve(connection, request, false, richUserDetails);
    } catch (ValidationException e) {
      reThrowAsSeviceException(e);
    }
  }

  @Override
  public GenericAcknowledgmentType provision(ProvisionRequestType parameters) throws ServiceException {
    checkOAuthScope(NsiScope.PROVISION);

    log.info("Received provision request for connection: {}", parameters.getProvision().getConnectionId());

    Connection connection = getConnectionOrFail(parameters.getProvision().getConnectionId());

    NsiV1RequestDetails requestDetails = new NsiV1RequestDetails(URI.create(parameters.getReplyTo()), parameters.getCorrelationId());
    connectionService.provision(connection.getId(), requestDetails);

    return createGenericAcknowledgment(requestDetails.getCorrelationId());
  }


  @Override
  public GenericAcknowledgmentType release(ReleaseRequestType parameters) throws ServiceException {
    checkOAuthScope(NsiScope.RELEASE);

    throw createServiceException(ConnectionServiceProviderError.NOT_IMPLEMENTED);
  }

  @Override
  public GenericAcknowledgmentType terminate(TerminateRequestType parameters) throws ServiceException {
    checkOAuthScope(NsiScope.TERMINATE);

    log.info("Received a NSI terminate request for connectionId '{}'", parameters.getTerminate().getConnectionId());

    Connection connection = getConnectionOrFail(parameters.getTerminate().getConnectionId());

    NsiV1RequestDetails requestDetails = new NsiV1RequestDetails(URI.create(parameters.getReplyTo()), parameters.getCorrelationId());

    connectionService.asyncTerminate(connection.getId(), parameters.getTerminate().getRequesterNSA(),
        requestDetails, Security.getUserDetails());

    return createGenericAcknowledgment(requestDetails.getCorrelationId());
  }

  @Override
  public GenericAcknowledgmentType query(QueryRequestType parameters) throws ServiceException {
    checkOAuthScope(NsiScope.QUERY);

    List<String> connectionIds = parameters.getQuery().getQueryFilter().getConnectionId();
    List<String> globalReservationIds = parameters.getQuery().getQueryFilter().getGlobalReservationId();

    log.info("Received NSI query request connectionIds: {}, globalReservationIds: {}", connectionIds, globalReservationIds);

    NsiV1RequestDetails requestDetails = new NsiV1RequestDetails(URI.create(parameters.getReplyTo()), parameters.getCorrelationId());
    QueryOperationType operation = parameters.getQuery().getOperation();

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
    // This is an incoming response. But for now we don't do any queries as a provider.
    throw new UnsupportedOperationException("Not implemented yet.");
  }

  @Override
  public void queryFailed(Holder<String> correlationId, QueryFailedType queryFailed) throws ServiceException {
    // This is an incoming response. But for now we don't do any queries as a provider.
    throw new UnsupportedOperationException("Not implemented yet.");
  }

  private void reThrowAsSeviceException(ValidationException ve) throws ServiceException {
    throw createServiceException(ve.getAttributeName(), ve.getError(), ve.getMessage());
  }

  private ServiceException createServiceException(ConnectionServiceProviderError error) {
    ServiceExceptionType serviceExceptionType = new ServiceExceptionType();
    serviceExceptionType.setErrorId(error.getErrorId());
    serviceExceptionType.setText(error.getMessage());

    return new ServiceException(error.getMessage(), serviceExceptionType);
  }

  private ServiceException createServiceException(String attributeName, ConnectionServiceProviderError error, String errorMessage) {
    ServiceExceptionType serviceExceptionType = new ServiceExceptionType()
      .withErrorId(error.getErrorId())
      .withText(errorMessage);

    AttributeType attribute = new AttributeType()
      .withName(attributeName)
      .withNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:basic");

    AttributeStatementType attributeStatement = new AttributeStatementType().withAttributeOrEncryptedAttribute(attribute);

    serviceExceptionType.setVariables(attributeStatement);

    return new ServiceException(error.getErrorId(), serviceExceptionType);
  }

  private Connection getConnectionOrFail(String connectionId) throws ServiceException {
    Connection connection = connectionRepo.findByConnectionId(connectionId);
    if (connection == null) {
      throw createServiceException("connectionId", CONNECTION_NON_EXISTENT, CONNECTION_NON_EXISTENT.getMessage());
    }
    return connection;
  }

  private GenericAcknowledgmentType createGenericAcknowledgment(final String correlationId) {
    GenericAcknowledgmentType genericAcknowledgmentType = new GenericAcknowledgmentType();
    genericAcknowledgmentType.setCorrelationId(correlationId);
    return genericAcknowledgmentType;
  }

  private void checkOAuthScope(NsiScope scope) throws ServiceException {
    if (!Security.hasOauthScope(scope)) {
      log.warn("OAuth access token not valid for {}", scope);
      throw createServiceException(ConnectionServiceProviderError.UNAUTHORIZED);
    }
  }

  static {
    // Don't show full stack trace in soap result if an exception occurs
    System.setProperty("com.sun.xml.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace", "false");
    // System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
    // System.setProperty("com.sun.xml.ws.util.pipe.StandaloneTubeAssembler.dump", "true");
    // System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
  }

}
