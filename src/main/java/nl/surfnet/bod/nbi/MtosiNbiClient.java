package nl.surfnet.bod.nbi;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.util.Environment;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmforum.mtop.fmw.xsd.gen.v1.AnyListType;
import org.tmforum.mtop.fmw.xsd.hdr.v1.CommunicationPatternType;
import org.tmforum.mtop.fmw.xsd.hdr.v1.CommunicationStyleType;
import org.tmforum.mtop.fmw.xsd.hdr.v1.Header;
import org.tmforum.mtop.fmw.xsd.hdr.v1.MessageTypeType;
import org.tmforum.mtop.fmw.xsd.nam.v1.NamingAttributeType;
import org.tmforum.mtop.fmw.xsd.nam.v1.RelativeDistinguishNameType;
import org.tmforum.mtop.sa.wsdl.sai.v1_0.ServiceActivationInterface;
import org.tmforum.mtop.sa.wsdl.scai.v1_0.ReserveException;
import org.tmforum.mtop.sa.wsdl.scai.v1_0.ServiceComponentActivationInterfaceHttp;
import org.tmforum.mtop.sa.xsd.scai.v1.ReserveRequest;
import org.tmforum.mtop.sb.xsd.svc.v1.AdminStateType;
import org.tmforum.mtop.sb.xsd.svc.v1.ResourceFacingServiceType;
import org.tmforum.mtop.sb.xsd.svc.v1.ServiceAccessPointType;
import org.tmforum.mtop.sb.xsd.svc.v1.ServiceCharacteristicValueType;
import org.tmforum.mtop.sb.xsd.svc.v1.ServiceStateType;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

import static nl.surfnet.bod.web.WebUtils.convertToXml;

public class MtosiNbiClient implements NbiClient {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Resource
  private Environment environment;

  private boolean shouldInit;

  private ServiceComponentActivationInterfaceHttp serviceComponentActivationInterfaceHttp;

  public MtosiNbiClient() {
    this(true);
  }

  @VisibleForTesting
  MtosiNbiClient(boolean shouldInit) {
    this.shouldInit = shouldInit;
    init();
  }

  @Override
  public boolean activateReservation(String reservationId) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public ReservationStatus cancelReservation(String scheduleId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public long getPhysicalPortsCount() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public Reservation createReservation(Reservation reservation, boolean autoProvision) {
    Holder<Header> mtopHeader = getServiceActivationRequestHeaders();
    ReserveRequest reserveRequest = createReservationRequest(reservation, autoProvision);

    try {
      serviceComponentActivationInterfaceHttp.getServiceComponentActivationInterfaceSoapHttp().reserve(mtopHeader,
          reserveRequest);
    }
    catch (ReserveException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return reservation;

  }

  ReserveRequest createReservationRequest(Reservation reservation, boolean autoProvision) {
    ReserveRequest reserveRequest = createReserveRequest();

    ResourceFacingServiceType rfsCreateData = createRfsCreateData();

    createDescribedByList(rfsCreateData.getDescribedByList(), reservation.getStartDateTime());

    createSapAndAddToList(rfsCreateData.getSapList(), reservation.getSourcePort().getPhysicalPort());

    createSapAndAddToList(rfsCreateData.getSapList(), reservation.getDestinationPort().getPhysicalPort());

//    createAndAddVendorExtensions(rfsCreateData, reservation.getStartDateTime());

    reserveRequest.setRfsCreateData(rfsCreateData);

    return reserveRequest;
  }

  private void createAndAddVendorExtensions(ResourceFacingServiceType rfsCreateData, DateTime startDateTime) {

    AnyListType startTimeAnyType = new org.tmforum.mtop.fmw.xsd.gen.v1.ObjectFactory().createAnyListType();
    startTimeAnyType.getAny().add(createNamingAttrib("startTime", convertToXml(startDateTime)));

    JAXBElement<AnyListType> vendorExtensions = new org.tmforum.mtop.fmw.xsd.coi.v1.ObjectFactory()
        .createCommonObjectInfoTypeVendorExtensions(startTimeAnyType);

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

  private org.tmforum.mtop.sa.xsd.scai.v1.ReserveRequest createReserveRequest() {
    return new org.tmforum.mtop.sa.xsd.scai.v1.ObjectFactory().createReserveRequest();
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

  @Override
  public List<PhysicalPort> findAllPhysicalPorts() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Optional<ReservationStatus> getReservationStatus(String scheduleId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PhysicalPort findPhysicalPortByNmsPortId(String nmsPortId) {
    // TODO Auto-generated method stub
    return null;
  }

  private void init() {
    if (shouldInit) {
      try {
        serviceComponentActivationInterfaceHttp = new ServiceComponentActivationInterfaceHttp(new URL(environment
            .getMtosiReserveEndPoint()), new QName("http://www.tmforum.org/mtop/sa/wsdl/scai/v1-0",
            "ServiceActivationInterfaceHttp"));

        final Map<String, Object> requestContext = ((BindingProvider) serviceComponentActivationInterfaceHttp
            .getPort(ServiceActivationInterface.class)).getRequestContext();

        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, environment.getMtosiReserveEndPoint());

        shouldInit = true;
      }
      catch (MalformedURLException e) {
        logger.error("Error: ", e);
      }
    }
  }

  /**
   * <v1:header> <v1:destinationURI>http://localhost:9006/mtosi/sa/
   * ServiceComponentActivation</v1:destinationURI>
   * <v1:communicationStyle>RPC</v1:communicationStyle>
   * <v1:timestamp>2012-11-26T00:00:00.000-05:00</v1:timestamp>
   * <v1:activityName>reserve</v1:activityName>
   * <v1:msgName>reserveRequest</v1:msgName>
   * <v1:senderURI>http://localhost:9009</v1:senderURI>
   * <v1:msgType>REQUEST</v1:msgType>
   * <v1:communicationPattern>SimpleResponse</v1:communicationPattern>
   * </v1:header>
   * 
   * @return
   */
  private Holder<Header> getServiceActivationRequestHeaders() {
    final Header header = new Header();
    header.setDestinationURI(environment.getMtosiReserveEndPoint());
    header.setCommunicationStyle(CommunicationStyleType.RPC);
    try {
      header.setTimestamp(DatatypeFactory.newInstance().newXMLGregorianCalendar());
    }
    catch (DatatypeConfigurationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    header.setActivityName("reserve");
    header.setMsgName("reserveRequest");
    header.setSenderURI("http://localhost:9009");
    header.setMsgType(MessageTypeType.REQUEST);
    header.setCommunicationPattern(CommunicationPatternType.SIMPLE_RESPONSE);

    return new Holder<Header>(header);
  }

}
