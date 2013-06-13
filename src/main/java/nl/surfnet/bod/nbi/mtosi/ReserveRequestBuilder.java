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
package nl.surfnet.bod.nbi.mtosi;

import static nl.surfnet.bod.nbi.mtosi.MtosiUtils.createNamingAttrib;
import static nl.surfnet.bod.nbi.mtosi.MtosiUtils.createNamingAttributeType;
import static nl.surfnet.bod.nbi.mtosi.MtosiUtils.createRdn;
import static nl.surfnet.bod.nbi.mtosi.MtosiUtils.createSscValue;

import java.util.List;

import javax.xml.bind.JAXBElement;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.util.XmlUtils;

import org.joda.time.DateTime;
import org.tmforum.mtop.fmw.xsd.gen.v1.AnyListType;
import org.tmforum.mtop.fmw.xsd.nam.v1.NamingAttributeType;
import org.tmforum.mtop.sa.xsd.scai.v1.ReserveRequest;
import org.tmforum.mtop.sb.xsd.svc.v1.*;

import com.ciena.mtop.tmw.xsd.coi.v1.Nvs;
import com.google.common.annotations.VisibleForTesting;

public class ReserveRequestBuilder {

  static final String MANAGING_DOMAIN = "CIENA/OneControl";
  static final String TRAFFIC_MAPPING_TABLECOUNT = "1";
  static final String TRAFFIC_MAPPING_FROM_TABLE_PRIORITY = "all";
  static final String TRAFFIC_MAPPING_TO_TABLE_TRAFFICCLASS = "5";

  public ReserveRequest createReservationRequest(Reservation reservation, boolean autoProvision, long sequence) {

    ResourceFacingServiceType rfsData = createBasicRfsData(reservation, sequence);

    rfsData.getSapList().add(getSap(reservation, reservation.getSourcePort(), sequence));

    rfsData.getSapList().add(getSap(reservation, reservation.getDestinationPort(), sequence));

    rfsData.setVendorExtensions(createVendorExtensions(reservation.getStartDateTime()));

    ReserveRequest reserveRequest = createReserveRequest(reservation.getEndDateTime());

    reserveRequest.setRfsCreateData(rfsData);

    return reserveRequest;
  }

  private JAXBElement<AnyListType> createVendorExtensions(DateTime startDateTime) {
    Nvs nvs = new Nvs();
    nvs.setName("startTime");
    nvs.setValue(convertToXml(startDateTime));

    AnyListType anyListType = new org.tmforum.mtop.fmw.xsd.gen.v1.ObjectFactory().createAnyListType();
    anyListType.getAny().add(nvs);

    return new org.tmforum.mtop.fmw.xsd.coi.v1.ObjectFactory().createCommonObjectInfoTypeVendorExtensions(anyListType);
  }

  @VisibleForTesting
  ServiceAccessPointType getSap(Reservation reservation, VirtualPort virtualPort, long sequence) {

    ServiceAccessPointType sap = createServiceAccessPoint(virtualPort.getPhysicalPort(), sequence);

    List<ServiceCharacteristicValueType> describedByList = sap.getDescribedByList();

    describedByList.add(createSscValue("TrafficMappingTableCount", TRAFFIC_MAPPING_TABLECOUNT));
    describedByList.add(createSscValue("TrafficMappingFrom_Table_Priority", TRAFFIC_MAPPING_FROM_TABLE_PRIORITY));
    describedByList.add(createSscValue("TrafficMappingTo_Table_TrafficClass", TRAFFIC_MAPPING_TO_TABLE_TRAFFICCLASS));

    if (virtualPort.getVlanId() != null) {
      describedByList.add(createSscValue("ServiceType", "EVPL"));
      describedByList.add(createSscValue("TrafficMappingFrom_Table_VID", String.valueOf(virtualPort.getVlanId())));
    }
    else {
      describedByList.add(createSscValue("ServiceType", "EPL"));
      describedByList.add(createSscValue("TrafficMappingFrom_Table_VID", "all"));
    }

    describedByList.add(createSscValue("InterfaceType", virtualPort.getPhysicalPort().getPortType()));
    describedByList.add(createSscValue("TrafficMappingTo_Table_IngressCIR", reservation.getBandwidth().toString()));
    describedByList.add(createSscValue("ProtectionLevel", reservation.getProtectionType().getMtosiName()));

    return sap;
  }

  private ReserveRequest createReserveRequest(DateTime endDateTime) {
    ReserveRequest reserveRequest = new org.tmforum.mtop.sa.xsd.scai.v1.ObjectFactory().createReserveRequest();

    reserveRequest.setExpiringTime(XmlUtils.toGregorianCalendar(endDateTime));

    return reserveRequest;
  }

  @VisibleForTesting
  ResourceFacingServiceType createBasicRfsData(Reservation reservation, long sequence) {
    ResourceFacingServiceType rfsData = new org.tmforum.mtop.sb.xsd.svc.v1.ObjectFactory()
        .createResourceFacingServiceType();

    rfsData.setName(createNamingAttributeType("RFS", reservation.getReservationId()));

    rfsData.setIsMandatory(true);
    rfsData.setIsStateful(true);
    rfsData.setAdminState(AdminStateType.UNLOCKED);
    rfsData.setServiceState(ServiceStateType.RESERVED);

    rfsData.getDescribedByList().add(createSscValue("startTime", convertToXml(reservation.getStartDateTime())));
    rfsData.getDescribedByList().add(createSscValue("AdmissionControl", "Strict"));

    return rfsData;
  }

  public static String convertToXml(DateTime timeStamp) {
    return XmlUtils.toGregorianCalendar(timeStamp).toXMLFormat();
  }

  @VisibleForTesting
  ServiceAccessPointType createServiceAccessPoint(PhysicalPort port, long sequence) {
    NamingAttributeType resourceRef = createNamingAttrib();

    resourceRef.getRdn().add(createRdn("MD", MANAGING_DOMAIN));
    resourceRef.getRdn().add(createRdn("ME", port.getNmsNeId()));
    resourceRef.getRdn().add(createRdn("PTP", MtosiUtils.extractPTPFromNmsPortId(port.getNmsPortId())));

    ServiceAccessPointType sap = new org.tmforum.mtop.sb.xsd.svc.v1.ObjectFactory()
        .createServiceAccessPointType();
    sap.setName(createNamingAttributeType("SAP", port.getNmsSapName() + "-" + sequence));
    sap.setResourceRef(resourceRef);

    return sap;
  }

}