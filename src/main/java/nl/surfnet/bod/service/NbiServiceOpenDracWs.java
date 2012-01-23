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

import static org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ValidReservationScheduleStatusT.*;
import static nl.surfnet.bod.domain.ReservationStatus.*;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.annotation.PostConstruct;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;

import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd.Security;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd.SecurityDocument;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd.UsernameToken;
import org.opendrac.www.ws.networkmonitoringservice.v3_0.NetworkMonitoringServiceFault;
import org.opendrac.www.ws.networkmonitoringservice.v3_0.NetworkMonitoringService_v30Stub;
import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.EndpointT;
import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointRequestDocument;
import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointRequestDocument.QueryEndpointRequest;
import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointResponseDocument;
import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointsRequestDocument;
import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointsRequestDocument.QueryEndpointsRequest;
import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.QueryEndpointsResponseDocument;
import org.opendrac.www.ws.networkmonitoringservicetypes_v3_0.ValidEndpointsQueryTypeT;
import org.opendrac.www.ws.resourceallocationandschedulingservice.v3_0.ResourceAllocationAndSchedulingService_v30Stub;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.CreateReservationScheduleRequestDocument;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.CreateReservationScheduleResponseDocument;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.PathRequestT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.QueryReservationScheduleRequestDocument;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.QueryReservationScheduleResponseDocument;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ReservationScheduleRequestT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ReservationScheduleT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.UserInfoT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ValidProtectionTypeT;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ValidReservationScheduleStatusT.Enum;
import org.opendrac.www.ws.resourceallocationandschedulingservicetypes_v3_0.ValidReservationScheduleTypeT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.nortel.appcore.app.drac.common.utility.CryptoWrapper;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper.CryptedString;
import com.nortel.www.drac._2007._07._03.ws.ct.draccommontypes.ValidLayerT;

/**
 * A bridge to OpenDRAC's {@link NrbInterface}. Everything is contained in this
 * one class so that only this class is linked to OpenDRAC related classes.
 * 
 * @author robert
 * 
 */
class NbiServiceOpenDracWs implements NbiService {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private NetworkMonitoringService_v30Stub networkingService;

  private ResourceAllocationAndSchedulingService_v30Stub schedulingService;

  private SecurityDocument securityDocument;

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

  @Value("${nbi.service.network}")
  private String networkServiceUrl;

  @Value("${nbi.service.scheduling}")
  private String schedulingServiceUrl;

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    try {
      schedulingService = new ResourceAllocationAndSchedulingService_v30Stub(schedulingServiceUrl);
      networkingService = new NetworkMonitoringService_v30Stub(networkServiceUrl);
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
    throw new UnsupportedOperationException();

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
      final CreateReservationScheduleResponseDocument resSchedRespDoc = schedulingService.createReservationSchedule(
          createSchedule(reservation), getSecurityDocument());
      log.info("Response: " + resSchedRespDoc.getCreateReservationScheduleResponse());
      return resSchedRespDoc.getCreateReservationScheduleResponse().getReservationScheduleId();
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
    throw new UnsupportedOperationException();

  }

  /*
   * (non-Javadoc)
   * 
   * @see nl.surfnet.bod.nbi.NbiService#findAllPhysicalPorts()
   */
  @Override
  public List<PhysicalPort> findAllPhysicalPorts() {

    final List<PhysicalPort> ports = new ArrayList<PhysicalPort>();
    for (final EndpointT endpoint : findAllEndPointTypes()) {
      final PhysicalPort port = getPhysicalPort(endpoint);
      ports.add(port);
    }
    return ports;
  }

  @Override
  public PhysicalPort findPhysicalPortByName(final String name) {
    try {
      return getPhysicalPort(findEndpointByTna(name));
    }
    catch (Exception e) {
      log.error("Error: ", e);
      return null;
    }
  }

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

    QueryReservationScheduleRequestDocument queryResSchedReqDoc = QueryReservationScheduleRequestDocument.Factory
        .newInstance();
    queryResSchedReqDoc.addNewQueryReservationScheduleRequest();
    queryResSchedReqDoc.getQueryReservationScheduleRequest().setReservationScheduleId(reservationId);

    QueryReservationScheduleResponseDocument queryRespDoc = null;
    try {
      queryRespDoc = schedulingService.queryReservationSchedule(queryResSchedReqDoc, getSecurityDocument());
    }
    catch (Exception e) {
      log.error("Error: ", e);
    }

    boolean isFound = queryRespDoc.getQueryReservationScheduleResponse().getIsFound();
    if (isFound) {
      ReservationScheduleT schedule = queryRespDoc.getQueryReservationScheduleResponse().getReservationSchedule();
      final Enum status = schedule.getStatus();

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
    return null;
  }

