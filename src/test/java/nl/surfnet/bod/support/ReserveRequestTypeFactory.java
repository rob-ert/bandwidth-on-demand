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
