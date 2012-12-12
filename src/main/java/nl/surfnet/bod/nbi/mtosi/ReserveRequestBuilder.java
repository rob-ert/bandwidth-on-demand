package nl.surfnet.bod.nbi.mtosi;

import java.util.List;

import javax.xml.bind.JAXBElement;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.util.XmlUtils;

import org.joda.time.DateTime;
import org.tmforum.mtop.fmw.xsd.gen.v1.AnyListType;
import org.tmforum.mtop.fmw.xsd.nam.v1.NamingAttributeType;
import org.tmforum.mtop.fmw.xsd.nam.v1.RelativeDistinguishNameType;
import org.tmforum.mtop.sa.xsd.scai.v1.ReserveRequest;
import org.tmforum.mtop.sb.xsd.svc.v1.*;

public class ReserveRequestBuilder {

  public ReserveRequest createReservationRequest(Reservation reservation, boolean autoProvision) {
    ReserveRequest reserveRequest = createReserveRequest(reservation.getEndDateTime());

    ResourceFacingServiceType rfsCreateData = createRfsCreateData();

    createDescribedByList(rfsCreateData.getDescribedByList(), reservation.getStartDateTime());

    createSapAndAddToList(rfsCreateData.getSapList(), reservation.getSourcePort().getPhysicalPort());

    createSapAndAddToList(rfsCreateData.getSapList(), reservation.getDestinationPort().getPhysicalPort());

    createVendorExtensionsAndAdd(rfsCreateData, reservation.getStartDateTime());

    reserveRequest.setRfsCreateData(rfsCreateData);

    return reserveRequest;
  }

  private void createVendorExtensionsAndAdd(ResourceFacingServiceType rfsCreateData, DateTime startDateTime) {

    AnyListType anyListType = new org.tmforum.mtop.fmw.xsd.gen.v1.ObjectFactory().createAnyListType();

    List<Object> anyList = anyListType.getAny();

    JAXBElement<NamingAttributeType> start = new org.tmforum.mtop.fmw.xsd.coi.v1.ObjectFactory()
        .createCommonObjectInfoTypeName(createNamingAttrib("startTime", convertToXml(startDateTime)));
    anyList.add(start);

    JAXBElement<AnyListType> vendorExtensions = new org.tmforum.mtop.fmw.xsd.coi.v1.ObjectFactory()
        .createCommonObjectInfoTypeVendorExtensions(anyListType);

    rfsCreateData.setVendorExtensions(vendorExtensions);
  }

  private void createSapAndAddToList(List<ServiceAccessPointType> sapList, PhysicalPort physicalPort) {
    ServiceAccessPointType serviceAccessPoint = createServiceAccessPoint(physicalPort);
    sapList.add(serviceAccessPoint);

    createServiceCharacsteristicsAndAddToList("UNI-N", createNamingAttrib("SSC", "InterfaceType"), serviceAccessPoint
        .getDescribedByList());
    createServiceCharacsteristicsAndAddToList("1", createNamingAttrib("SSC", "TrafficMappingTableCount"),
        serviceAccessPoint.getDescribedByList());
    createServiceCharacsteristicsAndAddToList("5", createNamingAttrib("SSC", "TrafficMappingFrom_Table_VID"),
        serviceAccessPoint.getDescribedByList());
    createServiceCharacsteristicsAndAddToList("all", createNamingAttrib("SSC", "TrafficMappingFrom_Table_Priority"),
        serviceAccessPoint.getDescribedByList());
    createServiceCharacsteristicsAndAddToList("4", createNamingAttrib("SSC", "TrafficMappingTo_Table_TrafficClass"),
        serviceAccessPoint.getDescribedByList());
    createServiceCharacsteristicsAndAddToList("250", createNamingAttrib("SSC", "TrafficMappingTo_Table_IngressCIR"),
        serviceAccessPoint.getDescribedByList());

  }

