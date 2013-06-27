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
package nl.surfnet.bod.nbi;

import static com.google.common.base.Preconditions.checkNotNull;
import static nl.surfnet.bod.domain.ReservationStatus.*;
import static org.joda.time.Minutes.minutes;
import static org.joda.time.Minutes.minutesBetween;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentMap;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.nbi.generated.NetworkMonitoringServiceFault;
import nl.surfnet.bod.nbi.generated.NetworkMonitoringService_v30Stub;
import nl.surfnet.bod.nbi.generated.ResourceAllocationAndSchedulingServiceFault;
import nl.surfnet.bod.nbi.generated.ResourceAllocationAndSchedulingService_v30Stub;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd.Security;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd.SecurityDocument;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd.UsernameToken;
import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.*;
import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointRequestDocument.QueryEndpointRequest;
import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointsRequestDocument.QueryEndpointsRequest;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.*;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ActivateReservationOccurrenceRequestDocument.ActivateReservationOccurrenceRequest;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.CancelReservationScheduleRequestDocument.CancelReservationScheduleRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nortel.www.drac._2007._07._03.ws.ct.draccommontypes.CompletionResponseDocument;
import com.nortel.www.drac._2007._07._03.ws.ct.draccommontypes.ValidCompletionTypeT;
import com.nortel.www.drac._2007._07._03.ws.ct.draccommontypes.ValidLayerT;

/**
 * A bridge to OpenDRAC's web services. Everything is contained in this one
 * class so that only this class is linked to OpenDRAC related classes.
 *
 */
public class NbiOpenDracWsClient implements NbiClient {

  private static final Minutes RAASS_TIMEOUT = minutes(10);
  private static final String ROUTING_ALGORITHM = "VCAT";
  private static final String DEFAULT_VID = "Untagged";
  private static final Minutes MAX_DURATION = Minutes.MAX_VALUE;

  private static final String CONNECTION_REFUSED_LOWER_CASE_MESSAGE = "connection refused";
  private static final String AUTHENTICATION_FAILED_LOWER_CASE_MESSAGE = "authentication check failed";

  private final HttpClient httpClient;

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final ConcurrentMap<String, String> idToTnaCache = Maps.newConcurrentMap();

  @Value("${nbi.drac.billing.group.name}")
  private String billingGroupName;

  @Value("${nbi.drac.password}")
  private String password;

  @Value("${nbi.drac.group.name}")
  private String groupName;

  @Value("${nbi.drac.resource.group.name}")
  private String resourceGroupName;

  @Value("${nbi.drac.user}")
  private String username;

  @Value("${nbi.drac.service.inventory}")
  private String inventoryServiceUrl;

  @Value("${nbi.drac.service.scheduling}")
  private String schedulingServiceUrl;

  public NbiOpenDracWsClient() {
    HttpConnectionManagerParams params = new HttpConnectionManagerParams();
    params.setDefaultMaxConnectionsPerHost(20);
    params.setConnectionTimeout(5000);
    MultiThreadedHttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
    httpConnectionManager.setParams(params);
    httpClient = new HttpClient(httpConnectionManager);
  }

  @Override
  public boolean activateReservation(final String reservationId) {
    checkNotNull(reservationId);

    Optional<String> serviceId = findServiceId(reservationId);

    if (serviceId.isPresent()) {
      try {
        ActivateReservationOccurrenceRequestDocument requestDocument = createActivateReservationOccurrenceRequest(serviceId.get());
        CompletionResponseDocument responseDocument = getResourceAllocationAndSchedulingService()
            .activateReservationOccurrence(requestDocument, getSecurityDocument());

        return responseDocument.getCompletionResponse().getResult() == ValidCompletionTypeT.SUCCESS;
      }
      catch (ResourceAllocationAndSchedulingServiceFault | RemoteException e) {
        log.error("Error activating reservation (" + reservationId + "): ", e);
      }
    }

    return false;
  }

