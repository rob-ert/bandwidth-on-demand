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
package nl.surfnet.bod.nbi.onecontrol;

import static nl.surfnet.bod.nbi.onecontrol.MtosiUtils.createComonObjectInfoTypeName;
import static nl.surfnet.bod.nbi.onecontrol.MtosiUtils.createRdn;
import static nl.surfnet.bod.nbi.onecontrol.MtosiUtils.createSscValue;

import java.util.List;

import com.google.common.annotations.VisibleForTesting;

import nl.surfnet.bod.domain.NbiPort;
import nl.surfnet.bod.domain.NbiPort.InterfaceType;
import nl.surfnet.bod.domain.ProtectionType;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationEndPoint;
import nl.surfnet.bod.util.XmlUtils;

import org.joda.time.DateTime;
import org.tmforum.mtop.fmw.xsd.nam.v1.NamingAttributeType;
import org.tmforum.mtop.sa.xsd.scai.v1.ReserveRequest;
import org.tmforum.mtop.sb.xsd.svc.v1.AdminStateType;
import org.tmforum.mtop.sb.xsd.svc.v1.ResourceFacingServiceType;
import org.tmforum.mtop.sb.xsd.svc.v1.ServiceAccessPointType;
import org.tmforum.mtop.sb.xsd.svc.v1.ServiceCharacteristicValueType;
import org.tmforum.mtop.sb.xsd.svc.v1.ServiceStateType;

public class ReserveRequestBuilder {

  static final String MANAGING_DOMAIN = "CIENA/OneControl";
  static final String TRAFFIC_MAPPING_TABLECOUNT = "1";
  static final String TRAFFIC_MAPPING_FROM_TABLE_PRIORITY = "all";
  static final String TRAFFIC_MAPPING_TO_TABLE_TRAFFICCLASS = "5";

  private static final String CTP_PREFIX = "/eth=";

  private ReserveRequestBuilder() {
  }

  public static ReserveRequest createReservationRequest(Reservation reservation) {
    ResourceFacingServiceType rfsData = createBasicRfsData(reservation).withSapList(
      getSap(reservation, reservation.getSourcePort()),
      getSap(reservation, reservation.getDestinationPort()));

    return createReserveRequest(reservation.getEndDateTime()).withRfsCreateData(rfsData);
  }

  @VisibleForTesting
  static ServiceAccessPointType getSap(Reservation reservation, ReservationEndPoint endPoint) {
    ServiceAccessPointType sap = createServiceAccessPoint(endPoint.getPhysicalPort().getNbiPort(), reservation.getReservationId());

    List<ServiceCharacteristicValueType> describedByList = sap.getDescribedByList();

    describedByList.add(createSscValue("TrafficMappingTableCount", TRAFFIC_MAPPING_TABLECOUNT));
    describedByList.add(createSscValue("TrafficMappingFrom_Table_Priority", TRAFFIC_MAPPING_FROM_TABLE_PRIORITY));
    describedByList.add(createSscValue("TrafficMappingTo_Table_TrafficClass", TRAFFIC_MAPPING_TO_TABLE_TRAFFICCLASS));

    Integer vlanId = vlanIdOfEndPoint(endPoint);
    if (vlanId != null) {
      describedByList.add(createSscValue("ServiceType", "EVPL"));
      describedByList.add(createSscValue("TrafficMappingFrom_Table_VID", String.valueOf(vlanId)));
    } else {
      describedByList.add(createSscValue("ServiceType", "EPL"));
      describedByList.add(createSscValue("TrafficMappingFrom_Table_VID", "all"));
    }

    describedByList.add(createSscValue("InterfaceType", translate(endPoint.getPhysicalPort().getNbiPort().getInterfaceType())));
    describedByList.add(createSscValue("TrafficMappingTo_Table_IngressCIR", reservation.getBandwidth().toString()));
    describedByList.add(createSscValue("ProtectionLevel", translate(reservation.getProtectionType())));

    return sap;
  }

  private static String translate(InterfaceType interfaceType) {
    switch (interfaceType) {
    case E_NNI:
      return "E-NNI";
    case UNI:
      return "UNI";
    }
    throw new IllegalArgumentException("Unsupported interfaceType (" + interfaceType.name() + ")");
  }

  private static String translate(ProtectionType protectionType) {
    switch (protectionType) {
    case PROTECTED:
      return "Partially Protected";
    case UNPROTECTED:
      return "Unprotected";
    case REDUNDANT:
      break;
    }
    throw new IllegalArgumentException("Unsupported protectionType (" + protectionType.name() + ")");
  }

  private static Integer vlanIdOfEndPoint(ReservationEndPoint endPoint) {
    if (endPoint.getEnniVlanId().isPresent()) {
      return endPoint.getEnniVlanId().get();
    } else if (endPoint.getVirtualPort().isPresent() && endPoint.getVirtualPort().get().getVlanId() != null) {
      return endPoint.getVirtualPort().get().getVlanId();
    } else {
      return null;
    }
  }

  private static ReserveRequest createReserveRequest(DateTime endDateTime) {
    ReserveRequest reserveRequest = new org.tmforum.mtop.sa.xsd.scai.v1.ObjectFactory().createReserveRequest()
      .withExpiringTime(XmlUtils.toGregorianCalendar(endDateTime));

    return reserveRequest;
  }

  @VisibleForTesting
  static ResourceFacingServiceType createBasicRfsData(Reservation reservation) {
    ResourceFacingServiceType rfsData = new org.tmforum.mtop.sb.xsd.svc.v1.ObjectFactory().createResourceFacingServiceType()
      .withName(createComonObjectInfoTypeName("RFS", reservation.getReservationId()))
      .withIsMandatory(true)
      .withIsStateful(true)
      .withAdminState(AdminStateType.UNLOCKED)
      .withServiceState(ServiceStateType.RESERVED)
      .withDescribedByList(
        createSscValue("StartTime", convertToXml(reservation.getStartDateTime())),
        createSscValue("AdmissionControl", "Strict"));

    return rfsData;
  }

  public static String convertToXml(DateTime timeStamp) {
    return XmlUtils.toGregorianCalendar(timeStamp).toXMLFormat();
  }

  @VisibleForTesting
  static ServiceAccessPointType createServiceAccessPoint(NbiPort port, String reservationId) {
    ServiceAccessPointType sap = new org.tmforum.mtop.sb.xsd.svc.v1.ObjectFactory().createServiceAccessPointType()
      .withName(createComonObjectInfoTypeName("SAP", port.getNmsSapName()))
      .withResourceRef(new NamingAttributeType().withRdn(
        createRdn("MD", MANAGING_DOMAIN),
        createRdn("ME", port.getNmsNeId()),
        createRdn("PTP", MtosiUtils.extractPtpFromNmsPortId(port.getNmsPortId())),
        createRdn("CTP", CTP_PREFIX + reservationId)));

    return sap;
  }

}
