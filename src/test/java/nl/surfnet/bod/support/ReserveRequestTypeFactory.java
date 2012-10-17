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
package nl.surfnet.bod.support;

import java.util.UUID;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;
import org.ogf.schemas.nsi._2011._10.connection.types.*;

public class ReserveRequestTypeFactory {

  private static final int PORT = 9082;
  private static final String NSI_REQUESTER_ENDPOINT = "http://localhost:" + PORT + "/bod/nsi/requester";

  private String correlationId = UUID.randomUUID().toString();
  private String connectionId = UUID.randomUUID().toString();
  private String providerNsa = "urn:example:nsa:provider";
  private String requesterNsa = "urn:example:nsa:provider";

  private XMLGregorianCalendar scheduleEndTime;
  private XMLGregorianCalendar scheduleStartTime;
  private int desiredBandwidth = 100;
  private int maxBandwidth = 100;
  private int minBandwidth = 100;
  private String description = "Some example Description";
  private PathType path;
  private ServiceParametersType serviceParameters = new ServiceParametersType();
  private Duration duration;

  public ReserveRequestTypeFactory() {
    try {
      scheduleEndTime = DatatypeFactory.newInstance().newXMLGregorianCalendar();
      scheduleStartTime = DatatypeFactory.newInstance().newXMLGregorianCalendar();
    }
    catch (DatatypeConfigurationException e) {
      e.printStackTrace();
    }

    scheduleStartTime.setDay(1);
    scheduleStartTime.setMonth(1);
    scheduleStartTime.setYear(2013);
    scheduleEndTime.setDay(scheduleStartTime.getDay() + 1);
  }

  public ReserveRequestType create() {
    final BandwidthType bandwidthType = new BandwidthType();
    bandwidthType.setDesired(desiredBandwidth);
    bandwidthType.setMaximum(maxBandwidth);
    bandwidthType.setMinimum(minBandwidth);

    final ScheduleType scheduleType = new ScheduleType();
    scheduleType.setEndTime(scheduleEndTime);
    scheduleType.setStartTime(scheduleStartTime);
    scheduleType.setDuration(duration);

    serviceParameters.setBandwidth(bandwidthType);
    serviceParameters.setSchedule(scheduleType);

    if (scheduleType.getEndTime() != null && scheduleType.getStartTime() != null) {
      try {
        scheduleType.setDuration(DatatypeFactory.newInstance().newDuration(
            scheduleType.getEndTime().getMillisecond() - scheduleType.getStartTime().getMillisecond()));
      }
      catch (DatatypeConfigurationException e) {
        e.printStackTrace();
      }
    }

    final ReservationInfoType reservationInfoType = new ReservationInfoType();
    reservationInfoType.setConnectionId(connectionId);
    reservationInfoType.setGlobalReservationId(correlationId);
    reservationInfoType.setDescription(description);
    reservationInfoType.setServiceParameters(serviceParameters);

    final ReserveType reservationType = new ReserveType();
    reservationType.setProviderNSA(providerNsa);
    reservationType.setReservation(reservationInfoType);
    reservationType.setRequesterNSA(requesterNsa);

    final ReserveRequestType reservationRequestType = new ReserveRequestType();
    reservationRequestType.setCorrelationId(this.correlationId);
    reservationRequestType.setReplyTo(NSI_REQUESTER_ENDPOINT);
    reservationRequestType.setReserve(reservationType);

    ServiceTerminationPointType sourceStp = new ServiceTerminationPointType();
    sourceStp.setStpId("urn:source:53");
    ServiceTerminationPointType destStp = new ServiceTerminationPointType();
    destStp.setStpId("urn:dest:52");

    if (path == null) {
      path = new PathType();
      path.setSourceSTP(sourceStp);
      path.setDestSTP(destStp);
    }
    reservationInfoType.setPath(path);

    return reservationRequestType;
  }

  public final ReserveRequestTypeFactory setDesiredBandwidth(int desiredBandwidth) {
    this.desiredBandwidth = desiredBandwidth;
    return this;
  }

  public final ReserveRequestTypeFactory setScheduleEndTime(XMLGregorianCalendar scheduleEndTime) {
    this.scheduleEndTime = scheduleEndTime;
    return this;
  }

  public final ReserveRequestTypeFactory setScheduleStartTime(XMLGregorianCalendar scheduleStartTime) {
    this.scheduleStartTime = scheduleStartTime;
    return this;
  }

  public final ReserveRequestTypeFactory setDescription(String description) {
    this.description = description;
    return this;
  }

  public final ReserveRequestTypeFactory setPath(PathType path) {
    this.path = path;
    return this;
  }

  public final ReserveRequestTypeFactory setServiceParameters(ServiceParametersType serviceParameters) {
    this.serviceParameters = serviceParameters;
    return this;
  }

  public final ReserveRequestTypeFactory setConnectionId(String connectionId) {
    this.connectionId = connectionId;
    return this;
  }

  public ReserveRequestTypeFactory setCorrelationId(String correlationId) {
    this.correlationId = correlationId;
    return this;
  }

  public ReserveRequestTypeFactory setProviderNsa(String providerNsa) {
    this.providerNsa = providerNsa;
    return this;
  }

  public ReserveRequestTypeFactory setDuration(Duration duration) {
    this.duration = duration;
    return this;
  }
}
