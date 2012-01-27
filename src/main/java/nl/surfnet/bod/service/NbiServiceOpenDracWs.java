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
package nl.surfnet.bod.service;

import static nl.surfnet.bod.domain.ReservationStatus.*;
import static org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ValidReservationScheduleStatusT.*;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.domain.VirtualPort;

import org.joda.time.Minutes;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd.Security;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd.SecurityDocument;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd.UsernameToken;
import org.opendrac.www.ws.networkmonitoringservice.v3_0.NetworkMonitoringServiceFault;
import org.opendrac.www.ws.networkmonitoringservice.v3_0.NetworkMonitoringService_v30Stub;
import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.*;
import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointRequestDocument.QueryEndpointRequest;
import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointsRequestDocument.QueryEndpointsRequest;
import org.opendrac.www.ws.resourceallocationandschedulingservice.v3_0.ResourceAllocationAndSchedulingService_v30Stub;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.*;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.CancelReservationScheduleRequestDocument.CancelReservationScheduleRequest;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.CreateReservationScheduleRequestDocument.CreateReservationScheduleRequest;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ExtendCurrentServiceForScheduleRequestDocument.ExtendCurrentServiceForScheduleRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.collect.Lists;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper.CryptedString;
import com.nortel.www.drac._2007._07._03.ws.ct.draccommontypes.CompletionResponseDocument;
import com.nortel.www.drac._2007._07._03.ws.ct.draccommontypes.ValidLayerT;

/**
 * A bridge to OpenDRAC's web services. Everything is contained in this one
 * class so that only this class is linked to OpenDRAC related classes.
 *
 * @author robert
 *
 */
class NbiServiceOpenDracWs implements NbiService {

  public static final String ROUTING_ALGORITHM = "VCAT";
  public static final String DEFAULT_VID = "Untagged";
  public static final ValidProtectionTypeT.Enum DEFAULT_PROTECTIONTYPE = ValidProtectionTypeT.X_1_PLUS_1_PATH;

  private final Logger log = LoggerFactory.getLogger(getClass());

  private NetworkMonitoringService_v30Stub networkingService;
  private ResourceAllocationAndSchedulingService_v30Stub schedulingService;
  private SecurityDocument securityDocument;

  private final Map<String, String> physicalPortsByNePkAndTna = new HashMap<String, String>();

  @Value("${nbi.billing.group.name}")
  private String billingGroupName;

  @Value("${nbi.password}")
  private String encryptedPassword;

  @Value("${nbi.group.name}")
  private String groupName;

  @Value("${nbi.resource.group.name}")
  private String resourceGroupName;

  @Value("${nbi.user}")
  private String username;

  @Value("${nbi.service.inventory}")
  private String inventoryServiceUrl;

