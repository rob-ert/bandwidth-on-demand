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
package nl.surfnet.bod.nsi.v2;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.collect.Lists.transform;
import static nl.surfnet.bod.nsi.ConnectionServiceProviderError.CONNECTION_NON_EXISTENT;
import static nl.surfnet.bod.nsi.ConnectionServiceProviderError.INVALID_TRANSITION;
import static nl.surfnet.bod.nsi.ConnectionServiceProviderError.MISSING_PARAMETER;
import static nl.surfnet.bod.nsi.ConnectionServiceProviderError.NOT_IMPLEMENTED;
import static nl.surfnet.bod.nsi.ConnectionServiceProviderError.UNAUTHORIZED;
import static nl.surfnet.bod.nsi.ConnectionServiceProviderError.UNSUPPORTED_PARAMETER;
import static nl.surfnet.bod.nsi.v2.ConnectionsV2.toQuerySummaryResultType;
import static nl.surfnet.bod.util.XmlUtils.xmlCalendarToDateTime;

import java.net.URI;
import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.Holder;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.sun.xml.ws.developer.SchemaValidation;

import nl.surfnet.bod.domain.ConnectionV2;
import nl.surfnet.bod.domain.NsiV2RequestDetails;
import nl.surfnet.bod.domain.oauth.NsiScope;
import nl.surfnet.bod.nsi.ConnectionServiceProviderError;
import nl.surfnet.bod.nsi.NsiHelper;
import nl.surfnet.bod.repo.ConnectionV2Repo;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.joda.time.DateTime;
import org.ogf.schemas.nsi._2013._12.connection.provider.ConnectionProviderPort;
import org.ogf.schemas.nsi._2013._12.connection.provider.Error;
import org.ogf.schemas.nsi._2013._12.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2013._12.connection.types.GenericErrorType;
import org.ogf.schemas.nsi._2013._12.connection.types.LifecycleStateEnumType;
import org.ogf.schemas.nsi._2013._12.connection.types.NotificationBaseType;
import org.ogf.schemas.nsi._2013._12.connection.types.ProvisionStateEnumType;
import org.ogf.schemas.nsi._2013._12.connection.types.QueryNotificationConfirmedType;
import org.ogf.schemas.nsi._2013._12.connection.types.QueryNotificationType;
import org.ogf.schemas.nsi._2013._12.connection.types.QueryResultResponseType;
import org.ogf.schemas.nsi._2013._12.connection.types.QuerySummaryResultType;
import org.ogf.schemas.nsi._2013._12.connection.types.ReservationConfirmCriteriaType;
import org.ogf.schemas.nsi._2013._12.connection.types.ReservationRequestCriteriaType;
import org.ogf.schemas.nsi._2013._12.connection.types.ReservationStateEnumType;
import org.ogf.schemas.nsi._2013._12.framework.headers.CommonHeaderType;
import org.ogf.schemas.nsi._2013._12.framework.types.ServiceExceptionType;
import org.ogf.schemas.nsi._2013._12.framework.types.TypeValuePairType;
import org.ogf.schemas.nsi._2013._12.framework.types.VariablesType;
import org.ogf.schemas.nsi._2013._12.services.point2point.P2PServiceBaseType;
import org.ogf.schemas.nsi._2013._12.services.types.DirectionalityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("connectionServiceProviderWs_v2")
@WebService(serviceName = "ConnectionServiceProvider", portName = "ConnectionServiceProviderPort", endpointInterface = "org.ogf.schemas.nsi._2013._12.connection.provider.ConnectionProviderPort", targetNamespace = "http://schemas.ogf.org/nsi/2013/12/connection/provider")
@SchemaValidation // NOTE This does not validate headers, see #createRequestDetails for additional header validation.
public class ConnectionServiceProviderV2Ws implements ConnectionProviderPort {

  private final Logger log = LoggerFactory.getLogger(ConnectionServiceProviderV2Ws.class);

