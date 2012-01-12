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
package nl.surfnet.bod.opendrac;

import java.util.ArrayList;
import java.util.List;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.service.NbiPortService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.types.PathType;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.TaskType;
import com.nortel.appcore.app.drac.common.types.UserType;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper.CryptedString;
import com.nortel.appcore.app.drac.security.ClientLoginType;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.nrb.NrbInterface;
import com.nortel.appcore.app.drac.server.requesthandler.RemoteConnectionProxy;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandlerException;

/**
 * A wrapper 'service' around OpenDRAC's {@link NrbInterface}. The main
 * difference is that the methods in this class use a {@link LoginToken} instead
 * of a clear text password.
 * 
 * @author robert
 * 
 */
// @Service("nbiClient")
public class NbiService implements NbiPortService {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private RemoteConnectionProxy nrbProxy;

  @Value("${nbi.user}")
  private String username;

  @Value("${nbi.password}")
  private String encryptedPassword;

  private NrbInterface getNrbInterface() {

    if (nrbProxy == null) {
      nrbProxy = new RemoteConnectionProxy();
    }

    try {
      return nrbProxy.getNrbInterface();
    }
    catch (RequestHandlerException e) {
      log.error("Error: ", e);
      return null;
    }
  }

  /**
   * 
   * @param loginToken
   *          a valid {@link LoginToken}
   * @return a list of all currently available network elements or
   *         <code>null</code> whenever an exception occurs
   */
  public List<NetworkElementHolder> getAllNetworkElements() {
    try {
      return getNrbInterface().getAllNetworkElements(getLoginToken());
    }
    catch (Exception e) {
      log.error("Error: ", e);
      return null;
    }

  }

  /**
   * 
   * @param loginToken
   *          a valid {@link LoginToken}
   * @return a list of all currently available UNI network facilities or
   *         <code>null</code> whenever an exception occurs
   */
  public List<Facility> getAllUniFacilities() {

    try {
      final List<Facility> facilities = new ArrayList<Facility>();
      for (NetworkElementHolder holder : getAllNetworkElements()) {
        final List<Facility> facilitiesPerNetworkElement = getNrbInterface().getFacilities(getLoginToken(),
            holder.getId());
        for (final Facility facility : facilitiesPerNetworkElement) {
          if ("UNI".equals(facility.get("interfaceType"))) {
            facilities.add(facility);
          }
        }
      }
      return facilities;
    }
    catch (Exception e) {
      log.error("Error: ", e);
      return null;
    }
  }

  private LoginToken getLoginToken() {
    return getLoginToken(username, encryptedPassword);
  }

  /**
   * 
   * @param username
   * @param encryptedPassword
   *          an an encrypted username
   * @return a valid {@link LoginToken} or <code>null</code> whenever an
   *         exception occurs
   */
  public LoginToken getLoginToken(final String username, final String encryptedPassword) {

    final String password = CryptoWrapper.INSTANCE.decrypt(new CryptedString(encryptedPassword));
    try {
      return getNrbInterface()
          .login(ClientLoginType.INTERNAL_LOGIN, username, password.toCharArray(), null, null, null);
    }
    catch (Exception e) {
      log.error("Error: ", e);
      return null;
    }
  }

  /**
   * 
   * @param loginToken
   *          a valid {@link LoginToken}
   * @param schedule
   *          the {@link Schedule} to be executed
   * @return the id of the created schedule or <code>null</code> whenever an
   *         exception occurs
   */
  public String createSchedule(final Schedule schedule) {
    try {
      return getNrbInterface().asyncCreateSchedule(getLoginToken(), schedule);
    }
    catch (Exception e) {
      log.error("Error: ", e);
      return null;
    }
  }

  /**
   * 
   * @param loginToken
   *          a valid {@link LoginToken}
   * @param scheduleId
   *          the id of the schedule of interest
   * @return
   */
  public TaskType getScheduleStatus(final String scheduleId) {
    try {
      return getNrbInterface().getTaskInfo(getLoginToken(), scheduleId);
    }
    catch (Exception e) {
      log.error("Error: ", e);
      return null;
    }
  }

  /**
   * 
   * @param loginToken
   * @param scheduleId
   */
  public void cancelSchedule(final String scheduleId) {
    try {
      getNrbInterface().cancelSchedule(getLoginToken(), scheduleId);
    }
    catch (Exception e) {
      log.error("Error: ", e);
    }
  }

  /**
   * 
   * @param loginToken
   * @param scheduleId
   * @param minutes
   */
  public void extendSchedule(final String scheduleId, int minutes) {
    try {
      final DracService dracService = getNrbInterface().getCurrentlyActiveServiceByScheduleId(getLoginToken(),
          scheduleId);
      getNrbInterface().extendServiceTime(getLoginToken(), dracService, minutes);
    }
    catch (Exception e) {
      log.error("Error: ", e);
    }
  }

  @Override
  public List<PhysicalPort> findAll() {
    final List<Facility> allUniFacilities = getAllUniFacilities();
    final List<PhysicalPort> ports = new ArrayList<PhysicalPort>();
    for (final Facility facility : allUniFacilities) {
      PhysicalPort pp = new PhysicalPort();
      pp.setDisplayName(facility.getAid());
      pp.setName(facility.get("pk"));
      ports.add(pp);
    }
    return ports;

  }

  @Override
  public long count() {
    return findAll().size();
  }

  @Override
  public PhysicalPort findByName(String name) {
    // TODO: There must be a better way
    final List<PhysicalPort> allPhysicalPorts = findAll();
    for (final PhysicalPort port : allPhysicalPorts) {
      if (port.getName().equals(name)) {
        return port;
      }

    }
    return null;
  }

  @Override
  public String createReservation(final Reservation reservation) {
    final Schedule schedule = new Schedule();
    final PathType pathType = new PathType();
    final UserType userType = new UserType();

    // Schedule info
    schedule.setStartTime(reservation.getStartDate().toDate().getTime());
    schedule.setEndTime(reservation.getEndDate().toDate().getTime());

    // User info
    userType.setUserId(reservation.getUser());

    // Path info
    pathType.setSource(reservation.getSourcePort().getPhysicalPort().getDisplayName());
    pathType.setTarget(reservation.getDestinationPort().getPhysicalPort().getDisplayName());
    pathType.setRate(reservation.getBandwidth());

    // pathType.setSourceVlanId(srcVlanID);
    // pathType.setTargetVlanId(destVlanID);
    // pathType.setRoutingAlgorithm(routingAlgorithm);
    // pathType.setProtectionType(pType);

    schedule.setUserInfo(userType);
    schedule.setPath(pathType);
    return createSchedule(schedule);
  }

}
