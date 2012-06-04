package nl.surfnet.bod.mtosi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBElement;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import org.apache.xerces.dom.ChildNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.tmforum.mtop.fmw.xsd.gen.v1.AnyListType;
import org.tmforum.mtop.fmw.xsd.hdr.v1.CommunicationPatternType;
import org.tmforum.mtop.fmw.xsd.hdr.v1.CommunicationStyleType;
import org.tmforum.mtop.fmw.xsd.hdr.v1.Header;
import org.tmforum.mtop.fmw.xsd.hdr.v1.MessageTypeType;
import org.tmforum.mtop.fmw.xsd.nam.v1.NamingAttributeType;
import org.tmforum.mtop.fmw.xsd.nam.v1.RelativeDistinguishNameType;
import org.tmforum.mtop.mri.wsdl.rir.v1_0.ResourceInventoryRetrievalRPC;
import org.tmforum.mtop.mri.xsd.rir.v1.GetInventoryRequest;
import org.tmforum.mtop.mri.xsd.rir.v1.GranularityType;
import org.tmforum.mtop.mri.xsd.rir.v1.ObjectFactory;
import org.tmforum.mtop.mri.xsd.rir.v1.SimpleFilterType;
import org.tmforum.mtop.mri.xsd.rir.v1.SimpleFilterType.IncludedObjectType;
import org.tmforum.mtop.nrf.xsd.invdata.v1.InventoryDataType;
import org.tmforum.mtop.nrf.xsd.invdata.v1.ManagedElementInventoryType;
import org.tmforum.mtop.nrf.xsd.invdata.v1.ManagementDomainInventoryType;

@Service("mtosiLiveClient")
public class MtosiLiveClient {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private ResourceInventoryRetrievalRPC resourceInventoryRetrievalRPCPort = null;
  private final GetInventoryRequest getInventoryRequest = new ObjectFactory().createGetInventoryRequest();

  
  // TODO: Get from prop file
  private final String resourceInventoryRetrievalUrl = "http://localhost:8088/mtosi/mri/ResourceInventoryRetrieval";

