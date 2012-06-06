package nl.surfnet.bod.mtosi;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBElement;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tmforum.mtop.fmw.xsd.gen.v1.AnyListType;
import org.tmforum.mtop.fmw.xsd.hdr.v1.CommunicationPatternType;
import org.tmforum.mtop.fmw.xsd.hdr.v1.CommunicationStyleType;
import org.tmforum.mtop.fmw.xsd.hdr.v1.Header;
import org.tmforum.mtop.fmw.xsd.hdr.v1.MessageTypeType;
import org.tmforum.mtop.fmw.xsd.nam.v1.NamingAttributeType;
import org.tmforum.mtop.fmw.xsd.nam.v1.RelativeDistinguishNameType;
import org.tmforum.mtop.mri.wsdl.rir.v1_0.GetInventoryException;
import org.tmforum.mtop.mri.wsdl.rir.v1_0.ResourceInventoryRetrievalRPC;
import org.tmforum.mtop.mri.xsd.rir.v1.GetInventoryRequest;
import org.tmforum.mtop.mri.xsd.rir.v1.GranularityType;
import org.tmforum.mtop.mri.xsd.rir.v1.ObjectFactory;
import org.tmforum.mtop.mri.xsd.rir.v1.SimpleFilterType;
import org.tmforum.mtop.mri.xsd.rir.v1.SimpleFilterType.IncludedObjectType;
import org.tmforum.mtop.nrf.xsd.invdata.v1.InventoryDataType;
import org.tmforum.mtop.nrf.xsd.invdata.v1.ManagedElementInventoryType;
import org.tmforum.mtop.nrf.xsd.invdata.v1.ManagementDomainInventoryType;
import org.w3c.dom.Node;

@Service("mtosiLiveClient")
public class MtosiLiveClient {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private ResourceInventoryRetrievalRPC resourceInventoryRetrievalRpcPort = null;
  private final GetInventoryRequest getInventoryRequest = new ObjectFactory().createGetInventoryRequest();

  @Value("${mtosi.inventory.retrieval.endpoint}")
  private String resourceInventoryRetrievalUrl;

  @PostConstruct
  public void init() {
    log.info("Starting");
    try {
      final BodResourceInventoryRetrieval bodResourceInventoryRetrieval = new BodResourceInventoryRetrieval();
      getInventoryRequest.setFilter(getInventoryRequestSimpleFilter());
      resourceInventoryRetrievalRpcPort = bodResourceInventoryRetrieval.getPort(ResourceInventoryRetrievalRPC.class);
      final Map<String, Object> requestContext = ((BindingProvider) resourceInventoryRetrievalRpcPort)
          .getRequestContext();
      requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, resourceInventoryRetrievalUrl);
    }
    catch (IOException e) {
      log.error("Error: ", e);
    }
  }

  public InventoryDataType getInventory() {
    log.info("Retrieving inventory at: {}", resourceInventoryRetrievalUrl);
    try {

      return resourceInventoryRetrievalRpcPort.getInventory(getInventoryRequestHeaders(), getInventoryRequest)
          .getInventoryData();
    }
    catch (GetInventoryException e) {
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
    // all this ^$%# for just getting the bloody NE names and macs ....
    final HashMap<String, String> ports = new HashMap<String, String>();
    final List<ManagementDomainInventoryType> mds = getInventory().getMdList().getMd();
    for (final ManagementDomainInventoryType md : mds) {
      final List<ManagedElementInventoryType> meInvs = md.getMeList().getMeInv();

      String hostname = "";

      for (final ManagedElementInventoryType meInv : meInvs) {
        final List<RelativeDistinguishNameType> rdns = meInv.getMeAttrs().getName().getValue().getRdn();
        for (final RelativeDistinguishNameType rdn : rdns) {
          if ("ME".equals(rdn.getType())) {
            hostname = rdn.getValue();
          }
          log.debug("Rdn type: {}", rdn.getType());
          log.debug("Rdn value: {}", rdn.getValue());
        }
        final JAXBElement<AnyListType> vendorExtensions = meInv.getMeAttrs().getVendorExtensions();

        if (vendorExtensions.isNil()) {
          log.info("Vendor extensions are null");
        }
        else {
          final List<Object> verndorExtensions = vendorExtensions.getValue().getAny();
          for (final Object vendorExtension : verndorExtensions) {
            final Node child = (Node) vendorExtension;
            log.debug("Child node: " + child.getNodeName());
            final String value = child.getFirstChild().getTextContent();
            log.debug("Value: {}", value);
            if ("meMacAddress".equals(child.getNodeName())) {
              ports.put(hostname, value);
            }
          }
        }
      }
    }
    return ports;
  }

  public long getUnallocatedMtosiPortCount() {
    return getUnallocatedPorts().size();
  }

  private final Holder<Header> getInventoryRequestHeaders() {
    final Header header = new Header();
    header.setDestinationURI(resourceInventoryRetrievalUrl);
    header.setCommunicationPattern(CommunicationPatternType.SIMPLE_RESPONSE);
    header.setCommunicationStyle(CommunicationStyleType.RPC);
    header.setActivityName("getInventory");
    header.setMsgName("getInventoryRequest");
    header.setSenderURI("http://62.190.191.48:9009");
    header.setMsgType(MessageTypeType.REQUEST);
    log.debug("header: {}", header);
    return new Holder<Header>(header);
  }
  
}