  @Resource private ConnectionServiceV2 connectionService;
  @Resource private ConnectionV2Repo connectionRepo;
  @Resource private Environment bodEnvironment;
  @Resource private NsiHelper nsiHelper;

  @Override
  public void reserve(Holder<String> connectionId, String globalReservationId, String description, ReservationRequestCriteriaType criteria, Holder<CommonHeaderType> header) throws ServiceException {
    NsiV2RequestDetails requestDetails = createRequestDetails(header.value);
    checkOAuthScope(NsiScope.RESERVE);

    log.info("Received a NSI v2 reserve request");

    if (!Strings.isNullOrEmpty(connectionId.value)) {
      // sending reservation message while supplying a connectionId indicates a 'modify' request, which we don't support
      throw notImplemented();
    }

    ConnectionV2 connection = createConnection(
        Optional.fromNullable(emptyToNull(globalReservationId)),
        Optional.fromNullable(emptyToNull(description)),
        requestDetails,
        header.value.getProviderNSA(),
        header.value.getRequesterNSA(),
        convertInitialRequestCriteria(criteria));

    connectionId.value = connection.getConnectionId();

    reserve(connection, requestDetails, Security.getUserDetails());
  }

  private void reserve(ConnectionV2 connection, NsiV2RequestDetails requestDetails, RichUserDetails richUserDetails) throws ServiceException {
    try {
      connectionService.reserve(connection, requestDetails, richUserDetails);
    } catch (ConnectionServiceV2.ReservationCreationException e) {
      throw new ServiceException(e.getMessage(), createServiceExceptionType(e.getErrorCode()));
    }
  }

  private ConnectionV2 createConnection(Optional<String> globalReservationId, Optional<String> description, NsiV2RequestDetails requestDetails,
      String providerNsa, String requesterNsa, ReservationConfirmCriteriaType criteria) throws ServiceException {
    Optional<DateTime> startTime = fromNullable(criteria.getSchedule().getStartTime()).transform(xmlCalendarToDateTime);
    Optional<DateTime> endTime = fromNullable(criteria.getSchedule().getEndTime()).transform(xmlCalendarToDateTime);

    P2PServiceBaseType service = extractService(criteria);
    if (service.getDirectionality() == DirectionalityType.UNIDIRECTIONAL) {
      throw unsupportedParameter("Directionality", service.getDirectionality());
    }

    validateStp(service.getSourceSTP(), "source");
    validateStp(service.getDestSTP(), "dest");

    ConnectionV2 connection = new ConnectionV2();
    connection.setConnectionId(NsiHelper.generateConnectionId());
    connection.setGlobalReservationId(globalReservationId.or(nsiHelper.generateGlobalReservationId()));
    connection.setDescription(description.orNull());
    connection.setStartTime(startTime.orNull());
    connection.setEndTime(endTime.orNull());
    connection.setDesiredBandwidth(service.getCapacity());
    connection.setProtectionType(bodEnvironment.getDefaultProtectionType());
    connection.setProviderNsa(providerNsa);
    connection.setRequesterNsa(requesterNsa);
    connection.setInitialReserveRequestDetails(requestDetails);
    connection.setLastReservationRequestDetails(requestDetails);
    connection.setReserveVersion(criteria.getVersion());
    connection.setCriteria(criteria);
    connection.setReserveHeldTimeoutValue(bodEnvironment.getNsiReserveHeldTimeoutValueInSeconds());
    connection.setLifecycleState(LifecycleStateEnumType.CREATED);

    return connection;
  }

  private void validateStp(String stpId, String sourceDest) throws ServiceException {
    if (!nsiHelper.isAcceptableStpIdV2(stpId)) {
      throw unsupportedParameter(sourceDest + "STP::networkId", stpId);
    }
  }