  @PostConstruct
  public void init() {
    log.info("Starting");
    try {
      final BodResourceInventoryRetrieval bodResourceInventoryRetrieval = new BodResourceInventoryRetrieval();
      getInventoryRequest.setFilter(getInventoryRequestSimpleFilter());
      resourceInventoryRetrievalRPCPort = bodResourceInventoryRetrieval.getPort(ResourceInventoryRetrievalRPC.class);
      final Map<String, Object> requestContext = ((BindingProvider) resourceInventoryRetrievalRPCPort)
          .getRequestContext();

      requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, resourceInventoryRetrievalUrl);

    }
    catch (Exception e) {
      log.error("Error: ", e);
    }
  }

  public InventoryDataType getInventory() {
    log.info("Starting");
    try {

      return resourceInventoryRetrievalRPCPort.getInventory(getInventoryRequestHeaders(), getInventoryRequest)
          .getInventoryData();
    }
    catch (Exception e) {
      log.error("Error: ", e);
      return null;
    }
  }

  /**
   * @return
   */
  private SimpleFilterType getInventoryRequestSimpleFilter() {

    //
    // <v11:filter
    // <v11:baseInstance>
    // <v12:rdn>
    // <v12:type>MD</v12:type>
    // <v12:value>Ciena</v12:value>
    // </v12:rdn>
    // </v11:baseInstance>
    // <v11:includedObjectType>
    // <v11:objectType>ME</v11:objectType>
    // <v11:granularity>ATTRS</v11:granularity>
    // </v11:includedObjectType>
    // <v11:includedObjectType>
    // <v11:objectType>EH</v11:objectType>
    // <v11:granularity>ATTRS</v11:granularity>
    // </v11:includedObjectType>
    // <v11:includedObjectType>
    // <v11:objectType>EQ</v11:objectType>
    // <v11:granularity>ATTRS</v11:granularity>
    // </v11:includedObjectType>
    // <v11:includedObjectType>
    // <v11:objectType>PTP</v11:objectType>
    // <v11:granularity>ATTRS</v11:granularity>
    // </v11:includedObjectType>
    // </v11:filter>
    //

    // baseInstance
    final RelativeDistinguishNameType relativeDistinguishName = new RelativeDistinguishNameType();
    relativeDistinguishName.setType("MD");
    relativeDistinguishName.setValue("Ciena");

    final NamingAttributeType namingAttribute = new NamingAttributeType();
    namingAttribute.getRdn().add(relativeDistinguishName);

    final SimpleFilterType simpleFilter = new ObjectFactory().createSimpleFilterType();
    simpleFilter.getBaseInstance().add(namingAttribute);

    // includedObjectTypes
    final String[] objectTypes = { "ME", "EH", "EQ", "PTP" };

    for (final String objectType : objectTypes) {
      final IncludedObjectType includeObject = new IncludedObjectType();
      includeObject.setObjectType(objectType);
      includeObject.setGranularity(GranularityType.ATTRS);
      simpleFilter.getIncludedObjectType().add(includeObject);
    }
    log.info("returning: {}", simpleFilter);
    return simpleFilter;
  }

  public HashMap<String, String> getUnallocatedPorts() {
    // all this ^$%# for just getting the bloody NE names ....
    final List<ManagementDomainInventoryType> mds = new MtosiLiveClient().getInventory().getMdList().getMd();

    int macCounter = 0;
    HashMap<String, String> ports = new HashMap<String, String>();
    for (final ManagementDomainInventoryType md : mds) {
      final List<ManagedElementInventoryType> meInvs = md.getMeList().getMeInv();
      for (final ManagedElementInventoryType meInv : meInvs) {
        final List<RelativeDistinguishNameType> rdns = meInv.getMeAttrs().getName().getValue().getRdn();

        String elementName = null;
        String macAddress = null;
        for (final RelativeDistinguishNameType rdn : rdns) {
          elementName = null;
          if ("ME".equals(rdn.getType())) {
            elementName = rdn.getValue();
          }

        }
        final JAXBElement<AnyListType> vendorExtensions = meInv.getMeAttrs().getVendorExtensions();

        if (vendorExtensions.isNil()) {
          log.info("Vendor extensions are null");
          macAddress = null;
        }
        else {
          final List<Object> some = vendorExtensions.getValue().getAny();
          for (final Object o : some) {
            macAddress = String.valueOf((macCounter++));

            log.info("Some vendor extension value: {}", o);
          }
        }

        if (elementName != null) {
          ports.put(elementName, macAddress);
        }
      }
    }

    log.info("Port: {}", ports);
    return ports;
  }

  public long getUnallocatedMTOSIEPortCount() {
    // FIXME
    return 10L;
  }

  private final Holder<Header> getInventoryRequestHeaders() {
    // <v1:header>
    // <v1:destinationURI>http://62.190.191.48:9006/mtosi/mri/ResourceInventoryRetrieval</v1:destinationURI>
    // <v1:communicationPattern>SimpleResponse</v1:communicationPattern>
    // <v1:communicationStyle>RPC</v1:communicationStyle>
    // <v1:activityName>getInventory</v1:activityName>
    // <v1:msgName>getInventoryRequest</v1:msgName>
    // <v1:senderURI>http://62.190.191.48:9009</v1:senderURI>
    // <v1:msgType>REQUEST</v1:msgType>a
    // </v1:header>

    final Header header = new Header();
    header.setDestinationURI("http://62.190.191.48:9006/mtosi/mri/ResourceInventoryRetrieval");
    header.setCommunicationPattern(CommunicationPatternType.SIMPLE_RESPONSE);
    header.setCommunicationStyle(CommunicationStyleType.RPC);
    header.setActivityName("getInventory");
    header.setMsgName("getInventoryRequest");
    header.setSenderURI("http://62.190.191.48:9009");
    header.setMsgType(MessageTypeType.REQUEST);
    log.info("header: {}", header);
    return new Holder<Header>(header);
  }

  public static void main(final String... args) {
    final Logger log = LoggerFactory.getLogger(MtosiLiveClient.class);

    // all this ^$%# for just getting the bloody NE names ....
    final MtosiLiveClient mtosiLiveClient = new MtosiLiveClient();
    mtosiLiveClient.init();

    final List<ManagementDomainInventoryType> mds = mtosiLiveClient.getInventory().getMdList().getMd();
    for (final ManagementDomainInventoryType md : mds) {
      final List<ManagedElementInventoryType> meInvs = md.getMeList().getMeInv();
      for (final ManagedElementInventoryType meInv : meInvs) {
        final List<RelativeDistinguishNameType> rdns = meInv.getMeAttrs().getName().getValue().getRdn();
        for (final RelativeDistinguishNameType rdn : rdns) {
          log.info("Rdn type: {}", rdn.getType());
          log.info("Rdn value: {}", rdn.getValue());
        }
        final JAXBElement<AnyListType> vendorExtensions = meInv.getMeAttrs().getVendorExtensions();

        if (vendorExtensions.isNil()) {
          log.info("Vendor extensions are null");
        }
        else {
          final List<Object> some = vendorExtensions.getValue().getAny();
          for (final Object o : some) {
            final ChildNode node = (ChildNode) o;
            if (node.getNodeName().equals("meMacAddress")) {
              log.info("Mac adress node found");
              log.info("Mac address value: {}", node.getNodeValue());
            }
          }
        }
      }
    }
  }

}