  private ActivateReservationOccurrenceRequestDocument createActivateReservationOccurrenceRequest(String serviceId) {
    ActivateReservationOccurrenceRequestDocument activateReservationOccurrenceRequestDocument = ActivateReservationOccurrenceRequestDocument.Factory
        .newInstance();

    ActivateReservationOccurrenceRequest activateReservationOccurrenceRequest = activateReservationOccurrenceRequestDocument
        .addNewActivateReservationOccurrenceRequest();
    activateReservationOccurrenceRequest.setOccurrenceId(serviceId);

    return activateReservationOccurrenceRequestDocument;
  }

  @Override
  public ReservationStatus cancelReservation(final String reservationId) {
    checkNotNull(reservationId);

    try {
      getResourceAllocationAndSchedulingService()
        .cancelReservationSchedule(createCancelReservationScheduleRequest(reservationId), getSecurityDocument());

      // CompletionResponseDocument always signals that the operation executed successfully.
      return CANCELLED;
    }
    catch (ResourceAllocationAndSchedulingServiceFault | RemoteException e) {
      log.error("Error canceling reservation (" + reservationId + "): ", e);
      return CANCEL_FAILED;
    }

  }

  private CancelReservationScheduleRequestDocument createCancelReservationScheduleRequest(final String reservationId) {
    CancelReservationScheduleRequestDocument requestDocument =
      CancelReservationScheduleRequestDocument.Factory.newInstance();

    CancelReservationScheduleRequest request = requestDocument.addNewCancelReservationScheduleRequest();
    request.setReservationScheduleId(reservationId);

    return requestDocument;
  }

  @Override
  public Reservation createReservation(Reservation reservation, boolean autoProvision) {
    CreateReservationScheduleRequestDocument requestDocument =
      createReservationScheduleRequest(reservation, autoProvision);

    try {
      CreateReservationScheduleResponseDocument responseDocument = getResourceAllocationAndSchedulingService()
          .createReservationSchedule(requestDocument, getSecurityDocument());

      log.debug("Create reservation response: {}", responseDocument.getCreateReservationScheduleResponse());

      String reservationId = responseDocument.getCreateReservationScheduleResponse().getReservationScheduleId();

      ReservationStatus status = OpenDracStatusTranslator.translate(responseDocument
          .getCreateReservationScheduleResponse().getResult(), autoProvision);

      if (status.isErrorState()) {
        String failedReason = composeFailedReason(responseDocument);

        reservation.setFailedReason(failedReason);

        log.info("Create reservation ({}) failed with '{}'", reservationId, failedReason);
      }

      reservation.setReservationId(reservationId);
      reservation.setStatus(status);
    } catch (ResourceAllocationAndSchedulingServiceFault e) {
      log.warn("Creating a reservation failed", e);
      reservation.setFailedReason(e.getMessage().trim());
      reservation.setStatus(NOT_ACCEPTED);
    } catch (RemoteException e) {
      log.error("Unexpected exception while requesting reservation from OpenDRAC", e);
      reservation.setFailedReason(e.getMessage().trim());
      reservation.setStatus(FAILED);
    }

    return reservation;
  }

  private String composeFailedReason(CreateReservationScheduleResponseDocument responseDocument) {
    List<String> reasons = Lists.newArrayList();
    for (ReservationOccurrenceInfoT occurenceInfo : responseDocument.getCreateReservationScheduleResponse()
        .getOccurrenceInfoArray()) {
      reasons.add(occurenceInfo.getReason().trim());
    }

    return Joiner.on(", ").join(reasons);
  }

  @Override
  public List<PhysicalPort> findAllPhysicalPorts() {
    List<PhysicalPort> ports = Lists.newArrayList();

    for (EndpointT endpoint : findAllEndPoints()) {
      ports.add(getPhysicalPort(endpoint));
    }

    return ports;
  }

  @Override
  public PhysicalPort findPhysicalPortByNmsPortId(final String nmsPortId) throws PortNotAvailableException {
    EndpointT endpoint = findEndPointById(nmsPortId);
    return getPhysicalPort(endpoint);
  }