  private P2PServiceBaseType extractService(ReservationConfirmCriteriaType criteria) throws ServiceException {
    Optional<P2PServiceBaseType> service = ConnectionsV2.findPointToPointService(criteria);
    if (service.isPresent()) {
      return service.get();
    } else {
      throw unsupportedParameter("Unsupported service type", criteria.getAny());
    }
  }

  @Override
  public void reserveCommit(String connectionId, Holder<CommonHeaderType> header) throws ServiceException {
    NsiV2RequestDetails requestDetails = createRequestDetails(header.value);
    checkOAuthScope(NsiScope.RESERVE);

    log.info("Received reserve commit request for connection: {}", connectionId);

    ConnectionV2 connection = getConnectionOrFail(connectionId);
    if (connection.getReservationState() != ReservationStateEnumType.RESERVE_HELD) {
      throw invalidTransition(connection.getLifecycleState(), connection.getReservationState(), connection.getProvisionState());
    }
    connectionService.asyncReserveCommit(connectionId, requestDetails);
  }

  @Override
  public void reserveAbort(String connectionId, Holder<CommonHeaderType> header) throws ServiceException {
    NsiV2RequestDetails requestDetails = createRequestDetails(header.value);
    checkOAuthScope(NsiScope.RESERVE);

    log.info("Received Reserve Abort for connection: {}", connectionId);

    ConnectionV2 connection = getConnectionOrFail(connectionId);
    switch (connection.getReservationState()) {
    case RESERVE_HELD:
    case RESERVE_FAILED:
    case RESERVE_TIMEOUT:
      connectionService.asyncReserveAbort(connectionId, requestDetails, Security.getUserDetails());
      break;
    default:
      throw invalidTransition(connection.getLifecycleState(), connection.getReservationState(), connection.getProvisionState());
    }
  }

  @Override
  public void provision(String connectionId, Holder<CommonHeaderType> header) throws ServiceException {
    NsiV2RequestDetails requestDetails = createRequestDetails(header.value);
    checkOAuthScope(NsiScope.PROVISION);

    log.info("Received a Provision for connection: {}", connectionId);

    ConnectionV2 connection = getConnectionOrFail(connectionId);
    checkProvisionAllowed(connection);

    connectionService.asyncProvision(connectionId, requestDetails);
  }

  private void checkProvisionAllowed(ConnectionV2 connection) throws ServiceException {
    if (!(connection.getProvisionState().isPresent() && connection.getProvisionState().get() == ProvisionStateEnumType.RELEASED)) {
      throw invalidTransition(connection.getLifecycleState(), connection.getReservationState(), connection.getProvisionState());
    }
  }

  @Override
  public void release(String connectionId, Holder<CommonHeaderType> header) throws ServiceException {
    throw notImplemented();
  }

  @Override
  public void terminate(String connectionId, Holder<CommonHeaderType> header) throws ServiceException {
    NsiV2RequestDetails requestDetails = createRequestDetails(header.value);
    checkOAuthScope(NsiScope.TERMINATE);

    log.info("Received a Terminate for connection: {}", connectionId);

    ConnectionV2 connection = getConnectionOrFail(connectionId);
    checkTerminateAllowed(connection);

    connectionService.asyncTerminate(connectionId, requestDetails, Security.getUserDetails());
  }

  private void checkTerminateAllowed(ConnectionV2 connection) throws ServiceException {
    if (connection.getLifecycleState() == LifecycleStateEnumType.TERMINATING || connection.getLifecycleState() == LifecycleStateEnumType.TERMINATED) {
      throw invalidTransition(connection.getLifecycleState(), connection.getReservationState(), connection.getProvisionState());
    }
  }

  @Override
  public void querySummary(List<String> connectionIds, List<String> globalReservationIds, Holder<CommonHeaderType> header) throws ServiceException {
    NsiV2RequestDetails requestDetails = createRequestDetails(header.value);
    checkOAuthScope(NsiScope.QUERY);

    log.info("Received a Query Summary");

    connectionService.asyncQuerySummary(connectionIds, globalReservationIds, requestDetails);
  }

