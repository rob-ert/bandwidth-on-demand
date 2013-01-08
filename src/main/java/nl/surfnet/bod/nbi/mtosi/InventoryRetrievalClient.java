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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import nl.surfnet.bod.domain.PhysicalPort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tmforum.mtop.fmw.xsd.nam.v1.RelativeDistinguishNameType;
import org.tmforum.mtop.msi.wsdl.sir.v1_0.GetServiceInventoryException;
import org.tmforum.mtop.msi.wsdl.sir.v1_0.ServiceInventoryRetrievalHttp;
import org.tmforum.mtop.msi.wsdl.sir.v1_0.ServiceInventoryRetrievalRPC;
import org.tmforum.mtop.msi.xsd.sir.v1.*;
import org.tmforum.mtop.msi.xsd.sir.v1.ServiceInventoryDataType.SapList;
import org.tmforum.mtop.sb.xsd.svc.v1.ServiceAccessPointType;
import org.tmforum.mtop.sb.xsd.svc.v1.ServiceCharacteristicValueType;

@Service
public class InventoryRetrievalClient {

  private static final String WSDL_LOCATION =
    "/mtosi/2.1/DDPs/ManageServiceInventory/IIS/wsdl/ServiceInventoryRetrieval/ServiceInventoryRetrievalHttp.wsdl";

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final ServiceInventoryRetrievalHttp service;

  private final String endPoint;

  @Autowired
  public InventoryRetrievalClient(@Value("${nbi.mtosi.inventory.retrieval.endpoint}") String endPoint) {
    this.endPoint = endPoint;
    this.service = new ServiceInventoryRetrievalHttp(this.getClass().getResource(WSDL_LOCATION),
        new QName("http://www.tmforum.org/mtop/msi/wsdl/sir/v1-0", "ServiceInventoryRetrievalHttp"));
  }

  private SapList getSapInventory() {
    try {
      GetServiceInventoryRequest inventoryRequest = new ObjectFactory().createGetServiceInventoryRequest();
      addSapFilter(inventoryRequest);

      ServiceInventoryRetrievalRPC proxy = getServiceProxy();

      GetServiceInventoryResponse serviceInventory =
        proxy.getServiceInventory(HeaderBuilder.buildInventoryHeader(endPoint), inventoryRequest);

      return serviceInventory.getInventoryData().getSapList();
    }
    catch (GetServiceInventoryException e) {
      log.error("Error: ", e);
      return null;
    }
  }

  private ServiceInventoryRetrievalRPC getServiceProxy() {
    ServiceInventoryRetrievalRPC proxy = service.getServiceInventoryRetrievalSoapHttp();
    ((BindingProvider) proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPoint);
    return proxy;
  }

  private void addSapFilter(GetServiceInventoryRequest request) {
    request.setFilter(getInventoryRequestSimpleFilter("SAP"));
  }

  private SimpleServiceFilterType getInventoryRequestSimpleFilter(String filter) {
    final SimpleServiceFilterType simpleFilter = new ObjectFactory().createSimpleServiceFilterType();
    simpleFilter.getScopeAndSelection().add(GranularityType.FULL);
    final SimpleServiceFilterType.Scope scope = new ObjectFactory().createSimpleServiceFilterTypeScope();
    scope.setServiceObjectType(filter);
    simpleFilter.getScopeAndSelection().add(scope);

    return simpleFilter;
  }

  public List<PhysicalPort> getUnallocatedPorts() {
    // [x] nmsPortId = sapList.sap.resourceRef.rdn.value where
    // sapList.sap.resourceRef.rdn.type == PTP

    // [x] nmsNEId = sapList.sap.resourceRef.rdn.value where
    // sapList.sap.resourceRef.rdn.type == ME

    // [x] nsmPortSpeed = sapList.sap.describedByList.value where
    // sapList.sap.describedByList.sscRef.rdn.value == AdministrativeSpeedRate

    // [x] bodPortId = spaList.sap.name

    // [x] vlanRequired = true if sapList.sap.describedByList.value in (EVPL,
    // EVPLAN) where sapList.sap.describedByList.sscRef.rdn.value ==
    // SupportedServiceType

    // [] nocLabel = nmsNeId + nmsPortId

    // [] nmsNeId = will be human friendly name for NE + snmp ifalias

    final List<PhysicalPort> mtosiPorts = new ArrayList<>();

    for (final ServiceAccessPointType sap : getSapInventory().getSap()) {
      final String nmsSapName = sap.getName().getValue().getRdn().get(0).getValue();

      String managedElement = null, ptp = null, nmsPortSpeed = null, supportedServiceType = null;
      boolean isVlanRequired = false;

      for (final RelativeDistinguishNameType relativeDistinguishNameType : sap.getResourceRef().getRdn()) {
        if (relativeDistinguishNameType.getType().equals("ME")) {
          managedElement = relativeDistinguishNameType.getValue();
        }
        else if (relativeDistinguishNameType.getType().equals("PTP")) {
          ptp = relativeDistinguishNameType.getValue();
        }
      }

      for (final ServiceCharacteristicValueType serviceCharacteristicValueType : sap.getDescribedByList()) {
        final String value = serviceCharacteristicValueType.getValue();
        for (final RelativeDistinguishNameType rdn : serviceCharacteristicValueType.getSscRef().getRdn()) {
          final String rdnValue = rdn.getValue();
          if ("AdministrativeSpeedRate".equals(rdnValue)) {
            nmsPortSpeed = value;
          }
          else if ("SupportedServiceType".equals(rdnValue)) {
            supportedServiceType = value;
            if ("EVPL".equals(value) || "EVPLAN".equals(value)) {
              isVlanRequired = true;
            }
          }
        }
      }

      final PhysicalPort physicalPort = new PhysicalPort(isVlanRequired);
      final String nmsPortId = MtosiUtils.physicalTerminationPointToNmsPortId(ptp);
      physicalPort.setNmsPortId(nmsPortId);
      physicalPort.setNmsNeId(managedElement);
      physicalPort.setBodPortId(nmsSapName);
      physicalPort.setNmsPortSpeed(nmsPortSpeed);
      physicalPort.setNmsSapName(nmsSapName);
      physicalPort.setNocLabel(managedElement + " " + nmsPortId);
      physicalPort.setSupportedServiceType(supportedServiceType);
      physicalPort.setSignalingType("NA");

      mtosiPorts.add(physicalPort);
    }
    return mtosiPorts;
  }

  public int getUnallocatedMtosiPortCount() {
    return getSapInventory().getSap().size();
  }

}