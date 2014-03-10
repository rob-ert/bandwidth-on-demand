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
package nl.surfnet.bod.nbi.opendrac;

import static com.google.common.base.Preconditions.checkNotNull;
import static nl.surfnet.bod.domain.ReservationStatus.AUTO_START;
import static nl.surfnet.bod.domain.ReservationStatus.CANCELLED;
import static nl.surfnet.bod.domain.ReservationStatus.FAILED;
import static nl.surfnet.bod.domain.ReservationStatus.NOT_ACCEPTED;
import static nl.surfnet.bod.domain.ReservationStatus.PASSED_END_TIME;
import static nl.surfnet.bod.domain.ReservationStatus.REQUESTED;
import static nl.surfnet.bod.domain.ReservationStatus.RESERVED;
import static nl.surfnet.bod.domain.ReservationStatus.RUNNING;
import static nl.surfnet.bod.domain.ReservationStatus.SCHEDULED;
import static nl.surfnet.bod.domain.ReservationStatus.SUCCEEDED;
import static org.joda.time.Minutes.minutes;
import static org.joda.time.Minutes.minutesBetween;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nortel.www.drac._2007._07._03.ws.ct.draccommontypes.CompletionResponseDocument;
import com.nortel.www.drac._2007._07._03.ws.ct.draccommontypes.ValidCompletionTypeT;
import com.nortel.www.drac._2007._07._03.ws.ct.draccommontypes.ValidLayerT;

import nl.surfnet.bod.domain.NbiPort;
import nl.surfnet.bod.domain.NbiPort.InterfaceType;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationEndPoint;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.domain.UpdatedReservationStatus;
import nl.surfnet.bod.nbi.NbiClient;
import nl.surfnet.bod.nbi.PortNotAvailableException;
import nl.surfnet.bod.nbi.generated.NetworkMonitoringServiceFault;
import nl.surfnet.bod.nbi.generated.NetworkMonitoringService_v30Stub;
import nl.surfnet.bod.nbi.generated.ResourceAllocationAndSchedulingServiceFault;
import nl.surfnet.bod.nbi.generated.ResourceAllocationAndSchedulingService_v30Stub;
import nl.surfnet.bod.repo.ReservationRepo;
import nl.surfnet.bod.service.ReservationService;

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
import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.EndpointT;
import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointRequestDocument;
import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointRequestDocument.QueryEndpointRequest;
import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointResponseDocument;
import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointsRequestDocument;
import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointsRequestDocument.QueryEndpointsRequest;
import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointsResponseDocument;
import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.ValidEndpointsQueryTypeT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ActivateReservationOccurrenceRequestDocument;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ActivateReservationOccurrenceRequestDocument.ActivateReservationOccurrenceRequest;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.CancelReservationScheduleRequestDocument;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.CancelReservationScheduleRequestDocument.CancelReservationScheduleRequest;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.CreateReservationScheduleRequestDocument;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.CreateReservationScheduleResponseDocument;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.PathRequestT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.QueryReservationScheduleRequestDocument;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.QueryReservationScheduleResponseDocument;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ReservationOccurrenceInfoT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ReservationScheduleRequestT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ReservationScheduleT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.UserInfoT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ValidProtectionTypeT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ValidReservationScheduleCreationResultT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ValidReservationScheduleStatusT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ValidReservationScheduleTypeT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * A bridge to OpenDRAC's web services. Everything is contained in this one
 * class so that only this class is linked to OpenDRAC related classes.
 *
 */
@Profile("opendrac")
@Component
public class NbiOpenDracWsClient implements NbiClient {

  private static final Minutes RAASS_TIMEOUT = minutes(10);
  private static final String DEFAULT_VID = "Untagged";
  private static final Minutes MAX_DURATION = Minutes.MAX_VALUE;

  private static final String CONNECTION_REFUSED_LOWER_CASE_MESSAGE = "connection refused";
  private static final String AUTHENTICATION_FAILED_LOWER_CASE_MESSAGE = "authentication check failed";

  private final HttpClient httpClient;
  private final Logger log = LoggerFactory.getLogger(getClass());
  private final ConcurrentMap<String, String> idToTnaCache = Maps.newConcurrentMap();

  private final ReservationRepo reservationRepo;
  private final ReservationService reservationService;