  @Override
  public void queryRecursive(List<String> connectionIds, List<String> globalReservationIds, Holder<CommonHeaderType> header) throws ServiceException {
    NsiV2RequestDetails requestDetails = createRequestDetails(header.value);

    checkOAuthScope(NsiScope.QUERY);

    log.info("Received a Query Recursive");

    connectionService.asyncQueryRecursive(connectionIds, globalReservationIds, requestDetails);
  }

  @Override
  public List<QuerySummaryResultType> querySummarySync(List<String> connectionIds, List<String> globalReservationIds, Holder<CommonHeaderType> header) throws Error {
    try {
      checkOAuthScope(NsiScope.QUERY);

      log.info("Received a Query Summary Sync");

      List<ConnectionV2> connections = connectionService.querySummary(connectionIds, globalReservationIds, header.value.getRequesterNSA());

      return transform(connections, toQuerySummaryResultType);
    } catch (ServiceException e) {
      throw toError(e);
    }
  }

  @Override
  public void queryNotification(String connectionId, Long startNotificationId, Long endNotificationId, Holder<CommonHeaderType> header) throws ServiceException {
    NsiV2RequestDetails requestDetails = createRequestDetails(header.value);

    checkOAuthScope(NsiScope.QUERY);

    log.info("Received a Query Notification async");

    connectionService.asyncQueryNotification(connectionId, Optional.fromNullable(startNotificationId),
        Optional.fromNullable(endNotificationId), requestDetails);
  }

  @Override
  public QueryNotificationConfirmedType queryNotificationSync(QueryNotificationType queryNotificationSync, Holder<CommonHeaderType> header) throws Error {
    try {
      NsiV2RequestDetails requestDetails = createRequestDetails(header.value);

      List<NotificationBaseType> notifications = connectionService.queryNotification(queryNotificationSync.getConnectionId(),
          Optional.fromNullable(queryNotificationSync.getStartNotificationId()),
          Optional.fromNullable(queryNotificationSync.getEndNotificationId()),
          requestDetails);

      return new QueryNotificationConfirmedType().withErrorEventOrReserveTimeoutOrDataPlaneStateChange(notifications);
    } catch (ServiceException e) {
      throw toError(e);
    }
  }

  @Override
  public void queryResult(String connectionId, Long startResultId, Long endResultId, Holder<CommonHeaderType> header)
          throws ServiceException {
    checkOAuthScope(NsiScope.QUERY);
    NsiV2RequestDetails requestDetails = createRequestDetails(header.value);
    connectionService.asyncQueryResult(connectionId, startResultId, endResultId, requestDetails);
  }

  @Override
  public List<QueryResultResponseType> queryResultSync(String connectionId, Long startResultId, Long endResultId, Holder<CommonHeaderType> header) throws Error {
    try {
      checkOAuthScope(NsiScope.QUERY);
      return connectionService.queryResult(connectionId, startResultId, endResultId);
    } catch (ServiceException e) {
      throw toError(e);
    }
  }


  private Error toError(ServiceException e) {
    return new Error(e.getMessage(), new GenericErrorType().withServiceException(e.getFaultInfo()), e);
  }

  private ConnectionV2 getConnectionOrFail(String connectionId) throws ServiceException {
    ConnectionV2 connection = connectionRepo.findByConnectionId(connectionId);
    if (connection == null) {
      throw connectionNotFoundServiceException(connectionId);
    }

    return connection;
  }

  private void checkOAuthScope(NsiScope scope) throws ServiceException {
    if (!Security.hasOauthScope(scope)) {
      throw unAuthorizedServiceException();
    }
  }

