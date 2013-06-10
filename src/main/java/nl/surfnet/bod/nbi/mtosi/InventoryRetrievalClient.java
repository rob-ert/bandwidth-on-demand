/**
 * Copyright (c) 2012, SURFnet BV
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

import static nl.surfnet.bod.nbi.mtosi.MtosiUtils.findRdnValue;
import static nl.surfnet.bod.nbi.mtosi.MtosiUtils.findSscValue;
import static nl.surfnet.bod.nbi.mtosi.MtosiUtils.getSapName;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import nl.surfnet.bod.domain.PhysicalPort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.tmforum.mtop.msi.wsdl.sir.v1_0.GetServiceInventoryException;
import org.tmforum.mtop.msi.wsdl.sir.v1_0.ServiceInventoryRetrievalHttp;
import org.tmforum.mtop.msi.wsdl.sir.v1_0.ServiceInventoryRetrievalRPC;
import org.tmforum.mtop.msi.xsd.sir.v1.*;
import org.tmforum.mtop.msi.xsd.sir.v1.ServiceInventoryDataType.RfsList;
import org.tmforum.mtop.msi.xsd.sir.v1.ServiceInventoryDataType.SapList;
import org.tmforum.mtop.sb.xsd.svc.v1.ServiceAccessPointType;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.FluentIterable;

@Service
@Lazy
public class InventoryRetrievalClient {

  private static final String SAP_CACHE = "sapCache";
  private static final String RFS_CACHE = "rfsCache";

  private static final String WSDL_LOCATION =
      "/mtosi/2.1/DDPs/ManageServiceInventory/IIS/wsdl/ServiceInventoryRetrieval/ServiceInventoryRetrievalHttp.wsdl";

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final ServiceInventoryRetrievalHttp service;

  private final String endPoint;

  private final LoadingCache<String, SapList> sapCache = CacheBuilder.newBuilder()
      .expireAfterAccess(15, TimeUnit.MINUTES)
      .build(
          new CacheLoader<String, SapList>() {
            @Override
            public SapList load(String key) throws Exception {
              if (key.equals(SAP_CACHE)) {
                return getServiceInventoryWithSapFilter().getInventoryData().getSapList();
              }
              throw new AssertionError("Unsupported cache key " + key);
            }
          }
      );

  private final LoadingCache<String, RfsList> rfsCache = CacheBuilder.newBuilder()
      .expireAfterAccess(15, TimeUnit.MINUTES)
      .build(
          new CacheLoader<String, RfsList>() {
            @Override
            public RfsList load(String key) throws Exception {
              if (key.equals(RFS_CACHE)) {
                return getServiceInventoryWithRfsFilter().getInventoryData().getRfsList();
              }
              throw new AssertionError("Unsupported cache key " + key);
            }
          }
       );

  @Autowired
  public InventoryRetrievalClient(@Value("${nbi.mtosi.inventory.retrieval.endpoint}") String endPoint) {
    this.endPoint = endPoint;
    this.service = new ServiceInventoryRetrievalHttp(this.getClass().getResource(WSDL_LOCATION),
        new QName("http://www.tmforum.org/mtop/msi/wsdl/sir/v1-0", "ServiceInventoryRetrievalHttp"));
  }

  public List<PhysicalPort> getPhysicalPorts() {

    return FluentIterable
      .from(getCachedSapInventory().getSap())
      .filter(new Predicate<ServiceAccessPointType>() {
        @Override
        public boolean apply(ServiceAccessPointType sap) {
          //(AdminStateType.UNLOCKED != sap.getAdminState() && (OperationalStateType.ENABLED != sap.getOperationalState()))) {
          return true;
        }
      })
      .transform(new Function<ServiceAccessPointType, PhysicalPort>() {
        @Override
        public PhysicalPort apply(ServiceAccessPointType sap) {
          return translateToPhysicalPort(sap);
        }
      })
      .toList();
  }

  public int getPhysicalPortCount() {
    return getCachedSapInventory().getSap().size();
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
  PhysicalPort translateToPhysicalPort(ServiceAccessPointType sap) {
    String nmsSapName = getSapName(sap);
    String managedElement = findRdnValue("ME", sap.getResourceRef()).get();
    String ptp = findRdnValue("PTP", sap.getResourceRef()).get();
    String nmsPortSpeed = findSscValue("AdministrativeSpeedRate", sap.getDescribedByList()).get();
    String supportedServiceType = findSscValue("SupportedServices", sap.getDescribedByList()).get();
    boolean isVlanRequired = determineVlanRequired(supportedServiceType);

    PhysicalPort physicalPort = new PhysicalPort(isVlanRequired);
    physicalPort.setNmsPortId(MtosiUtils.composeNmsPortId(managedElement, MtosiUtils.convertToShortPtP(ptp)));
    physicalPort.setNmsSapName(nmsSapName);
    physicalPort.setNmsNeId(managedElement);
    physicalPort.setNmsPortSpeed(nmsPortSpeed);
    physicalPort.setBodPortId(nmsSapName);
    physicalPort.setNocLabel(managedElement + "@" + ptp);
    physicalPort.setSupportedServiceType(supportedServiceType);
    physicalPort.setSignalingType("NA");

    logger.debug("Retrieved physicalport: {}", physicalPort);

    return physicalPort;
  }

  @VisibleForTesting
  boolean determineVlanRequired(String supportedServiceType) {
    return "EVPL".equals(supportedServiceType) || "EVPLAN".equals(supportedServiceType);
  }

  private ServiceInventoryRetrievalRPC getServiceProxy() {
    ServiceInventoryRetrievalRPC proxy = service.getServiceInventoryRetrievalSoapHttp();
    ((BindingProvider) proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPoint);

    return proxy;
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

  private SimpleServiceFilterType getInventoryRequestSimpleFilter(String filter) {
    SimpleServiceFilterType simpleFilter = new ObjectFactory().createSimpleServiceFilterType();
    simpleFilter.getScopeAndSelection().add(GranularityType.FULL);

    SimpleServiceFilterType.Scope scope = new ObjectFactory().createSimpleServiceFilterTypeScope();
    scope.setServiceObjectType(filter);
    simpleFilter.getScopeAndSelection().add(scope);

    return simpleFilter;
  }

  private SapList getCachedSapInventory() {
    try {
      return sapCache.get(SAP_CACHE);
    }
    catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  public RfsList getCachedRfsInventory() {
     try {
      return rfsCache.get(RFS_CACHE);
    }
    catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private GetServiceInventoryResponse retrieveServiceInventory(GetServiceInventoryRequest inventoryRequest)
      throws GetServiceInventoryException {
    ServiceInventoryRetrievalRPC proxy = getServiceProxy();

    GetServiceInventoryResponse serviceInventory =
        proxy.getServiceInventory(HeaderBuilder.buildInventoryHeader(endPoint), inventoryRequest);

    return serviceInventory;
  }

}