  /**
   * Find by id is a little more complex, because the webservice only supports a
   * lookup by tna and not by NMS port id. In BoD the tna is locally stored as the
   * nocLabel field. This field can be edited. The tna can also be edited in
   * OpenDRAC. So the only save thing todo is use the id. The service keeps a
   * chache of id to tna.
   */
  private EndpointT findEndPointById(String id) throws PortNotAvailableException {
    String tna = idToTnaCache.get(id);

    if (tna == null) {
      List<EndpointT> endPoints = findAllEndPoints();
      for (EndpointT endPoint : endPoints) {
        if (endPoint.getId().equals(id)) {
          return endPoint;
        }
      }
      throw new PortNotAvailableException(id);
    }
    else {
      EndpointT endPoint = findEndpointByTna(tna);

      if (endPoint.getId().equals(id)) {
        return endPoint;
      }
      else {
        idToTnaCache.remove(id);
        return findEndPointById(id);
      }
    }
  }

  private String findTnaById(String id) throws PortNotAvailableException {
    String tna = idToTnaCache.get(id);
    if (tna == null) {
      tna = findEndPointById(id).getTna();
    }

    return tna;
  }

  @Override
  public long getPhysicalPortsCount() {
    return findAllPhysicalPorts().size();
  }

  private Optional<QueryReservationScheduleResponseDocument> queryReservation(String reservationId) {
    try {
      QueryReservationScheduleRequestDocument requestDocument = createQueryReservationScheduleRequest(reservationId);
      QueryReservationScheduleResponseDocument queryReservationSchedule = getResourceAllocationAndSchedulingService().queryReservationSchedule(requestDocument,
          getSecurityDocument());
      return Optional.of(queryReservationSchedule);
    }
    catch (AxisFault e) {
      log.error("Error querying reservation (" + reservationId + "): ", e);

      String errorMessageToLowerCase = e.getMessage().toLowerCase();

      // no connection to nms
      if (errorMessageToLowerCase.contains(CONNECTION_REFUSED_LOWER_CASE_MESSAGE)) {
        log.warn("Connection refused to {}", schedulingServiceUrl);
      }
      else {
        // invalid credentials
        if (errorMessageToLowerCase.contains(AUTHENTICATION_FAILED_LOWER_CASE_MESSAGE)) {
          log.warn("Authentication check failed for user {} and resource group {}", username, resourceGroupName);
        }
      }

    }
    catch (ResourceAllocationAndSchedulingServiceFault | RemoteException e) {
      log.error("Error querying reservation (" + reservationId + "): ", e);
    }

    return Optional.absent();
  }

  @Override
  public Optional<ReservationStatus> getReservationStatus(final String reservationId) {
    Optional<QueryReservationScheduleResponseDocument> responseDocument = queryReservation(reservationId);

    return responseDocument.transform(new Function<QueryReservationScheduleResponseDocument, ReservationStatus>() {
      @Override
      public ReservationStatus apply(QueryReservationScheduleResponseDocument response) {
        if (response.getQueryReservationScheduleResponse().getIsFound()) {
          ReservationScheduleT schedule = response.getQueryReservationScheduleResponse().getReservationSchedule();
          return OpenDracStatusTranslator.translate(schedule);
        }
        else {
          log.warn("No reservation found for reservationId: {}, returning FAILED", reservationId);
          return FAILED;
        }
      }
    });
  }

  private QueryReservationScheduleRequestDocument createQueryReservationScheduleRequest(String reservationId) {
    QueryReservationScheduleRequestDocument requestDocument = QueryReservationScheduleRequestDocument.Factory
        .newInstance();
    requestDocument.addNewQueryReservationScheduleRequest();
    requestDocument.getQueryReservationScheduleRequest().setReservationScheduleId(reservationId);

    return requestDocument;
  }

  protected static final class OpenDracStatusTranslator {
    private static ImmutableMap<ValidReservationScheduleCreationResultT.Enum, ReservationStatus> creationAutoProvionResultTranslations = new ImmutableMap.Builder<ValidReservationScheduleCreationResultT.Enum, ReservationStatus>()
        .put(ValidReservationScheduleCreationResultT.FAILED, NOT_ACCEPTED).put(
            ValidReservationScheduleCreationResultT.SUCCEEDED, AUTO_START).put(
            ValidReservationScheduleCreationResultT.SUCCEEDED_PARTIALLY, AUTO_START).put(
            ValidReservationScheduleCreationResultT.UNKNOWN, FAILED).build();