  private CreateReservationScheduleRequestDocument createSchedule(final Reservation reservation) {
    final CreateReservationScheduleRequestDocument reservationScheduleRequestDocument = CreateReservationScheduleRequestDocument.Factory
        .newInstance();
    final CreateReservationScheduleRequestDocument.CreateReservationScheduleRequest reservationScheduleRequest = reservationScheduleRequestDocument
        .addNewCreateReservationScheduleRequest();
    final ReservationScheduleRequestT reservationSched = reservationScheduleRequest.addNewReservationSchedule();
    reservationSched.setName(reservation.getUserCreated() + "-" + System.currentTimeMillis());
    reservationSched.setType(ValidReservationScheduleTypeT.RESERVATION_SCHEDULE_AUTOMATIC);
    final Calendar calendar = Calendar.getInstance();
    calendar.setTime(reservation.getStartDateTime().toDate());
    reservationSched.setStartTime(calendar);

    final long start = reservation.getStartDateTime().toDate().getTime();
    final long end = reservation.getEndDateTime().toDate().getTime();
    reservationSched.setReservationOccurrenceDuration((int) ((end - start) / 1000));
    reservationSched.setIsRecurring(false);
    PathRequestT pathReq = createPath(reservation);
    reservationSched.setPath(pathReq);
    UserInfoT userInfo = createUser(reservation);
    reservationSched.setUserInfo(userInfo);

    return reservationScheduleRequestDocument;

  }

  private UserInfoT createUser(final Reservation reservation) {
    UserInfoT userInfo = UserInfoT.Factory.newInstance();
    userInfo.setBillingGroup(billingGroupName);
    userInfo.setSourceEndpointResourceGroup(resourceGroupName);
    userInfo.setSourceEndpointUserGroup(groupName);
    userInfo.setTargetEndpointResourceGroup(resourceGroupName);
    userInfo.setTargetEndpointUserGroup(groupName);
    return userInfo;

  }

  private PathRequestT createPath(final Reservation reservation) {
    PathRequestT pathReq = PathRequestT.Factory.newInstance();
    pathReq.setSourceTna(reservation.getSourcePort().getPhysicalPort().getName());
    pathReq.setTargetTna(reservation.getDestinationPort().getPhysicalPort().getName());
    pathReq.setRate(reservation.getBandwidth());
    pathReq.setSourceVlanId("Untagged");
    pathReq.setTargetVlanId("Untagged");
    pathReq.setRoutingAlgorithm("VCAT");
    ValidProtectionTypeT.Enum pType = ValidProtectionTypeT.Enum.forString("1Plus1Path");
    pathReq.setProtectionType(pType);
    return pathReq;

  }

  private List<EndpointT> findAllEndPointTypes() {
    try {
      final QueryEndpointsRequestDocument queryEndpointsRequest31 = QueryEndpointsRequestDocument.Factory.newInstance();
      final QueryEndpointsRequest queryEndpointsRequest = queryEndpointsRequest31.addNewQueryEndpointsRequest();
      queryEndpointsRequest.setUserGroup(groupName);
      queryEndpointsRequest.setLayer(ValidLayerT.LAYER_2);
      queryEndpointsRequest.setType(ValidEndpointsQueryTypeT.QUERY_ENDPOINTS_BY_LAYER_AND_USER_GROUP_T);
      final QueryEndpointsResponseDocument endpoints = networkingService.queryEndpoints(queryEndpointsRequest31,
          getSecurityDocument());

      final List<EndpointT> newPoints = new ArrayList<EndpointT>();
      for (final String tna : endpoints.getQueryEndpointsResponse().getTnaArray()) {
        log.info("networkingService: " + tna);
        newPoints.add(findEndpointByTna(tna));
      }
      return newPoints;
    }
    catch (Exception e) {
      log.error("Error: ", e);
      return null;
    }

  }

  private EndpointT findEndpointByTna(final String tna) throws RemoteException, NetworkMonitoringServiceFault {
    final QueryEndpointRequestDocument queryEndpointRequest31 = QueryEndpointRequestDocument.Factory.newInstance();
    final QueryEndpointRequest queryEndpointRequest = queryEndpointRequest31.addNewQueryEndpointRequest();
    queryEndpointRequest.setTna(tna);
    final QueryEndpointResponseDocument endpoint = networkingService.queryEndpoint(queryEndpointRequest31,
        getSecurityDocument());
    final EndpointT endpointFound = endpoint.getQueryEndpointResponse().getEndpoint();
    log.info("endpointFound: " + endpointFound);
    return endpointFound;
  }

  private PhysicalPort getPhysicalPort(final EndpointT facility) {
    final PhysicalPort port = new PhysicalPort();
    final PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroup();
    // port.setDisplayName(facility.getAttributes().get("userLabel"));
    port.setName(facility.getTna());
    port.setNetworkElementPk(facility.getId());
    physicalResourceGroup.setAdminGroup(groupName);
    port.setPhysicalResourceGroup(physicalResourceGroup);
    return port;
  }

  private SecurityDocument getSecurityDocument() {
    if (securityDocument == null) {
      securityDocument = SecurityDocument.Factory.newInstance();
      final Security security = securityDocument.addNewSecurity();
      final UsernameToken token = security.addNewUsernameToken();
      token.setUsername(username);
      try {
        token.setPassword(new String(CryptoWrapper.getInstance().decrypt(new CryptedString(encryptedPassword))));
      }
      catch (Exception e) {
        log.error("Error: ", e);
        securityDocument = null;
      }
    }
    return securityDocument;
  }

}
