package nl.surfnet.bod.mtosi;

import javax.xml.ws.Holder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.tmforum.mtop.fmw.xsd.hdr.v1.CommunicationPatternType;
import org.tmforum.mtop.fmw.xsd.hdr.v1.CommunicationStyleType;
import org.tmforum.mtop.fmw.xsd.hdr.v1.Header;
import org.tmforum.mtop.fmw.xsd.hdr.v1.MessageTypeType;
import org.tmforum.mtop.fmw.xsd.nam.v1.NamingAttributeType;
import org.tmforum.mtop.fmw.xsd.nam.v1.RelativeDistinguishNameType;
import org.tmforum.mtop.mri.wsdl.rir.v1_0.GetInventoryException;
import org.tmforum.mtop.mri.wsdl.rir.v1_0.ResourceInventoryRetrievalHttp;
import org.tmforum.mtop.mri.xsd.rir.v1.GetInventoryRequest;
import org.tmforum.mtop.mri.xsd.rir.v1.GetInventoryResponse;
import org.tmforum.mtop.mri.xsd.rir.v1.GranularityType;
import org.tmforum.mtop.mri.xsd.rir.v1.ObjectFactory;
import org.tmforum.mtop.mri.xsd.rir.v1.SimpleFilterType;
import org.tmforum.mtop.mri.xsd.rir.v1.SimpleFilterType.IncludedObjectType;
import org.tmforum.mtop.nrf.xsd.invdata.v1.InventoryDataType;

@Service("mtosiLiveClient")
public class MtosiLiveClient {

  private final Logger log = LoggerFactory.getLogger(getClass());

  public InventoryDataType getInventory() {
    final GetInventoryRequest getInventoryRequest = new ObjectFactory().createGetInventoryRequest();
    getInventoryRequest.setFilter(getInventoryRequestSimpleFilter());
    try {
      final GetInventoryResponse inventory = new ResourceInventoryRetrievalHttp()
          .getResourceInventoryRetrievalSoapHttp().getInventory(getInventoryRequestHeaders(), getInventoryRequest);
      return inventory.getInventoryData();
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
    
    final SimpleFilterType simpleFilter = new ObjectFactory().createSimpleFilterType();
    
    final RelativeDistinguishNameType relativeDistinguishName = new RelativeDistinguishNameType();
    relativeDistinguishName.setType("MD");
    relativeDistinguishName.setValue("Ciena");

    final NamingAttributeType namingAttribute = new NamingAttributeType();
    namingAttribute.getRdn().add(relativeDistinguishName);
    
    simpleFilter.getBaseInstance().add(namingAttribute);

    final String[] objectTypes = { "ME", "EH", "EQ", "PTP" };

    for (String objectType : objectTypes) {
      final IncludedObjectType includeObject = new IncludedObjectType();
      includeObject.setObjectType(objectType);
      includeObject.setGranularity(GranularityType.ATTRS);
      simpleFilter.getIncludedObjectType().add(includeObject);
    }

    return simpleFilter;
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

    return new Holder<Header>(header);
  }
}