    private static ImmutableMap<ValidReservationScheduleCreationResultT.Enum, ReservationStatus> creationResultTranslations = new ImmutableMap.Builder<ValidReservationScheduleCreationResultT.Enum, ReservationStatus>()
        .put(ValidReservationScheduleCreationResultT.FAILED, NOT_ACCEPTED).put(
            ValidReservationScheduleCreationResultT.SUCCEEDED, RESERVED).put(
            ValidReservationScheduleCreationResultT.SUCCEEDED_PARTIALLY, RESERVED).put(
            ValidReservationScheduleCreationResultT.UNKNOWN, FAILED).build();

    private static ImmutableMap<ValidReservationScheduleStatusT.Enum, ReservationStatus> scheduleStatusTranslations = new ImmutableMap.Builder<ValidReservationScheduleStatusT.Enum, ReservationStatus>()
        .put(ValidReservationScheduleStatusT.CONFIRMATION_PENDING, REQUESTED).put(
            ValidReservationScheduleStatusT.CONFIRMATION_TIMED_OUT, FAILED).put(
            ValidReservationScheduleStatusT.CONFIRMATION_CANCELLED, CANCELLED).put(
            ValidReservationScheduleStatusT.EXECUTION_IN_PROGRESS, RUNNING).put(
            ValidReservationScheduleStatusT.EXECUTION_SUCCEEDED, SUCCEEDED).put(
            ValidReservationScheduleStatusT.EXECUTION_PARTIALLY_SUCCEEDED, FAILED).put(
            ValidReservationScheduleStatusT.EXECUTION_TIMED_OUT, TIMED_OUT).put(
            ValidReservationScheduleStatusT.EXECUTION_FAILED, FAILED).put(
            ValidReservationScheduleStatusT.EXECUTION_PARTIALLY_CANCELLED, CANCELLED).put(
            ValidReservationScheduleStatusT.EXECUTION_CANCELLED, CANCELLED).build();

    private OpenDracStatusTranslator() {
    }

    public static ReservationStatus translate(ValidReservationScheduleCreationResultT.Enum status, boolean autoProvision) {
      if (autoProvision) {
        return creationAutoProvionResultTranslations.get(status);
      }

      return creationResultTranslations.get(status);
    }

    public static ReservationStatus translate(ReservationScheduleT reservationSchedule) {
      ValidReservationScheduleStatusT.Enum status = reservationSchedule.getStatus();

      if (status.equals(ValidReservationScheduleStatusT.EXECUTION_PENDING)) {
        boolean isActivated = reservationSchedule.getActivated();
        return isActivated ? AUTO_START
            : new DateTime(reservationSchedule.getStartTime()).isAfterNow() ? RESERVED : SCHEDULED;
      }
      else {
        return scheduleStatusTranslations.get(status);
      }
    }
  }

  @VisibleForTesting
  CreateReservationScheduleRequestDocument createReservationScheduleRequest(
      Reservation reservation, boolean autoProvision) {

    CreateReservationScheduleRequestDocument requestDocument =
      CreateReservationScheduleRequestDocument.Factory.newInstance();

    ReservationScheduleRequestT schedule =
      requestDocument.addNewCreateReservationScheduleRequest().addNewReservationSchedule();

    ValidReservationScheduleTypeT.Enum type = autoProvision ? ValidReservationScheduleTypeT.RESERVATION_SCHEDULE_AUTOMATIC
        : ValidReservationScheduleTypeT.RESERVATION_SCHEDULE_MANUAL;
    Calendar startTime = reservation.getStartDateTime().toCalendar(Locale.getDefault());
    Minutes duration = reservation.getEndDateTime() == null ? MAX_DURATION : minutesBetween(reservation
        .getStartDateTime(), reservation.getEndDateTime());

    schedule.setName(reservation.getUserCreated() + "-" + System.currentTimeMillis());
    schedule.setType(type);
    schedule.setStartTime(startTime);
    schedule.setReservationOccurrenceDuration(duration.getMinutes());
    schedule.setIsRecurring(false);
    schedule.setPath(createPath(reservation));
    schedule.setUserInfo(createUser());

    return requestDocument;
  }

