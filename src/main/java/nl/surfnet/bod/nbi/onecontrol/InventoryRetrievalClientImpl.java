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

import static nl.surfnet.bod.nbi.onecontrol.MtosiUtils.findRdnValue;
import static nl.surfnet.bod.nbi.onecontrol.MtosiUtils.findSscValue;
import static nl.surfnet.bod.nbi.onecontrol.MtosiUtils.getSapName;

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.sun.xml.ws.developer.JAXWSProperties;

import nl.surfnet.bod.domain.NbiPort;
import nl.surfnet.bod.domain.NbiPort.InterfaceType;
import nl.surfnet.bod.nbi.onecontrol.OneControlInstance.OneControlConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.tmforum.mtop.fmw.xsd.hdr.v1.Header;
import org.tmforum.mtop.msi.wsdl.sir.v1_0.GetServiceInventoryException;
import org.tmforum.mtop.msi.wsdl.sir.v1_0.ServiceInventoryRetrievalHttp;
import org.tmforum.mtop.msi.wsdl.sir.v1_0.ServiceInventoryRetrievalRPC;
import org.tmforum.mtop.msi.xsd.sir.v1.GetServiceInventoryRequest;
import org.tmforum.mtop.msi.xsd.sir.v1.GetServiceInventoryResponse;
import org.tmforum.mtop.msi.xsd.sir.v1.GranularityType;
import org.tmforum.mtop.msi.xsd.sir.v1.ObjectFactory;
import org.tmforum.mtop.msi.xsd.sir.v1.ServiceInventoryDataType.RfsList;
import org.tmforum.mtop.msi.xsd.sir.v1.ServiceInventoryDataType.SapList;
import org.tmforum.mtop.msi.xsd.sir.v1.SimpleServiceFilterType;
import org.tmforum.mtop.sb.xsd.svc.v1.ServiceAccessPointType;

@Profile("onecontrol")
@Service
public class InventoryRetrievalClientImpl implements InventoryRetrievalClient {

  private static final String WSDL_LOCATION = "/mtosi/2.1/DDPs/ManageServiceInventory/IIS/wsdl/ServiceInventoryRetrieval/ServiceInventoryRetrievalHttp.wsdl";

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Resource private OneControlInstance oneControlInstance;

  @Value("${onecontrol.inventory.client.connect.timeout}")
  private int connectTimeout;

  @Value("${onecontrol.inventory.client.request.timeout}")
  private int requestTimeout;

  @Override
  public List<NbiPort> getPhysicalPorts() {
    Optional<SapList> inventory = getSapInventory();
    if (!inventory.isPresent()) {
      return Collections.emptyList();
    }

    return FluentIterable
      .from(getSapInventory().get().getSap())
      .transform(new Function<ServiceAccessPointType, NbiPort>() {
        @Override
        public NbiPort apply(ServiceAccessPointType sap) {
          return translateToNbiPort(sap);
        }
      })
      .toList();
  }

  @Override
  public int getPhysicalPortCount() {
    Optional<SapList> inventory = getSapInventory();
    return inventory.isPresent() ? inventory.get().getSap().size() : 0;
  }

  @VisibleForTesting
  GetServiceInventoryResponse getServiceInventoryWithRfsFilter() throws GetServiceInventoryException {
    GetServiceInventoryRequest inventoryRequest = new ObjectFactory().createGetServiceInventoryRequest();
    addRfsFilter(inventoryRequest);

    return retrieveServiceInventory(inventoryRequest);
  }

  @VisibleForTesting
  GetServiceInventoryResponse getServiceInventoryWithSapFilter() throws GetServiceInventoryException {
    GetServiceInventoryRequest inventoryRequest = new ObjectFactory().createGetServiceInventoryRequest();
    addSapFilter(inventoryRequest);

    return retrieveServiceInventory(inventoryRequest);
  }

