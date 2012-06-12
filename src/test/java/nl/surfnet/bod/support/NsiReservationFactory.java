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

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import nl.surfnet.bod.nsi.ws.ConnectionService;

import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;
import org.ogf.schemas.nsi._2011._10.connection.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NsiReservationFactory {

  public static final int PORT = 9082;
  public static final String NSI_REQUESTER_ENDPOINT = "http://localhost:" + PORT + "/bod/nsi/requester";

  private final Logger log = LoggerFactory.getLogger(getClass());

  private String correlationId = ConnectionService.getCorrelationId();
  private String connectionId = ConnectionService.getCorrelationId();
  private String nsaProviderUrn = "urn:ogf:network:nsa:netherlight";
  private int desiredBandwidth = 1000;
  private int maxBandwidth = 1000;
  private int minBandwidth = 950;
  private XMLGregorianCalendar scheduleEndTime;
  private XMLGregorianCalendar scheduleStartTime;

  /**
   * @param nsaProviderUrn
   * @param correlationId
   * @return
   */
  public ReserveRequestType createReservation() {

    final BandwidthType bandwidthType = new BandwidthType();
    bandwidthType.setDesired(desiredBandwidth);
    bandwidthType.setMaximum(maxBandwidth);
    bandwidthType.setMinimum(minBandwidth);

    final ScheduleType scheduleType = new ScheduleType();
    scheduleType.setEndTime(scheduleEndTime);
    scheduleType.setStartTime(scheduleStartTime);

    if (scheduleType.getEndTime() != null && scheduleType.getStartTime() != null) {
      try {
        scheduleType.setDuration(DatatypeFactory.newInstance().newDuration(
            scheduleType.getEndTime().getMillisecond() - scheduleType.getStartTime().getMillisecond()));
      }
      catch (DatatypeConfigurationException e) {
        log.error("Error: ", e);
      }
    }
    final ServiceParametersType serviceParametersType = new ServiceParametersType();
    // serviceParametersType.setBandwidth(desiredBandwidth);
    serviceParametersType.setBandwidth(bandwidthType);
    serviceParametersType.setSchedule(scheduleType);

    final ReservationInfoType reservationInfoType = new ReservationInfoType();
    reservationInfoType.setServiceParameters(serviceParametersType);
    reservationInfoType.setConnectionId(connectionId);
    reservationInfoType.setGlobalReservationId(correlationId);

    final ReserveType reservationType = new ReserveType();
    reservationType.setProviderNSA(nsaProviderUrn);
    reservationType.setReservation(reservationInfoType);

    final ReserveRequestType reservationRequestType = new ReserveRequestType();
    reservationRequestType.setCorrelationId(this.correlationId);
    reservationRequestType.setReplyTo(NSI_REQUESTER_ENDPOINT);
    reservationRequestType.setReserve(reservationType);

    return reservationRequestType;
  }

  public final NsiReservationFactory setCorrelationId(String correlationId) {
    this.correlationId = correlationId;
    return this;
  }

  public final NsiReservationFactory setNsaProviderUrn(String nsaProviderUrn) {
    this.nsaProviderUrn = nsaProviderUrn;
    return this;
  }

  public final NsiReservationFactory setDesiredBandwidth(int desiredBandwidth) {
    this.desiredBandwidth = desiredBandwidth;
    return this;
  }

  // public final NsiReservationFactory setMaxBandwidth(long maxBandwidth) {
  // this.maxBandwidth = maxBandwidth;
  // return this;
  // }
  //
  // public final NsiReservationFactory setMinBandwidth(long minBandwidth) {
  // this.minBandwidth = minBandwidth;
  // return this;
  // }

  public final NsiReservationFactory setScheduleEndTime(XMLGregorianCalendar scheduleEndTime) {
    this.scheduleEndTime = scheduleEndTime;
    return this;
  }

  public final NsiReservationFactory setScheduleStartTime(XMLGregorianCalendar scheduleStartTime) {
    this.scheduleStartTime = scheduleStartTime;
    return this;
  }

  public final NsiReservationFactory setConnectionId(String connectionId) {
    this.connectionId = connectionId;
    return this;
  }

}