  @Value("${nbi.opendrac.billing.group.name}") private String billingGroupName;
  @Value("${nbi.opendrac.password}") private String password;
  @Value("${nbi.opendrac.group.name}") private String groupName;
  @Value("${nbi.opendrac.resource.group.name}") private String resourceGroupName;
  @Value("${nbi.opendrac.user}") private String username;
  @Value("${nbi.opendrac.service.inventory}") private String inventoryServiceUrl;
  @Value("${nbi.opendrac.service.scheduling}") private String schedulingServiceUrl;
  @Value("${nbi.opendrac.routing.algorithm}") private String routingAlgorithm;

  @Autowired
  public NbiOpenDracWsClient(ReservationRepo reservationRepo, ReservationService reservationService) {
    this.reservationRepo = reservationRepo;
    this.reservationService = reservationService;
    HttpConnectionManagerParams params = new HttpConnectionManagerParams();
    params.setDefaultMaxConnectionsPerHost(20);
    params.setConnectionTimeout(5000);
    MultiThreadedHttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
    httpConnectionManager.setParams(params);
    httpClient = new HttpClient(httpConnectionManager);
  }

  @Override
  public void activateReservation(final String reservationId) {
    checkNotNull(reservationId);

    Optional<String> serviceId = findServiceId(reservationId);

    if (serviceId.isPresent()) {
      try {
        ActivateReservationOccurrenceRequestDocument requestDocument = createActivateReservationOccurrenceRequest(serviceId.get());
        CompletionResponseDocument responseDocument = getResourceAllocationAndSchedulingService()
            .activateReservationOccurrence(requestDocument, getSecurityDocument());

        if (responseDocument.getCompletionResponse().getResult() == ValidCompletionTypeT.SUCCESS) {
          reservationService.updateStatus(reservationId, UpdatedReservationStatus.forNewStatus(ReservationStatus.AUTO_START));
        }
      } catch (ResourceAllocationAndSchedulingServiceFault | RemoteException e) {
        log.error("Error activating reservation (" + reservationId + "): ", e);
      }
    }
  }

  private ActivateReservationOccurrenceRequestDocument createActivateReservationOccurrenceRequest(String serviceId) {
    ActivateReservationOccurrenceRequestDocument requestDocument = ActivateReservationOccurrenceRequestDocument.Factory.newInstance();
    ActivateReservationOccurrenceRequest activateReservationOccurrenceRequest = requestDocument.addNewActivateReservationOccurrenceRequest();
    activateReservationOccurrenceRequest.setOccurrenceId(serviceId);

    return requestDocument;
  }

  @Override
  public void cancelReservation(final String reservationId) {
    checkNotNull(reservationId);

    try {
      getResourceAllocationAndSchedulingService().cancelReservationSchedule(createCancelReservationScheduleRequest(reservationId), getSecurityDocument());
      reservationService.updateStatus(reservationId, UpdatedReservationStatus.forNewStatus(ReservationStatus.CANCELLED));
    } catch (ResourceAllocationAndSchedulingServiceFault | RemoteException e) {
      String message = "Error canceling reservation (" + reservationId + "): " + e;
      log.info(message, e);
      reservationService.updateStatus(reservationId, UpdatedReservationStatus.cancelFailed(message));
    }

  }

  private CancelReservationScheduleRequestDocument createCancelReservationScheduleRequest(final String reservationId) {
    CancelReservationScheduleRequestDocument requestDocument = CancelReservationScheduleRequestDocument.Factory.newInstance();

    CancelReservationScheduleRequest request = requestDocument.addNewCancelReservationScheduleRequest();
    request.setReservationScheduleId(reservationId);

    return requestDocument;
  }