  @VisibleForTesting
  NbiPort translateToNbiPort(ServiceAccessPointType sap) {
    String nmsSapName = getSapName(sap);
    String managedElement = findRdnValue("ME", sap.getResourceRef()).get();
    String ptp = findRdnValue("PTP", sap.getResourceRef()).get();
    String nmsPortSpeed = findSscValue("AdministrativeSpeedRate", sap.getDescribedByList()).get();
    String supportedServiceType = findSscValue("SupportedServices", sap.getDescribedByList()).get();
    InterfaceType interfaceType = InterfaceType.valueOf(findSscValue("InterfaceType", sap.getDescribedByList()).get().replace('-', '_'));
    boolean isVlanRequired = determineVlanRequired(supportedServiceType);

    NbiPort port = new NbiPort();
    port.setNmsPortId(MtosiUtils.composeNmsPortId(managedElement, MtosiUtils.convertToShortPtP(ptp)));
    port.setNmsSapName(nmsSapName);
    port.setNmsNeId(managedElement);
    port.setNmsPortSpeed(nmsPortSpeed);
    port.setSupportedServiceType(supportedServiceType);
    port.setInterfaceType(interfaceType);
    port.setSignalingType("NA");
    port.setVlanRequired(isVlanRequired);
    port.setSuggestedBodPortId(nmsSapName);
    port.setSuggestedNocLabel(managedElement + "@" +  MtosiUtils.convertToShortPtP(ptp));

    logger.debug("Retrieved physicalport: {}", port);

    return port;
  }

  @VisibleForTesting
  boolean determineVlanRequired(String supportedServiceType) {
    return "EVPL".equals(supportedServiceType) || "EVPLAN".equals(supportedServiceType);
  }

  private void addSapFilter(GetServiceInventoryRequest request) {
    addFilter(request, "SAP");
  }

  private void addRfsFilter(GetServiceInventoryRequest request) {
    addFilter(request, "RFS");
  }

  private void addFilter(GetServiceInventoryRequest request, String filter) {
    request.setFilter(getInventoryRequestSimpleFilter(filter));
  }

  private SimpleServiceFilterType getInventoryRequestSimpleFilter(String serviceObjectType) {
    SimpleServiceFilterType.Scope scope = new ObjectFactory().createSimpleServiceFilterTypeScope()
      .withServiceObjectType(serviceObjectType);

    SimpleServiceFilterType simpleFilter = new ObjectFactory().createSimpleServiceFilterType()
      .withScopeAndSelection(GranularityType.FULL)
      .withScopeAndSelection(scope);

    return simpleFilter;
  }

  private Optional<SapList> getSapInventory() {
    try {
      return Optional.fromNullable(getServiceInventoryWithSapFilter().getInventoryData().getSapList());
    } catch (GetServiceInventoryException e) {
      logger.warn("Could not load SAP inventory", e);
      return Optional.absent();
    }
  }

  @Override
  public Optional<RfsList> getRfsInventory() {
    try {
      return Optional.fromNullable(getServiceInventoryWithRfsFilter().getInventoryData().getRfsList());
    } catch (GetServiceInventoryException e) {
      logger.warn("Could not load RFS inventory", e);
      return Optional.absent();
    }
  }

  private GetServiceInventoryResponse retrieveServiceInventory(GetServiceInventoryRequest inventoryRequest) throws GetServiceInventoryException {
    OneControlConfiguration configuration = oneControlInstance.getCurrentConfiguration();

    Holder<Header> header = HeaderBuilder.buildInventoryHeader(configuration);

    return createPort(configuration).getServiceInventory(header, inventoryRequest);
  }

  private ServiceInventoryRetrievalRPC createPort(OneControlConfiguration configuration) {
    ServiceInventoryRetrievalRPC port = new ServiceInventoryRetrievalHttp(this.getClass().getResource(WSDL_LOCATION)).getServiceInventoryRetrievalSoapHttp();
    BindingProvider bindingProvider = (BindingProvider) port;
    bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, configuration.getInventoryRetrievalEndpoint());
    bindingProvider.getRequestContext().put(JAXWSProperties.CONNECT_TIMEOUT, connectTimeout);
    bindingProvider.getRequestContext().put(JAXWSProperties.REQUEST_TIMEOUT, requestTimeout);
    return port;
  }

}