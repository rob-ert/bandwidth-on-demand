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
package nl.surfnet.bod.nsi.v2;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.collect.Lists.transform;
import static nl.surfnet.bod.nsi.v2.ConnectionsV2.toQuerySummaryResultType;
import static nl.surfnet.bod.util.XmlUtils.xmlCalendarToDateTime;

import java.net.URI;
import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.xml.ws.Holder;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.sun.xml.ws.developer.SchemaValidation;

import nl.surfnet.bod.domain.ConnectionV2;
import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.domain.ProtectionType;
import nl.surfnet.bod.domain.oauth.NsiScope;
import nl.surfnet.bod.nsi.NsiHelper;
import nl.surfnet.bod.repo.ConnectionV2Repo;
import nl.surfnet.bod.service.ConnectionServiceV2;
import nl.surfnet.bod.service.ConnectionServiceV2.ValidationException;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.joda.time.DateTime;
import org.ogf.schemas.nsi._2013._04.connection.provider.ConnectionProviderPort;
import org.ogf.schemas.nsi._2013._04.connection.provider.QueryNotificationSyncFailed;
import org.ogf.schemas.nsi._2013._04.connection.provider.QuerySummarySyncFailed;
import org.ogf.schemas.nsi._2013._04.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2013._04.connection.types.ProvisionStateEnumType;
import org.ogf.schemas.nsi._2013._04.connection.types.QueryFailedType;
import org.ogf.schemas.nsi._2013._04.connection.types.QueryNotificationConfirmedType;
import org.ogf.schemas.nsi._2013._04.connection.types.QueryNotificationType;
import org.ogf.schemas.nsi._2013._04.connection.types.QuerySummaryResultType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReservationConfirmCriteriaType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReservationRequestCriteriaType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReservationStateEnumType;
import org.ogf.schemas.nsi._2013._04.framework.headers.CommonHeaderType;
import org.ogf.schemas.nsi._2013._04.framework.types.ServiceExceptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("connectionServiceProviderWs_v2")
@WebService(serviceName = "ConnectionServiceProvider", portName = "ConnectionServiceProviderPort", endpointInterface = "org.ogf.schemas.nsi._2013._04.connection.provider.ConnectionProviderPort", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/provider")
@SchemaValidation
public class ConnectionServiceProviderV2Ws implements ConnectionProviderPort {

  private final Logger log = LoggerFactory.getLogger(ConnectionServiceProviderV2Ws.class);

  @Resource private ConnectionServiceV2 connectionService;
  @Resource private ConnectionV2Repo connectionRepo;
  @Resource private Environment bodEnvironment;

  @Override
  public void reserve(Holder<String> connectionId, String globalReservationId, String description, ReservationRequestCriteriaType criteria,
      Holder<CommonHeaderType> header) throws ServiceException {
    NsiRequestDetails requestDetails = createRequestDetails(header.value);
    updateHeadersForReply(header);
    checkOAuthScope(NsiScope.RESERVE);

    log.info("Received a NSI v2 reserve request");

    if (!Strings.isNullOrEmpty(connectionId.value)) {
      throw notSupportedOperation();
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

  private void reserve(ConnectionV2 connection, NsiRequestDetails requestDetails, RichUserDetails richUserDetails) throws ServiceException {
    try {
      connectionService.reserve(connection, requestDetails, richUserDetails);
    } catch (ValidationException e) {
      ServiceExceptionType faultInfo = new ServiceExceptionType().withErrorId(e.getErrorCode()).withNsaId(bodEnvironment.getNsiProviderNsa())
          .withText(e.getMessage());

      throw new ServiceException(e.getMessage(), faultInfo, e);
    }
  }

  private ConnectionV2 createConnection(Optional<String> globalReservationId, Optional<String> description, NsiRequestDetails requestDetails,
      String providerNsa, String requesterNsa, ReservationConfirmCriteriaType criteria) {
    Optional<DateTime> startTime = fromNullable(criteria.getSchedule().getStartTime()).transform(xmlCalendarToDateTime);
    Optional<DateTime> endTime = fromNullable(criteria.getSchedule().getEndTime()).transform(xmlCalendarToDateTime);

    ConnectionV2 connection = new ConnectionV2();
    connection.setConnectionId(NsiHelper.generateConnectionId());
    connection.setGlobalReservationId(globalReservationId.or(NsiHelper.generateGlobalReservationId()));
    connection.setDescription(description.orNull());
    connection.setStartTime(startTime.orNull());
    connection.setEndTime(endTime.orNull());
    connection.setDesiredBandwidth(criteria.getBandwidth());
    connection.setProtectionType(ProtectionType.PROTECTED.name());
    connection.setProviderNsa(providerNsa);
    connection.setRequesterNsa(requesterNsa);
    connection.setProvisionRequestDetails(requestDetails);
    connection.setReserveVersion(criteria.getVersion());
    connection.setPath(criteria.getPath());
    connection.setServiceAttributes(criteria.getServiceAttributes());

    return connection;
  }

  @Override
  public void reserveCommit(String connectionId, Holder<CommonHeaderType> header) throws ServiceException {
    NsiRequestDetails requestDetails = createRequestDetails(header.value);
    updateHeadersForReply(header);
    checkOAuthScope(NsiScope.RESERVE);

    log.info("Received reserve commit request for connection: {}", connectionId);

    ConnectionV2 connection = getConnectionOrFail(connectionId);
    if (connection.getReservationState() != ReservationStateEnumType.RESERVE_HELD) {
      throw notApplicable();
    }
    connectionService.asyncReserveCommit(connectionId, requestDetails);
  }

  @Override
  public void reserveAbort(String connectionId, Holder<CommonHeaderType> header) throws ServiceException {
    NsiRequestDetails requestDetails = createRequestDetails(header.value);
    updateHeadersForReply(header);
    checkOAuthScope(NsiScope.RESERVE);

    log.info("Received Reserve Abort for connection: {}", connectionId);

    ConnectionV2 connection = getConnectionOrFail(connectionId);
    if (connection.getReservationState() != ReservationStateEnumType.RESERVE_HELD) {
      throw notApplicable();
    }

    connectionService.asyncReserveAbort(connectionId, requestDetails, Security.getUserDetails());
  }

  @Override
  public void provision(String connectionId, Holder<CommonHeaderType> header) throws ServiceException {
    NsiRequestDetails requestDetails = createRequestDetails(header.value);
    updateHeadersForReply(header);
    checkOAuthScope(NsiScope.PROVISION);

    log.info("Received a Provision for connection: {}", connectionId);

    ConnectionV2 connection = getConnectionOrFail(connectionId);
    checkProvisionAllowed(connection);

    connectionService.asyncProvision(connectionId, requestDetails);
  }

  private void checkProvisionAllowed(ConnectionV2 connection) throws ServiceException {
    if (!(connection.getProvisionState().isPresent() && connection.getProvisionState().get() == ProvisionStateEnumType.RELEASED)) {
      throw notApplicable();
    }
  }

  @Override
  public void release(String connectionId, Holder<CommonHeaderType> header) throws ServiceException {
    updateHeadersForReply(header);
    throw notSupportedOperation();
  }

  @Override
  public void terminate(String connectionId, Holder<CommonHeaderType> header) throws ServiceException {
    NsiRequestDetails requestDetails = createRequestDetails(header.value);
    updateHeadersForReply(header);
    checkOAuthScope(NsiScope.TERMINATE);

    log.info("Received a Terminate for connection: {}", connectionId);

    ConnectionV2 connection = getConnectionOrFail(connectionId);
    checkTerminateAllowed(connection);

    connectionService.asyncTerminate(connectionId, requestDetails, Security.getUserDetails());
  }

  private void checkTerminateAllowed(ConnectionV2 connection) throws ServiceException {
    if (!connection.getLifecycleState().isPresent()) {
      throw notApplicable();
    }
  }

  @Override
  public void querySummary(List<String> connectionIds, List<String> globalReservationIds, Holder<CommonHeaderType> header) throws ServiceException {
    NsiRequestDetails requestDetails = createRequestDetails(header.value);
    updateHeadersForReply(header);
    checkOAuthScope(NsiScope.QUERY);

    log.info("Received a Query Summary");

    connectionService.asyncQuerySummary(connectionIds, globalReservationIds, requestDetails);
  }

  @Override
  public void queryRecursive(List<String> connectionIds, List<String> globalReservationIds, Holder<CommonHeaderType> header) throws ServiceException {
    NsiRequestDetails requestDetails = createRequestDetails(header.value);

    updateHeadersForReply(header);
    checkOAuthScope(NsiScope.QUERY);

    log.info("Received a Query Recursive");

    connectionService.asyncQueryRecursive(connectionIds, globalReservationIds, requestDetails);
  }

  @Override
  public List<QuerySummaryResultType> querySummarySync(List<String> connectionIds, List<String> globalReservationIds, Holder<CommonHeaderType> header)
      throws QuerySummarySyncFailed {
    try {
      updateHeadersForReply(header);
      checkOAuthScope(NsiScope.QUERY);

      log.info("Received a Query Summary Sync");

      List<ConnectionV2> connections = connectionService.querySummarySync(connectionIds, globalReservationIds, header.value.getRequesterNSA());

      return transform(connections, toQuerySummaryResultType);
    } catch (ServiceException e) {
      throw toQuerySummarySyncFailed(e);
    }
  }

  private QuerySummarySyncFailed toQuerySummarySyncFailed(ServiceException e) {
    return new QuerySummarySyncFailed(e.getMessage(), new QueryFailedType().withServiceException(e.getFaultInfo()));
  }

  private ConnectionV2 getConnectionOrFail(String connectionId) throws ServiceException {
    ConnectionV2 connection = connectionRepo.findByConnectionId(connectionId);
    if (connection == null) {
      throw connectionNotFoundServiceException();
    }

    return connection;
  }

  private void checkOAuthScope(NsiScope scope) throws ServiceException {
    if (!Security.hasOauthScope(scope)) {
      throw unAuthorizedServiceException();
    }
  }

  private NsiRequestDetails createRequestDetails(CommonHeaderType header) {
    return new NsiRequestDetails(URI.create(header.getReplyTo()), header.getCorrelationId(), header.getRequesterNSA(), header.getProviderNSA());
  }

  private ServiceException missingParameter(String parameter) throws ServiceException {
    return new ServiceException("Missing parameter '" + parameter + "'", createServiceExceptionType("Missing parameter '" + parameter + "'"));
  }

  private ServiceException notApplicable() throws ServiceException {
    return new ServiceException("This operation is not applicable", createServiceExceptionType("Not Applicable"));
  }

  private ServiceException notSupportedOperation() {
    return new ServiceException("This operation is not supported by this provider", createServiceExceptionType("Not Supported"));
  }

  private ServiceException connectionNotFoundServiceException() {
    return new ServiceException("Could not find connection", createServiceExceptionType("Not found"));
  }

  private ServiceException unAuthorizedServiceException() {
    return new ServiceException("Unauthorized", createServiceExceptionType("Unauthorized"));
  }

  private ServiceExceptionType createServiceExceptionType(String message) {
    return new ServiceExceptionType().withNsaId(bodEnvironment.getNsiProviderNsa()).withErrorId("0100").withText(message);
  }

  private ReservationConfirmCriteriaType convertInitialRequestCriteria(ReservationRequestCriteriaType criteria) throws ServiceException {
    if (criteria.getBandwidth() == null) {
      throw missingParameter("bandwidth");
    }
    if (criteria.getPath() == null) {
      throw missingParameter("path");
    }
    if (criteria.getSchedule() == null) {
      throw missingParameter("schedule");
    }
    if (criteria.getServiceAttributes() == null) {
      throw missingParameter("serviceAttributes");
    }

    return new ReservationConfirmCriteriaType().withBandwidth(criteria.getBandwidth()).withPath(criteria.getPath())
        .withSchedule(criteria.getSchedule()).withServiceAttributes(criteria.getServiceAttributes())
        .withVersion(criteria.getVersion() == null ? 0 : criteria.getVersion());
  }

  private void updateHeadersForReply(Holder<CommonHeaderType> header) {
    header.value.setReplyTo(null);
  }

  @Override
  @WebMethod(action = "http://schemas.ogf.org/nsi/2013/04/connection/service/queryNotification")
  @RequestWrapper(localName = "queryNotification", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/types", className = "org.ogf.schemas.nsi._2013._04.connection.types.QueryNotificationType")
  @ResponseWrapper(localName = "acknowledgment", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/types", className = "org.ogf.schemas.nsi._2013._04.connection.types.GenericAcknowledgmentType")
  public void queryNotification(
      @WebParam(name = "connectionId", targetNamespace = "") String connectionId,
      @WebParam(name = "startNotificationId", targetNamespace = "") Integer startNotificationId,
      @WebParam(name = "endNotificationId", targetNamespace = "") Integer endNotificationId,
      @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = Mode.INOUT, partName = "header") Holder<CommonHeaderType> header)
      throws ServiceException {
    // TODO Auto-generated method stub

  }

  @Override
  @WebMethod(action = "http://schemas.ogf.org/nsi/2013/04/connection/service/queryNotificationSync")
  @WebResult(name = "queryNotificationSyncConfirmed", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/types", partName = "queryNotificationSyncConfirmed")
  @SOAPBinding(parameterStyle = ParameterStyle.BARE)
  public QueryNotificationConfirmedType queryNotificationSync(
      @WebParam(name = "queryNotificationSync", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/connection/types", partName = "queryNotificationSync") QueryNotificationType queryNotificationSync,
      @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/04/framework/headers", header = true, mode = Mode.INOUT, partName = "header") Holder<CommonHeaderType> header)
      throws QueryNotificationSyncFailed {
    // TODO Auto-generated method stub
    return null;
  }
}