  @Override
  public void createReservation(Reservation reservation, boolean autoProvision) {
    CreateReservationScheduleRequestDocument requestDocument = createReservationScheduleRequest(reservation, autoProvision);

    UpdatedReservationStatus result;
    try {
      CreateReservationScheduleResponseDocument responseDocument = getResourceAllocationAndSchedulingService()
          .createReservationSchedule(requestDocument, getSecurityDocument());

      log.debug("Create reservation response: {}", responseDocument.getCreateReservationScheduleResponse());

      String reservationId = responseDocument.getCreateReservationScheduleResponse().getReservationScheduleId();
      reservation.setReservationId(reservationId);
      reservation = reservationRepo.saveAndFlush(reservation);

      ReservationStatus status = OpenDracStatusTranslator.translate(responseDocument.getCreateReservationScheduleResponse().getResult(), autoProvision);
      if (status.isErrorState()) {
        String failedReason = composeFailedReason(responseDocument);
        result = UpdatedReservationStatus.error(status, failedReason);

        log.info("Create reservation ({}) failed with '{}'", reservationId, failedReason);
      } else {
        result = UpdatedReservationStatus.forNewStatus(status);
      }
    } catch (ResourceAllocationAndSchedulingServiceFault e) {
      log.warn("Creating a reservation failed", e);
      result = UpdatedReservationStatus.notAccepted(e.getMessage().trim());
    } catch (RemoteException e) {
      log.error("Unexpected exception while requesting reservation from OpenDRAC", e);
      result = UpdatedReservationStatus.failed(e.getMessage().trim());
    }

    if (reservation.getReservationId() != null) {
      reservationService.updateStatus(reservation.getReservationId(), result);
    }
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
  public List<NbiPort> findAllPorts() {
    List<NbiPort> ports = Lists.newArrayList();

    for (EndpointT endpoint : findAllEndPoints()) {
      ports.add(convertToNbiPort(endpoint));
    }

    return ports;
  }

  @Override
  public NbiPort findPhysicalPortByNmsPortId(final String nmsPortId) throws PortNotAvailableException {
    EndpointT endpoint = findEndPointById(nmsPortId);
    return convertToNbiPort(endpoint);
  }

  /**
   * Find by id is a little more complex, because the webservice only supports a
   * lookup by tna and not by NMS port id. In BoD the tna is locally stored as the
   * nocLabel field. This field can be edited. The tna can also be edited in
   * OpenDRAC. So the only save thing to do is use the id. The service keeps a
   * cache of id to tna.
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
    } else {
      EndpointT endPoint = findEndpointByTna(tna);

      if (endPoint.getId().equals(id)) {
        return endPoint;
      } else {
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
    return findAllPorts().size();
  }

  private Optional<QueryReservationScheduleResponseDocument> queryReservation(String reservationId) {
    try {
      QueryReservationScheduleRequestDocument requestDocument = createQueryReservationScheduleRequest(reservationId);
      QueryReservationScheduleResponseDocument queryReservationSchedule = getResourceAllocationAndSchedulingService().queryReservationSchedule(requestDocument,
          getSecurityDocument());
      return Optional.of(queryReservationSchedule);
    } catch (AxisFault e) {
      log.error("Error querying reservation (" + reservationId + "): ", e);

      String errorMessageToLowerCase = e.getMessage().toLowerCase();

      // no connection to nms
      if (errorMessageToLowerCase.contains(CONNECTION_REFUSED_LOWER_CASE_MESSAGE)) {
        log.warn("Connection refused to {}", schedulingServiceUrl);
      } else {
        // invalid credentials
        if (errorMessageToLowerCase.contains(AUTHENTICATION_FAILED_LOWER_CASE_MESSAGE)) {
          log.warn("Authentication check failed for user {} and resource group {}", username, resourceGroupName);
        }
      }

    } catch (ResourceAllocationAndSchedulingServiceFault | RemoteException e) {
      log.error("Error querying reservation (" + reservationId + "): ", e);
    }

    return Optional.empty();
  }

  @Override
  public Optional<ReservationStatus> getReservationStatus(final String reservationId) {
    Optional<QueryReservationScheduleResponseDocument> responseDocument = queryReservation(reservationId);

    return responseDocument.map(response -> {
      if (response.getQueryReservationScheduleResponse().getIsFound()) {
        ReservationScheduleT schedule = response.getQueryReservationScheduleResponse().getReservationSchedule();
        return OpenDracStatusTranslator.translate(schedule);
      } else {
        log.warn("No reservation found for reservationId: {}, returning FAILED", reservationId);
        return FAILED;
      }
    });
  }

  private QueryReservationScheduleRequestDocument createQueryReservationScheduleRequest(String reservationId) {
    QueryReservationScheduleRequestDocument requestDocument = QueryReservationScheduleRequestDocument.Factory.newInstance();
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
            ValidReservationScheduleStatusT.EXECUTION_TIMED_OUT, PASSED_END_TIME).put(
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
      } else {
        return scheduleStatusTranslations.get(status);
      }
    }
  }

  @VisibleForTesting
  CreateReservationScheduleRequestDocument createReservationScheduleRequest(Reservation reservation, boolean autoProvision) {

    CreateReservationScheduleRequestDocument requestDocument = CreateReservationScheduleRequestDocument.Factory.newInstance();

    ReservationScheduleRequestT schedule =
      requestDocument.addNewCreateReservationScheduleRequest().addNewReservationSchedule();

    ValidReservationScheduleTypeT.Enum type = autoProvision ? ValidReservationScheduleTypeT.RESERVATION_SCHEDULE_AUTOMATIC
        : ValidReservationScheduleTypeT.RESERVATION_SCHEDULE_MANUAL;
    Calendar startTime = reservation.getStartDateTime().toCalendar(Locale.getDefault());

    Minutes duration = reservation.getEndDateTime().isPresent()? minutesBetween(reservation.getStartDateTime(), reservation.getEndDateTime().get()) : MAX_DURATION;

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

    Optional<Optional<String>> serviceId = responseDocument.map(response -> {
      if (response.getQueryReservationScheduleResponse().getIsFound()) {
        return Optional.of(response.getQueryReservationScheduleResponse().getReservationSchedule().getOccurrenceIdArray(0));
      }
      return Optional.empty();
    });

    return flatten(serviceId);
  }

  private static <T> Optional<T> flatten(Optional<Optional<T>> optional) {
    return optional.isPresent() ? optional.get() : Optional.empty();
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
    ReservationEndPoint virtualSourcePort = reservation.getSourcePort();
    ReservationEndPoint virtualDestinationPort = reservation.getDestinationPort();

    String sourceTna;
    String destinationTna;
    try {
      sourceTna = findTnaById(virtualSourcePort.getPhysicalPort().getNmsPortId());
      destinationTna = findTnaById(virtualDestinationPort.getPhysicalPort().getNmsPortId());
    } catch (PortNotAvailableException e) {
      throw new IllegalStateException(e);
    }

    PathRequestT pathRequest = PathRequestT.Factory.newInstance();
    pathRequest.setSourceTna(sourceTna);
    pathRequest.setTargetTna(destinationTna);
    pathRequest.setRate(reservation.getBandwidth().intValue());
    pathRequest.setSourceVlanId(translateVlanId(virtualSourcePort));
    pathRequest.setTargetVlanId(translateVlanId(virtualDestinationPort));
    pathRequest.setRoutingAlgorithm(routingAlgorithm);

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

  private NbiPort convertToNbiPort(EndpointT endpoint) {
    NbiPort port = new NbiPort();

    if (StringUtils.hasText(endpoint.getUserLabel())) {
      port.setSuggestedNocLabel(endpoint.getUserLabel());
    } else {
      port.setSuggestedNocLabel(endpoint.getTna());
    }
    port.setNmsPortId(endpoint.getId());
    port.setSuggestedBodPortId(endpoint.getTna());
    port.setVlanRequired(isVlanRequired(endpoint.getTna()));
    port.setInterfaceType(translateInterfaceType(endpoint.getSignalingType()));

    return port;
  }

  private InterfaceType translateInterfaceType(String interfaceType) {
    if (interfaceType.equals("ENNI")) {
      return InterfaceType.E_NNI;
    } else if (interfaceType.equals("UNI")) {
      return InterfaceType.UNI;
    }

    throw new IllegalArgumentException("Uknown interface type " + interfaceType);
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

  private String translateVlanId(ReservationEndPoint endPoint) {
    return endPoint.getVlanId().map(v -> v.toString()).orElse(DEFAULT_VID);
  }

  private List<EndpointT> findAllEndPoints() {
    try {
      QueryEndpointsRequestDocument requestDocument = createQueryEndpointsRequest();
      QueryEndpointsResponseDocument response = getNetworkMonitoringService().queryEndpoints(requestDocument, getSecurityDocument());

      log.debug("Find all endpoints response: {}", response);

      List<EndpointT> endPoints = Lists.newArrayList();
      for (String tna : response.getQueryEndpointsResponse().getTnaArray()) {
        try {
          endPoints.add(findEndpointByTna(tna));
        } catch (PortNotAvailableException e) {
          log.error("Unable to find endpoint for TNA {}", tna);
        }
      }

      return endPoints;
    } catch (NetworkMonitoringServiceFault | RemoteException e) {
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
    } catch (NetworkMonitoringServiceFault | RemoteException e) {
      log.warn("Can not query openDrac for end point by tna", e);
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
    ResourceAllocationAndSchedulingService_v30Stub schedulingService = new ResourceAllocationAndSchedulingService_v30Stub(schedulingServiceUrl);

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

  void setGroupName(String groupName) {
    this.groupName = groupName;
  }

}