  private Optional<String> findServiceId(String reservationId) {
    Optional<QueryReservationScheduleResponseDocument> responseDocument = queryReservation(reservationId);

    Optional<Optional<String>> serviceId = responseDocument
        .transform(new Function<QueryReservationScheduleResponseDocument, Optional<String>>() {
          @Override
          public Optional<String> apply(QueryReservationScheduleResponseDocument response) {
            if (response.getQueryReservationScheduleResponse().getIsFound()) {
              return Optional.of(response.getQueryReservationScheduleResponse().getReservationSchedule()
                  .getOccurrenceIdArray(0));
            }
            return Optional.absent();
          }
        });

    return flatten(serviceId);
  }

  private static <T> Optional<T> flatten(Optional<Optional<T>> optional) {
    return optional.isPresent() ? optional.get() : Optional.<T> absent();
  }

  private UserInfoT createUser() {
    UserInfoT userInfo = UserInfoT.Factory.newInstance();
    userInfo.setBillingGroup(billingGroupName);
    userInfo.setSourceEndpointResourceGroup(resourceGroupName);
    userInfo.setSourceEndpointUserGroup(groupName);
    userInfo.setTargetEndpointResourceGroup(resourceGroupName);
    userInfo.setTargetEndpointUserGroup(groupName);
    return userInfo;
  }

  @VisibleForTesting
  protected PathRequestT createPath(Reservation reservation) {
    VirtualPort virtualSourcePort = reservation.getSourcePort();
    VirtualPort virtualDestinationPort = reservation.getDestinationPort();

    String sourceTna;
    String destinationTna;
    try {
      sourceTna = findTnaById(virtualSourcePort.getPhysicalPort().getNmsPortId());
      destinationTna = findTnaById(virtualDestinationPort.getPhysicalPort().getNmsPortId());
    }
    catch (PortNotAvailableException e) {
      throw new IllegalStateException(e);
    }

    PathRequestT pathRequest = PathRequestT.Factory.newInstance();
    pathRequest.setSourceTna(sourceTna);
    pathRequest.setTargetTna(destinationTna);
    pathRequest.setRate(reservation.getBandwidth());
    pathRequest.setSourceVlanId(translateVlanId(virtualSourcePort));
    pathRequest.setTargetVlanId(translateVlanId(virtualDestinationPort));
    pathRequest.setRoutingAlgorithm(ROUTING_ALGORITHM);

    switch (reservation.getProtectionType()) {
    case PROTECTED:
      pathRequest.setProtectionType(ValidProtectionTypeT.X_1_PLUS_1_PATH);
      break;
    case UNPROTECTED:
      pathRequest.setProtectionType(ValidProtectionTypeT.UNPROTECTED);
      break;
    default:
      throw new IllegalStateException("Unknown protection type: " + reservation.getProtectionType());
    }

    return pathRequest;
  }

  private PhysicalPort getPhysicalPort(EndpointT endpoint) {
    PhysicalPort port = new PhysicalPort(isVlanRequired(endpoint.getTna()));

    if (endpoint.getUserLabel() == null || endpoint.getUserLabel().isEmpty()) {
      port.setNocLabel(endpoint.getTna());
    }
    else {
      port.setNocLabel(endpoint.getUserLabel());
    }
    port.setNmsPortId(endpoint.getId());
    port.setBodPortId(endpoint.getTna());

    return port;
  }

  /**
   * @return true when a VlanId is required for this port. This is only the case
   *         when the tna of the port contains NOT
   *         {@link NbiClient#VLAN_REQUIRED_SELECTOR}
   */
  @VisibleForTesting
  boolean isVlanRequired(String tna) {
    return tna == null ? false : !tna.toLowerCase().contains(VLAN_REQUIRED_SELECTOR);
  }

  private String translateVlanId(VirtualPort virtualPort) {
    return virtualPort.getVlanId() == null ? DEFAULT_VID : virtualPort.getVlanId().toString();
  }

