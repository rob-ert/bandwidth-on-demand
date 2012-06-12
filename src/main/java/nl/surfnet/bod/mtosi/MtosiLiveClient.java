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
package nl.surfnet.bod.mtosi;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tmforum.mtop.fmw.xsd.hdr.v1.CommunicationPatternType;
import org.tmforum.mtop.fmw.xsd.hdr.v1.CommunicationStyleType;
import org.tmforum.mtop.fmw.xsd.hdr.v1.Header;
import org.tmforum.mtop.fmw.xsd.hdr.v1.MessageTypeType;
import org.tmforum.mtop.fmw.xsd.nam.v1.NamingAttributeType;
import org.tmforum.mtop.fmw.xsd.nam.v1.RelativeDistinguishNameType;
import org.tmforum.mtop.mri.wsdl.rir.v1_0.GetInventoryException;
import org.tmforum.mtop.mri.wsdl.rir.v1_0.ResourceInventoryRetrievalRPC;
import org.tmforum.mtop.mri.xsd.rir.v1.*;
import org.tmforum.mtop.mri.xsd.rir.v1.SimpleFilterType.IncludedObjectType;
import org.tmforum.mtop.nrf.xsd.invdata.v1.InventoryDataType;
import org.tmforum.mtop.nrf.xsd.invdata.v1.ManagedElementInventoryType;
import org.tmforum.mtop.nrf.xsd.invdata.v1.ManagementDomainInventoryType;

@Service("mtosiLiveClient")
public class MtosiLiveClient {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private ResourceInventoryRetrievalRPC resourceInventoryRetrievalRpcPort = null;

  private final GetInventoryRequest getInventoryRequest = new ObjectFactory().createGetInventoryRequest();
  private final String resourceInventoryRetrievalUrl;
  private final String senderUri;

  @Autowired
  public MtosiLiveClient(@Value("${mtosi.inventory.retrieval.endpoint}") String retrievalUrl,
      @Value("${mtosi.inventory.sender.uri}") String senderUri) {
    this.resourceInventoryRetrievalUrl = retrievalUrl;
    this.senderUri = senderUri;

    log.info("Using ws at: {}", resourceInventoryRetrievalUrl);
  }

  @PostConstruct
  public void init() {
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
    // baseInstance
    final RelativeDistinguishNameType relativeDistinguishName = new RelativeDistinguishNameType();
    relativeDistinguishName.setType("MD");
    relativeDistinguishName.setValue("Ciena");

    final NamingAttributeType namingAttribute = new NamingAttributeType();
    namingAttribute.getRdn().add(relativeDistinguishName);

    final SimpleFilterType simpleFilter = new ObjectFactory().createSimpleFilterType();
    simpleFilter.getBaseInstance().add(namingAttribute);

    // includedObjectTypes, maybe we only need EH or maybe don;t use a filter
    // after all
    final String[] objectTypes = { "ME", "EH", "EQ", "PTP" };

    for (final String objectType : objectTypes) {
      final IncludedObjectType includeObject = new IncludedObjectType();
      includeObject.setObjectType(objectType);
      includeObject.setGranularity(GranularityType.ATTRS);
      simpleFilter.getIncludedObjectType().add(includeObject);
    }
    // log.info("returning: {}", simpleFilter);
    return simpleFilter;
  }

  public HashMap<String, String> getUnallocatedPorts() {
    // all this ^$%# for just getting the bloody NE names and macs ....
    final HashMap<String, String> ports = new HashMap<String, String>();
    final List<ManagementDomainInventoryType> mds = getInventory().getMdList().getMd();
    for (final ManagementDomainInventoryType md : mds) {

      final List<ManagedElementInventoryType> meInvs = md.getMeList().getMeInv();

      for (final ManagedElementInventoryType meInv : meInvs) {
        final String macAddress = meInv.getMeNm();
        final String userLabel = meInv.getMeAttrs().getUserLabel().getValue();
        ports.put(userLabel, macAddress);
      }
    }
    return ports;
  }

  public long getUnallocatedMtosiPortCount() {
    return getUnallocatedPorts().size();
  }

  private Holder<Header> getInventoryRequestHeaders() {
    final Header header = new Header();
    header.setDestinationURI(resourceInventoryRetrievalUrl);
    header.setCommunicationPattern(CommunicationPatternType.SIMPLE_RESPONSE);
    header.setCommunicationStyle(CommunicationStyleType.RPC);
    header.setActivityName("getInventory");
    header.setMsgName("getInventoryRequest");
    header.setSenderURI(senderUri);
    header.setMsgType(MessageTypeType.REQUEST);
    log.debug("header: {}", header);

    return new Holder<Header>(header);
  }

  public static void main(String... args) {
    final MtosiLiveClient mtosiLiveClient = new MtosiLiveClient(
        "http://localhost:9006/mtosi/mri/ResourceInventoryRetrieval", "http://localhost:9009");
    mtosiLiveClient.init();
    mtosiLiveClient.getUnallocatedPorts();
  }

}
