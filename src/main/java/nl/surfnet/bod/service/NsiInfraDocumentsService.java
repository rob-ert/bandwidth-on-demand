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

import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.ogf.schemas.nml._2013._05.base.BidirectionalPortType;
import org.ogf.schemas.nml._2013._05.base.LabelGroupType;
import org.ogf.schemas.nml._2013._05.base.ObjectFactory;
import org.ogf.schemas.nml._2013._05.base.PortGroupRelationType;
import org.ogf.schemas.nml._2013._05.base.PortGroupType;
import org.ogf.schemas.nml._2013._05.base.TopologyRelationType;
import org.ogf.schemas.nml._2013._05.base.TopologyType;
import org.ogf.schemas.nsi._2014._02.discovery.nsa.FeatureType;
import org.ogf.schemas.nsi._2014._02.discovery.nsa.InterfaceType;
import org.ogf.schemas.nsi._2014._02.discovery.nsa.LocationType;
import org.ogf.schemas.nsi._2014._02.discovery.nsa.NsaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ietf.params.xml.ns.vcard_4.FnPropType;
import ietf.params.xml.ns.vcard_4.KindPropType;
import ietf.params.xml.ns.vcard_4.NPropType;
import ietf.params.xml.ns.vcard_4.ProdidPropType;
import ietf.params.xml.ns.vcard_4.RevPropType;
import ietf.params.xml.ns.vcard_4.UidPropType;
import ietf.params.xml.ns.vcard_4.VcardsType;
import nl.surfnet.bod.domain.EnniPort;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.nsi.NsiHelper;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.util.JaxbUserType;
import nl.surfnet.bod.util.NsaUserType;
import nl.surfnet.bod.util.Orderings;
import nl.surfnet.bod.util.TopologyUserType;
import nl.surfnet.bod.util.XmlUtils;

@Service
public class NsiInfraDocumentsService {

  private static final String PROTOCOL_VERSION = "application/vnd.org.ogf.nsi.cs.v2+soap";
  private static final String HAS_OUTBOUND_PORT_TYPE = "http://schemas.ogf.org/nml/2013/05/base#hasOutboundPort";
  private static final String HAS_INBOUND_PORT_TYPE = "http://schemas.ogf.org/nml/2013/05/base#hasInboundPort";
  private static final String IS_ALIAS_TYPE = "http://schemas.ogf.org/nml/2013/05/base#isAlias";

  @Resource private Environment bodEnvironment;
  @Resource private NsiHelper nsiHelper;
  @Resource private VirtualPortService virtualPortService;
  @Resource private PhysicalPortService physicalPortService;

  @Value("${nsi.discovery.lat}") private Float latitude;
  @Value("${nsi.discovery.lng}") private Float longitude;
  @Value("${nsi.discovery.admin.contact}") private String adminContact;
  @Value("${nsi.network.name}") private String networkName;
  @Value("${nsi.provider.name}") private String providerName;

  private volatile TopologyType cachedTopology;
  private volatile DateTime topologyCacheTime;

  private volatile NsaType cachedNsaType;
  private volatile DateTime nsaCacheTime;

  private static DateTime startUpTime = DateTime.now();

  @PostConstruct
  protected void init() {

    cachedTopology = topology();
    topologyCacheTime = DateTime.now();

    cachedNsaType = nsa();
    nsaCacheTime = DateTime.now();
  }

  public static final JaxbUserType<TopologyType> TOPOLOGY_CONVERTER = new TopologyUserType();
  public static final JaxbUserType<NsaType> DISCOVERY_CONVERTER = new NsaUserType();


  public NsaType nsiDiscovery(){
    NsaType newerNsa = nsa();
    synchronized (cachedNsaType) {
      if (!newerNsa.equals(cachedNsaType)) {
        cachedNsaType = newerNsa;
        nsaCacheTime = DateTime.now();
      }
    }

    return cachedNsaType.withVersion(XmlUtils.toGregorianCalendar(nsaCacheTime));
  }