  @Value("${nbi.service.scheduling}")
  private String schedulingServiceUrl;

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    try {
      schedulingService = new ResourceAllocationAndSchedulingService_v30Stub(schedulingServiceUrl);
      networkingService = new NetworkMonitoringService_v30Stub(inventoryServiceUrl);
      getSecurityDocument();
    }
    catch (IOException e) {
      log.error("Error: ", e);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see nl.surfnet.bod.nbi.NbiService#cancelSchedule(java.lang.String)
   */
  @Override
  public void cancelReservation(final String reservationId) {
    final CancelReservationScheduleRequestDocument requestDocument = CancelReservationScheduleRequestDocument.Factory
        .newInstance();
    final CancelReservationScheduleRequest request = requestDocument.addNewCancelReservationScheduleRequest();
    request.setReservationScheduleId(reservationId);
    try {
      final CompletionResponseDocument response = schedulingService.cancelReservationSchedule(requestDocument,
          getSecurityDocument());
      log.info("Status: {}", response.getCompletionResponse().getResult());
    }
    catch (Exception e) {
      log.error("Error: ", e);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see nl.surfnet.bod.nbi.NbiService#createReservation(nl.surfnet.bod.domain.
   * Reservation)
   */
  @Override
  public String createReservation(final Reservation reservation) {
    try {
      final CreateReservationScheduleResponseDocument responseDocument = schedulingService.createReservationSchedule(
          createSchedule(reservation), getSecurityDocument());
      log.info("Response: {}", responseDocument.getCreateReservationScheduleResponse());
      return responseDocument.getCreateReservationScheduleResponse().getReservationScheduleId();
    }
    catch (Exception e) {
      log.error("Error: ", e);
    }
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see nl.surfnet.bod.nbi.NbiService#extendSchedule(java.lang.String, int)
   */
  @Override
  public void extendReservation(final String reservationId, final int minutes) {
    final ExtendCurrentServiceForScheduleT extensionDefinition = ExtendCurrentServiceForScheduleT.Factory.newInstance();
    extensionDefinition.setScheduleId(reservationId);
    extensionDefinition.setMinutesToExtend(minutes);

    final ExtendCurrentServiceForScheduleRequestDocument requestDocument = ExtendCurrentServiceForScheduleRequestDocument.Factory
        .newInstance();

    final ExtendCurrentServiceForScheduleRequest request = requestDocument
        .addNewExtendCurrentServiceForScheduleRequest();
    request.setExtensionDefinition(extensionDefinition);
    try {
      schedulingService.extendCurrentServiceForSchedule(requestDocument, getSecurityDocument());
    }
    catch (Exception e) {
      log.error("Error: ", e);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see nl.surfnet.bod.nbi.NbiService#findAllPhysicalPorts()
   */
  @Override
  public List<PhysicalPort> findAllPhysicalPorts() {
    final List<PhysicalPort> ports = new ArrayList<PhysicalPort>();
    physicalPortsByNePkAndTna.clear();
    for (final EndpointT endpoint : findAllEndPointTypes()) {
      final PhysicalPort port = getPhysicalPort(endpoint);
      ports.add(port);
      physicalPortsByNePkAndTna.put(port.getNetworkElementPk(), port.getName());
    }
    return ports;
  }

  public PhysicalPort findPhysicalPortByName(final String name) {
    try {
      return getPhysicalPort(findEndpointByTna(name));
    }
    catch (Exception e) {
      log.error("Error: ", e);
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * nl.surfnet.bod.nbi.NbiService#findPhysicalPortByNetworkElementId(java.lang
   * .String)
   */
  @Override
  public PhysicalPort findPhysicalPortByNetworkElementId(final String networkElementId) {
    final String name = physicalPortsByNePkAndTna.get(networkElementId);
    PhysicalPort physicalPort = null;
    if (name != null) {
      physicalPort = findPhysicalPortByName(name);
    }
    if (physicalPort == null || !physicalPort.getNetworkElementPk().equals(networkElementId)) {
      final List<PhysicalPort> physicalPorts = findAllPhysicalPorts();
      for (final PhysicalPort port : physicalPorts) {
        if (port.getNetworkElementPk().equals(networkElementId)) {
          return port;
        }
      }
    }
    else {
      return physicalPort;
    }
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see nl.surfnet.bod.nbi.NbiService#getPhysicalPortsCount
   */
  @Override
  public long getPhysicalPortsCount() {
    return findAllPhysicalPorts().size();
  }

  /*
   * (non-Javadoc)
   *
   * @see nl.surfnet.bod.nbi.NbiService#getScheduleStatus(java.lang.String)
   */
  @Override
  public ReservationStatus getReservationStatus(final String reservationId) {
    final QueryReservationScheduleRequestDocument requestDocument = QueryReservationScheduleRequestDocument.Factory
        .newInstance();
    requestDocument.addNewQueryReservationScheduleRequest();
    requestDocument.getQueryReservationScheduleRequest().setReservationScheduleId(reservationId);

    QueryReservationScheduleResponseDocument responseDocument = null;
    try {
      responseDocument = schedulingService.queryReservationSchedule(requestDocument, getSecurityDocument());
    }
    catch (Exception e) {
      log.error("Error: ", e);
    }

    if (responseDocument.getQueryReservationScheduleResponse().getIsFound()) {
      final ReservationScheduleT schedule = responseDocument.getQueryReservationScheduleResponse()
          .getReservationSchedule();
      final ValidReservationScheduleStatusT.Enum status = schedule.getStatus();

      // Translate OpenDRAC schedule status or state to BoD's reservation status
      if (status == CONFIRMATION_PENDING) {
        return PREPARING;
      }
      else if (status == CONFIRMATION_TIMED_OUT) {
        return FAILED;
      }
      else if (status == CONFIRMATION_CANCELLED) {
        return CANCELLED;
      }
      else if (status == EXECUTION_PENDING) {
        return SCHEDULED;
      }
      else if (status == EXECUTION_IN_PROGRESS) {
        return RUNNING;
      }
      else if (status == EXECUTION_SUCCEEDED) {
        return SUCCEEDED;
      }
      else if (status == EXECUTION_PARTIALLY_SUCCEEDED) {
        // An OpenDRAC service can be partially successful, a single schedule
        // not (thats a reservation in BoD context).
        return FAILED;
      }
      else if (status == EXECUTION_TIMED_OUT) {
        return FAILED;
      }
      else if (status == EXECUTION_FAILED) {
        return FAILED;
      }
      else if (status == EXECUTION_PARTIALLY_CANCELLED) {
        return CANCELLED;
      }
      else if (status == EXECUTION_CANCELLED) {
        return CANCELLED;
      }
      else {
        log.warn("Unknow status: {}", status);
        return null;
      }
    }
    else {
      log.info("No reservation found for reservation id: {}, returning null", reservationId);
      return null;
    }

  }

  private CreateReservationScheduleRequestDocument createSchedule(final Reservation reservation) {
    final CreateReservationScheduleRequestDocument requestDocument = CreateReservationScheduleRequestDocument.Factory
        .newInstance();
    final CreateReservationScheduleRequest request = requestDocument.addNewCreateReservationScheduleRequest();
    final ReservationScheduleRequestT schedule = request.addNewReservationSchedule();
    final PathRequestT pathRequest = createPath(reservation);
    final UserInfoT userInfo = createUser(reservation);

    schedule.setName(reservation.getUserCreated() + "-" + System.currentTimeMillis());
    schedule.setType(ValidReservationScheduleTypeT.RESERVATION_SCHEDULE_AUTOMATIC);

    final Calendar calendar = Calendar.getInstance();
    calendar.setTime(reservation.getStartDateTime().toDate());
    schedule.setStartTime(calendar);
    schedule.setReservationOccurrenceDuration(Minutes.minutesBetween(reservation.getStartDateTime(),
        reservation.getEndDateTime()).getMinutes());
    schedule.setIsRecurring(false);
    schedule.setPath(pathRequest);
    schedule.setUserInfo(userInfo);
    return requestDocument;
  }

  private UserInfoT createUser(final Reservation reservation) {
    final UserInfoT userInfo = UserInfoT.Factory.newInstance();
    userInfo.setBillingGroup(billingGroupName);
    userInfo.setSourceEndpointResourceGroup(resourceGroupName);
    userInfo.setSourceEndpointUserGroup(groupName);
    userInfo.setTargetEndpointResourceGroup(resourceGroupName);
    userInfo.setTargetEndpointUserGroup(groupName);
    return userInfo;
  }

  private PathRequestT createPath(final Reservation reservation) {
    final PathRequestT pathRequest = PathRequestT.Factory.newInstance();
    final VirtualPort virtualSourcePort = reservation.getSourcePort();
    pathRequest.setSourceTna(virtualSourcePort.getPhysicalPort().getName());
    final VirtualPort virtualDestinationPort = reservation.getDestinationPort();
    pathRequest.setTargetTna(virtualDestinationPort.getPhysicalPort().getName());
    pathRequest.setRate(reservation.getBandwidth());
    if (virtualSourcePort.getVlanId() == null) {
      pathRequest.setSourceVlanId(DEFAULT_VID);
    }
    else {
      pathRequest.setSourceVlanId(virtualSourcePort.getVlanId().toString());
    }
    if (virtualDestinationPort.getVlanId() == null) {
      pathRequest.setTargetVlanId(DEFAULT_VID);
    }
    else {
      pathRequest.setTargetVlanId(virtualDestinationPort.getVlanId().toString());
    }
    pathRequest.setRoutingAlgorithm(ROUTING_ALGORITHM);
    pathRequest.setProtectionType(DEFAULT_PROTECTIONTYPE);
    return pathRequest;
  }

  private List<EndpointT> findAllEndPointTypes() {
    try {
      final QueryEndpointsRequestDocument requestDocument = QueryEndpointsRequestDocument.Factory.newInstance();
      final QueryEndpointsRequest request = requestDocument.addNewQueryEndpointsRequest();
      request.setUserGroup(groupName);
      request.setLayer(ValidLayerT.LAYER_2);
      request.setType(ValidEndpointsQueryTypeT.QUERY_ENDPOINTS_BY_LAYER_AND_USER_GROUP_T);
      final QueryEndpointsResponseDocument response = networkingService.queryEndpoints(requestDocument,
          getSecurityDocument());
      final List<EndpointT> endPoints = new ArrayList<EndpointT>();
      for (final String tna : response.getQueryEndpointsResponse().getTnaArray()) {
        log.debug("networkingService: {}", tna);
        endPoints.add(findEndpointByTna(tna));
      }
      return endPoints;
    }
    catch (Exception e) {
      log.error("Error: ", e);
      return null;
    }

  }

  private EndpointT findEndpointByTna(final String tna) throws RemoteException, NetworkMonitoringServiceFault {
    final QueryEndpointRequestDocument requestDocument = QueryEndpointRequestDocument.Factory.newInstance();
    final QueryEndpointRequest request = requestDocument.addNewQueryEndpointRequest();
    request.setTna(tna);
    final QueryEndpointResponseDocument response = networkingService.queryEndpoint(requestDocument,
        getSecurityDocument());
    final EndpointT endpointFound = response.getQueryEndpointResponse().getEndpoint();
    log.debug("endpointFound: {}", endpointFound);
    return endpointFound;
  }

  private PhysicalPort getPhysicalPort(final EndpointT endpoint) {
    final PhysicalPort port = new PhysicalPort();
    port.setName(endpoint.getTna());
    port.setNetworkElementPk(endpoint.getId());

    return port;
  }

  private SecurityDocument getSecurityDocument() {
    if (securityDocument == null) {
      securityDocument = SecurityDocument.Factory.newInstance();
      final Security security = securityDocument.addNewSecurity();
      final UsernameToken token = security.addNewUsernameToken();
      token.setUsername(username);
      try {
        token.setPassword(CryptoWrapper.getInstance().decrypt(new CryptedString(encryptedPassword)));
      }
      catch (Exception e) {
        log.error("Error: ", e);
        securityDocument = null;
      }
    }
    return securityDocument;
  }

}
