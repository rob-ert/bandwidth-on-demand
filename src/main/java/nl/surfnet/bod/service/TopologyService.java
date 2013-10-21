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
package nl.surfnet.bod.service;

import ietf.params.xml.ns.vcard4_0.FormattedNameType;
import ietf.params.xml.ns.vcard4_0.NameType;
import ietf.params.xml.ns.vcard4_0.OrganizationType;

import java.io.StringWriter;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.XMLGregorianCalendar;

import nl.surfnet.bod.domain.EnniPort;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.nsi.NsiHelper;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.util.XmlUtils;

import org.joda.time.DateTime;
import org.ogf.schemas.nml._2013._05.base.BidirectionalPortType;
import org.ogf.schemas.nml._2013._05.base.LabelGroupType;
import org.ogf.schemas.nml._2013._05.base.LocationType;
import org.ogf.schemas.nml._2013._05.base.ObjectFactory;
import org.ogf.schemas.nml._2013._05.base.PortGroupRelationType;
import org.ogf.schemas.nml._2013._05.base.PortGroupType;
import org.ogf.schemas.nml._2013._05.base.TopologyRelationType;
import org.ogf.schemas.nml._2013._05.base.TopologyType;
import org.ogf.schemas.nsi._2013._09.topology.NSARelationType;
import org.ogf.schemas.nsi._2013._09.topology.NSAType;
import org.ogf.schemas.nsi._2013._09.topology.NsiServiceRelationType;
import org.ogf.schemas.nsi._2013._09.topology.NsiServiceType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TopologyService {

  private static final String PROTOCOL_VERSION = "application/vnd.org.ogf.nsi.cs.v2+soap";
  private static final String HAS_OUTBOUND_PORT_TYPE = "http://schemas.ogf.org/nml/2013/05/base#hasOutboundPort";
  private static final String HAS_INBOUND_PORT_TYPE = "http://schemas.ogf.org/nml/2013/05/base#hasInboundPort";
  private static final String PROVIDED_BY_TYPE = "http://schemas.ogf.org/nsi/2013/09/topology#providedBy";
  private static final String IS_ALIAS_TYPE = "http://schemas.ogf.org/nml/2013/05/base#isAlias";
  private static final String ADMIN_CONTACT_TYPE = "http://schemas.ogf.org/nsi/2013/09/topology#adminContact";

  @Resource private Environment bodEnvironment;
  @Resource private NsiHelper nsiHelper;
  @Resource private VirtualPortService virtualPortService;
  @Resource private PhysicalPortService physicalPortService;

  @Value("${nsi.topology.lat}") private Float latitude;
  @Value("${nsi.topology.lng}") private Float longitude;
  @Value("${nsi.topology.admin.contact}") private String adminContact;
  @Value("${nsi.topology.admin.organization}") private String adminOrganization;

  @Value("${nsi.network.name}") private String networkName;
  @Value("${nsi.provider.name}") private String providerName;

  public String nsiToplogyAsString() {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance("org.ogf.schemas.nsi._2013._09.topology:ietf.params.xml.ns.vcard4_0");

      StringWriter writer = new StringWriter();

      jaxbContext.createMarshaller().marshal(new org.ogf.schemas.nsi._2013._09.topology.ObjectFactory().createNSA(nsiTopology()), writer);

      return writer.toString();
    } catch (JAXBException e) {
      throw new AssertionError("Could not serialize nsi toplogy", e);
    }
  }

  public NSAType nsiTopology() {
    return new NSAType()
      .withId(nsiHelper.getProviderNsaV2())
      .withName(providerName)
      .withVersion(version())
      .withLocation(location())
      .withService(service())
      .withRelation(adminContact())
      .withTopology(topology());
  }

  private NsiServiceType service() {
    return new NsiServiceType()
      .withId(nsiHelper.getProviderNsaV2() + ":connection-service")
      .withLink(getNsi2ConnectionProviderUrl())
      .withType(PROTOCOL_VERSION)
      .withRelation(new NsiServiceRelationType()
        .withType(PROVIDED_BY_TYPE)
        .withNSA(new NSAType().withId(nsiHelper.getProviderNsaV2())));
  }

  private TopologyType topology() {
    TopologyType topology = new TopologyType()
      .withId(nsiHelper.getUrnTopology())
      .withName(networkName);

    List<VirtualPort> virtualPorts = virtualPortService.findAll();
    List<EnniPort> enniPorts = physicalPortService.findAllAllocatedEnniEntries();

    for (VirtualPort virtualPort : virtualPorts) {
      topology.withAny(new ObjectFactory().createBidirectionalPort(bidirectionalPort(virtualPort)));
    }
    for (EnniPort enniPort : enniPorts) {
      topology.withAny(new ObjectFactory().createBidirectionalPort(bidirectionalPort(enniPort)));
    }

    for (VirtualPort virtualPort : virtualPorts) {
      topology.withRelation(outboundPort(virtualPort));
      topology.withRelation(inboundPort(virtualPort));
    }
    for (EnniPort enniPort : enniPorts) {
      topology.withRelation(outboundPort(enniPort));
      topology.withRelation(inboundPort(enniPort));
    }

    return topology;
  }

  private TopologyRelationType inboundPort(VirtualPort virtualPort) {
    PortGroupType group = new PortGroupType().withId(nsiHelper.getStpIdV2(virtualPort) + ":in");

    if (virtualPort.getVlanId() != null) {
      group.withLabelGroup(vlanLabelGroup("" + virtualPort.getVlanId()));
    }

    return inboundPort(group);
  }

  private TopologyRelationType inboundPort(EnniPort enniPort) {
    PortGroupType group = new PortGroupType()
      .withId(nsiHelper.getStpIdV2(enniPort) + ":in")
      .withLabelGroup(vlanLabelGroup(enniPort.getVlanRanges()))
      .withRelation(isAlias(enniPort.getInboundPeer()));

    return inboundPort(group);
  }

  private TopologyRelationType outboundPort(VirtualPort virtualPort) {
    PortGroupType group = new PortGroupType().withId(nsiHelper.getStpIdV2(virtualPort) + ":out");

    if (virtualPort.getVlanId() != null) {
      group.withLabelGroup(vlanLabelGroup("" + virtualPort.getVlanId()));
    }

    return outboundPort(group);
  }

  private LabelGroupType vlanLabelGroup(String vlanRange) {
    return new LabelGroupType()
      .withLabeltype("http://schemas.ogf.org/nml/2012/10/ethernet#vlan")
      .withValue(vlanRange);
  }

  private TopologyRelationType outboundPort(EnniPort enniPort) {
    PortGroupType group = new PortGroupType()
      .withId(nsiHelper.getStpIdV2(enniPort) + ":out")
      .withLabelGroup(vlanLabelGroup(enniPort.getVlanRanges()))
      .withRelation(isAlias(enniPort.getOutboundPeer()));

    return outboundPort(group);
  }

  private PortGroupRelationType isAlias(String peer) {
    return new PortGroupRelationType()
      .withType(IS_ALIAS_TYPE)
      .withPortGroup(new PortGroupType().withId(peer));
  }

  private TopologyRelationType inboundPort(PortGroupType portGroup) {
    return new TopologyRelationType()
      .withType(HAS_INBOUND_PORT_TYPE)
      .withPortGroup(portGroup);
  }

  private TopologyRelationType outboundPort(PortGroupType portGroup) {
    return new TopologyRelationType()
      .withType(HAS_OUTBOUND_PORT_TYPE)
      .withPortGroup(portGroup);
  }

  private BidirectionalPortType bidirectionalPort(VirtualPort virtualPort) {
    return bidirectionalPort(nsiHelper.getStpIdV2(virtualPort));
  }

  private BidirectionalPortType bidirectionalPort(EnniPort enniPort) {
    return bidirectionalPort(nsiHelper.getStpIdV2(enniPort));
  }

  private BidirectionalPortType bidirectionalPort(String stpId) {
    @SuppressWarnings("unchecked")
    BidirectionalPortType port = new BidirectionalPortType()
      .withId(stpId)
      .withRest(
          new ObjectFactory().createPortGroup(new PortGroupType().withId(stpId + ":out")),
          new ObjectFactory().createPortGroup(new PortGroupType().withId(stpId + ":in")));

    return port;
  }

  private LocationType location() {
    return new LocationType()
      .withId(nsiHelper.getProviderNsaV2() + ":location")
      .withLat(latitude)
      .withLong(longitude);
  }

  private NSARelationType adminContact() {
    String[] nameTokens = adminContact.split(" ");

    return new NSARelationType().withType(ADMIN_CONTACT_TYPE).withAny(new Object[] {
      new FormattedNameType().withText(adminContact),
      new NameType().withGiven(nameTokens[0]).withSurname(nameTokens[1]),
      new OrganizationType().withText(adminOrganization) });
  }

  private String getNsi2ConnectionProviderUrl() {
    return bodEnvironment.getExternalBodUrl() + bodEnvironment.getNsiV2ServiceUrl();
  }

  private XMLGregorianCalendar version() {
    return XmlUtils.toGregorianCalendar(DateTime.now());
  }

}