  private NsiV2RequestDetails createRequestDetails(CommonHeaderType header) throws ServiceException {
    // The @SchemaValidation annotiation does not validate the headers, so we have to manually validate everything here.
    if (header.getProtocolVersion() == null) {
      throw missingParameter("protocolVersion");
    }
    if (header.getCorrelationId() == null) {
      throw missingParameter("correlationId");
    }
    if (header.getRequesterNSA() == null) {
      throw missingParameter("requesterNSA");
    }
    if (header.getProviderNSA() == null) {
      throw missingParameter("providerNSA");
    }
    if (!nsiHelper.getProviderNsaV2().equals(header.getProviderNSA())) {
      throw unsupportedParameter("providerNSA", header.getProviderNSA());
    }

    Optional<URI> replyTo;
    if (header.getReplyTo() == null) {
      replyTo = Optional.absent();
    } else {
      replyTo = Optional.of(URI.create(header.getReplyTo()));
    }
    return new NsiV2RequestDetails(replyTo, header.getCorrelationId(), header.getRequesterNSA(), header.getProviderNSA());
  }

  private ServiceException missingParameter(String parameter) throws ServiceException {
    return new ServiceException("Missing parameter '" + parameter + "'",
      createServiceExceptionType(MISSING_PARAMETER).withVariables(new VariablesType().withVariable(new TypeValuePairType().withValue(parameter))));
  }

  private ServiceException invalidTransition(LifecycleStateEnumType lifecycleState, ReservationStateEnumType reservationState, Optional<ProvisionStateEnumType> provisionState) throws ServiceException {
    VariablesType states = new VariablesType().withVariable(
      new TypeValuePairType().withType("reservationState").withValue(reservationState.name()),
      new TypeValuePairType().withType("lifecycleState").withValue(lifecycleState.name()));

    if (provisionState.isPresent()) {
      states.withVariable(new TypeValuePairType().withType("provisionState").withValue(provisionState.get().name()));
    }

    return new ServiceException("This operation is not applicable",
      createServiceExceptionType(INVALID_TRANSITION).withVariables(states)
    );
  }

  private ServiceException unsupportedParameter(String attribute, Object value) {
    return new ServiceException(String.format("The attribute '%s' with value '%s' is not supported by this provider", attribute, value),
      createServiceExceptionType(UNSUPPORTED_PARAMETER)
        .withVariables(new VariablesType().withVariable(new TypeValuePairType().withType(attribute).withValue(value.toString())))
    );
  }

  private ServiceException notImplemented() {
    return new ServiceException("This operation is not supported by this provider", createServiceExceptionType(NOT_IMPLEMENTED));
  }

  private ServiceException connectionNotFoundServiceException(String connectionId) {
    return new ServiceException("Client asked for non-existing connection: " + connectionId,
      createServiceExceptionType(CONNECTION_NON_EXISTENT).withConnectionId(connectionId));
  }

  private ServiceException unAuthorizedServiceException() {
    return new ServiceException("Unauthorized", createServiceExceptionType(UNAUTHORIZED));
  }

  private ServiceExceptionType createServiceExceptionType(ConnectionServiceProviderError error) {
    return new ServiceExceptionType().withNsaId(nsiHelper.getProviderNsaV2()).withText(error.getMessage()).withErrorId(error.getErrorId());
  }

  private ReservationConfirmCriteriaType convertInitialRequestCriteria(ReservationRequestCriteriaType criteria) throws ServiceException {
    if (criteria.getSchedule() == null) {
      throw missingParameter("schedule");
    }
    if (criteria.getServiceType() == null) {
      throw missingParameter("serviceType");
    }
    if (!criteria.getServiceType().equals(bodEnvironment.getNsiV2ServiceType())) {
      throw unsupportedParameter("serviceType", criteria.getServiceType());
    }

    return new ReservationConfirmCriteriaType()
      .withAny(criteria.getAny())
      .withSchedule(criteria.getSchedule())
      .withServiceType(criteria.getServiceType())
      .withVersion(criteria.getVersion() == null ? 0 : criteria.getVersion());
  }

}