  private org.tmforum.mtop.sa.xsd.scai.v1.ReserveRequest createReserveRequest(DateTime endDateTime) {
    ReserveRequest reserveRequest = new org.tmforum.mtop.sa.xsd.scai.v1.ObjectFactory().createReserveRequest();

    reserveRequest.setExpiringTime(XmlUtils.getXmlTimeStampFromDateTime(endDateTime).get());

    return reserveRequest;
  }

  private ResourceFacingServiceType createRfsCreateData() {
    ResourceFacingServiceType rfsData = new org.tmforum.mtop.sb.xsd.svc.v1.ObjectFactory()
        .createResourceFacingServiceType();

    rfsData.setName(createNamingAttributeType("RFS", "mtosiRFSTestEVPL1"));
    rfsData.setIsMandatory(true);
    rfsData.setIsStateful(true);
    rfsData.setAdminState(AdminStateType.UNLOCKED);
    rfsData.setServiceState(ServiceStateType.RESERVED);

    return rfsData;
  }

  private void createDescribedByList(List<ServiceCharacteristicValueType> describedByList, DateTime timeStamp) {
    createServiceCharacsteristicsAndAddToList("EVPL", createNamingAttrib("SSC", "ServiceType"), describedByList);

    createServiceCharacsteristicsAndAddToList(convertToXml(timeStamp), createNamingAttrib("SSC", "startTime"),
        describedByList);

    createServiceCharacsteristicsAndAddToList("Strict", createNamingAttrib("SSC", "AdmissionControl"), describedByList);

    createServiceCharacsteristicsAndAddToList("Fully Protected", createNamingAttrib("SSC", "ProtectionLevel"),
        describedByList);
  }

  public static String convertToXml(DateTime timeStamp) {
    return XmlUtils.getXmlTimeStampFromDateTime(timeStamp).get().toXMLFormat();
  }

  private ServiceAccessPointType createServiceAccessPoint(PhysicalPort port) {
    ServiceAccessPointType serviceAccessPoint = new org.tmforum.mtop.sb.xsd.svc.v1.ObjectFactory()
        .createServiceAccessPointType();

    NamingAttributeType resourceRef = createNamingAttrib(null, null);
    serviceAccessPoint.setResourceRef(resourceRef);

    serviceAccessPoint.setName(createNamingAttributeType("SAP", port.getNmsSapName()));

    List<RelativeDistinguishNameType> resourceRefList = serviceAccessPoint.getResourceRef().getRdn();
    resourceRefList.add(createRdn("MD", "CIENA/OneControl"));
    resourceRefList.add(createRdn("ME", port.getNmsNeId()));
    resourceRefList.add(createRdn("PTP", "/rack=1/shelf=1/slot=1/port=4"));
    resourceRefList.add(createRdn("CTP", "/eth=mtosiRFSTestEVPL1"));

    return serviceAccessPoint;
  }

  private JAXBElement<NamingAttributeType> createNamingAttributeType(String type, String value) {
    return new org.tmforum.mtop.fmw.xsd.coi.v1.ObjectFactory().createCommonObjectInfoTypeName(createNamingAttrib(type,
        value));
  }

  private void createServiceCharacsteristicsAndAddToList(String value, NamingAttributeType namingAttributeType,
      List<ServiceCharacteristicValueType> list) {
    ServiceCharacteristicValueType serviceCharacteristicValueType = new org.tmforum.mtop.sb.xsd.svc.v1.ObjectFactory()
        .createServiceCharacteristicValueType();
    serviceCharacteristicValueType.setValue(value);

    serviceCharacteristicValueType.setSscRef(namingAttributeType);
    list.add(serviceCharacteristicValueType);
  }

  private RelativeDistinguishNameType createRdn(String type, String value) {
    RelativeDistinguishNameType rel = new RelativeDistinguishNameType();
    rel.setType(type);
    rel.setValue(value);
    return rel;
  }

  private NamingAttributeType createNamingAttrib(String type, String value) {
    NamingAttributeType namingAttributeType = new NamingAttributeType();

    namingAttributeType.getRdn().add(createRdn(type, value));

    return namingAttributeType;
  }

}