  private List<EndpointT> findAllEndPoints() {
    try {
      QueryEndpointsRequestDocument requestDocument = createQueryEndpointsRequest();
      QueryEndpointsResponseDocument response = getNetworkMonitoringService().queryEndpoints(requestDocument,
          getSecurityDocument());

      log.debug("Find all endpoints response: {}", response);

      List<EndpointT> endPoints = Lists.newArrayList();
      for (String tna : response.getQueryEndpointsResponse().getTnaArray()) {
        try {
          endPoints.add(findEndpointByTna(tna));
        }
        catch (PortNotAvailableException e) {
          log.error("Unable to find endpoint for TNA {}", tna);
        }
      }

      return endPoints;
    }
    catch (NetworkMonitoringServiceFault | RemoteException e) {
      log.warn("Could not query openDRAC for end points", e);
      throw new RuntimeException(e);
    }
  }

  private QueryEndpointsRequestDocument createQueryEndpointsRequest() {
    QueryEndpointsRequestDocument requestDocument = QueryEndpointsRequestDocument.Factory.newInstance();
    QueryEndpointsRequest request = requestDocument.addNewQueryEndpointsRequest();
    request.setUserGroup(groupName);
    request.setLayer(ValidLayerT.LAYER_2);
    request.setType(ValidEndpointsQueryTypeT.QUERY_ENDPOINTS_BY_LAYER_AND_USER_GROUP_T);

    return requestDocument;
  }

  private EndpointT findEndpointByTna(final String tna) throws PortNotAvailableException {
    try {
      QueryEndpointRequestDocument requestDocument = createQueryEndpointRequest(tna);
      QueryEndpointResponseDocument response = getNetworkMonitoringService().queryEndpoint(requestDocument,
          getSecurityDocument());

      EndpointT endpointFound = response.getQueryEndpointResponse().getEndpoint();

      if (endpointFound == null) {
        throw new PortNotAvailableException(tna);
      }

      idToTnaCache.put(endpointFound.getId(), endpointFound.getTna());

      return endpointFound;
    }
    catch (NetworkMonitoringServiceFault | RemoteException e) {
      log.warn("Can query openDrac for end point by tna", e);
      throw new RuntimeException(e);
    }
  }

  private QueryEndpointRequestDocument createQueryEndpointRequest(String tna) {
    QueryEndpointRequestDocument requestDocument = QueryEndpointRequestDocument.Factory.newInstance();
    QueryEndpointRequest request = requestDocument.addNewQueryEndpointRequest();
    request.setTna(tna);

    return requestDocument;
  }

  private SecurityDocument getSecurityDocument() {
    SecurityDocument document = SecurityDocument.Factory.newInstance();

    Security security = document.addNewSecurity();
    UsernameToken token = security.addNewUsernameToken();
    token.setUsername(username);
    token.setPassword(password);

    return document;
  }

  protected NetworkMonitoringService_v30Stub getNetworkMonitoringService() throws AxisFault {
    NetworkMonitoringService_v30Stub networkMonitoringService = new NetworkMonitoringService_v30Stub(inventoryServiceUrl);
    networkMonitoringService._getServiceClient().getOptions().setProperty(HTTPConstants.CACHED_HTTP_CLIENT, httpClient);

    return networkMonitoringService;
  }

  protected ResourceAllocationAndSchedulingService_v30Stub getResourceAllocationAndSchedulingService() throws AxisFault {
    ResourceAllocationAndSchedulingService_v30Stub schedulingService = new ResourceAllocationAndSchedulingService_v30Stub(
        schedulingServiceUrl);

    Options options = schedulingService._getServiceClient().getOptions();
    options.setTimeOutInMilliSeconds(RAASS_TIMEOUT.toStandardDuration().getMillis());
    options.setProperty(HTTPConstants.CACHED_HTTP_CLIENT, httpClient);

    return schedulingService;
  }

  void setPassword(String password) {
    this.password = password;
  }

  void setUsername(String username) {
    this.username = username;
  }

  void setInventoryServiceUrl(String inventoryServiceUrl) {
    this.inventoryServiceUrl = inventoryServiceUrl;
  }

}