  private NsaType nsa() {
    return new NsaType().
      withId(nsiHelper.getProviderNsaV2()).
      withNetworkId(nsiHelper.getUrnTopology()).
      withName(providerName).
      withSoftwareVersion(bodEnvironment.getVersion()).
      withStartTime(XmlUtils.toGregorianCalendar(startUpTime)).
      withLocation(location()).
      withAdminContact(adminContact()).
      withInterface(
        new InterfaceType().withType("application/vnd.ogf.nsi.topology.v2+xml").withHref(bodEnvironment.getExternalBodUrl() + "/nsi-topology"),
        new InterfaceType().withType(PROTOCOL_VERSION).withHref(getNsi2ConnectionProviderUrl())
      ).
      withFeature(new FeatureType().withType("vnd.ogf.nsi.cs.v2.role.uPA"));
  }

  public TopologyType nsiTopology() {
    TopologyType newerTopology = topology();
    synchronized (cachedTopology) {
      if (!newerTopology.equals(cachedTopology)) {
        cachedTopology = newerTopology;
        topologyCacheTime = DateTime.now();
      }
    }

    return cachedTopology.withVersion(XmlUtils.toGregorianCalendar(topologyCacheTime));
  }



  protected TopologyType topology() {
    TopologyType topology = new TopologyType()
      .withId(nsiHelper.getUrnTopology())
      .withName(networkName);

    List<VirtualPort> virtualPorts = Orderings.LOGGABLE_ORDERING.immutableSortedCopy(virtualPortService.findAll());
    List<EnniPort> enniPorts = Orderings.LOGGABLE_ORDERING.immutableSortedCopy(physicalPortService.findAllAllocatedEnniEntries());

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
      .withRelation(isAlias(enniPort.getInboundPeer()));
    if (enniPort.isVlanRequired()) {
      group.withLabelGroup(vlanLabelGroup(enniPort.getVlanRanges()));
    }
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
    return bidirectionalPort(nsiHelper.getStpIdV2(virtualPort), virtualPort.getUserLabel());
  }

  private BidirectionalPortType bidirectionalPort(EnniPort enniPort) {
    return bidirectionalPort(nsiHelper.getStpIdV2(enniPort), enniPort.getNocLabel());
  }

  private BidirectionalPortType bidirectionalPort(String stpId, String name) {
    @SuppressWarnings("unchecked")
    BidirectionalPortType port = new BidirectionalPortType()
      .withId(stpId)
      .withName(name)
      .withRest(
          new ObjectFactory().createPortGroup(new PortGroupType().withId(stpId + ":out")),
          new ObjectFactory().createPortGroup(new PortGroupType().withId(stpId + ":in")));

    return port;
  }

  private LocationType location() {
    return new LocationType()
      .withLatitude(latitude)
      .withLongitude(longitude);
  }

  private VcardsType adminContact() {
    String[] nameTokens = adminContact.split(" ");
    final VcardsType vcardsType = new VcardsType().withVcard(new VcardsType.Vcard().
                    withRev(new RevPropType().withTimestamp(DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss'Z'").print(DateTime.now()))).
                    withProdid(new ProdidPropType().withText("surfnet-" + bodEnvironment.getEnvironment())).
                    withKind(new KindPropType().withText("individual")).
                    withFn(new FnPropType().withText(adminContact)).
                    withN(new NPropType().withGiven(nameTokens[0]).withSurname(nameTokens[1]))
    );
    vcardsType.getVcard().withUid(new UidPropType().withUri(getNsi2ConnectionProviderUrl() + "#adminContact"));
    return vcardsType;
  }

  private String getNsi2ConnectionProviderUrl() {
    return bodEnvironment.getExternalBodUrl() + bodEnvironment.getNsiV2ServiceUrl();
  }

  protected void setAdminContact(String adminContact) {
    this.adminContact = adminContact;
  }

}
