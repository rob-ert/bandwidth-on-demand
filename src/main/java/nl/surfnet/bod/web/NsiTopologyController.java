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
package nl.surfnet.bod.web;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.EnniPort;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.nsi.NsiHelper;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.util.Environment;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/nsi-topology")
public class NsiTopologyController {

  @Resource private Environment bodEnvironment;
  @Resource private VirtualPortService virtualPortService;
  @Resource private PhysicalPortService physicalPortService;
  @Resource private NsiHelper nsiHelper;

  @RequestMapping(method = RequestMethod.GET)
  public String renderTopology(Model model) throws Exception {

    model.addAttribute("providerNsa", nsiHelper.getProviderNsaV2());
    model.addAttribute("networkName", nsiHelper.getNetworkIdV2());
    model.addAttribute("version", DateTime.now().toString());
    model.addAttribute("topologyId", nsiHelper.getUrnStpV2());
    model.addAttribute("nsi2ConnectionProviderUrl", getNsi2ConnectionProviderUrl());

    URL url = this.getClass().getResource(bodEnvironment.getNsiTopologyAdminContactContentFileUri());
    String adminContactContent = FileUtils.readFileToString(new File(url.toURI()));
    model.addAttribute("adminContactContent", adminContactContent);

    // query all virtual ports, these are my 'UNI ports'
    List<VirtualPort> virtualPorts = virtualPortService.findAll();
    List<TopologyEntryView> entries = new ArrayList<>();

    for (VirtualPort virtualPort : virtualPorts) {
      String portGroupId = nsiHelper.getStpIdV2(virtualPort);

      String vlanRange = null;
      if (virtualPort.getVlanId() != null) {
        vlanRange = virtualPort.getVlanId().toString();
      }
      entries.add(new TopologyEntryView(portGroupId, vlanRange, null, null));
    }

    for (EnniPort enniPort : physicalPortService.findAllAllocatedEnniEntries()) {
      String portGroupId = nsiHelper.getStpIdV2(enniPort);
      entries.add(new TopologyEntryView(portGroupId, enniPort.getVlanRanges(), enniPort.getOutboundPeer(), enniPort.getInboundPeer()));
    }

    model.addAttribute("entries", entries);

    return "topology";
  }

  /**
   * @return the URL of the soap-service that we run
   */
  private String getNsi2ConnectionProviderUrl() {
    return bodEnvironment.getExternalBodUrl() + bodEnvironment.getNsiV2ServiceUrl();
  }

  public static class TopologyEntryView {

    private final String portGroupId;
    private final String vlanRanges;
    private final String outboundPeer;
    private final String inboundPeer;

    public TopologyEntryView(String portgroupId, String vlanRanges, String outboundPeer, String inboundPeer) {
      this.portGroupId = portgroupId;
      this.vlanRanges = vlanRanges;
      this.outboundPeer = outboundPeer;
      this.inboundPeer = inboundPeer;
    }

    public String getPortGroupId() {
      return portGroupId;
    }

    public String getVlanRanges() {
      return vlanRanges;
    }

    public String getOutboundPeer() {
      return outboundPeer;
    }

    public String getInboundPeer() {
      return inboundPeer;
    }
  }

}
