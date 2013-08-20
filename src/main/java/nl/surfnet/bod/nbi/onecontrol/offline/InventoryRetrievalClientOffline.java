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
package nl.surfnet.bod.nbi.onecontrol.offline;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import nl.surfnet.bod.domain.NbiPort;
import nl.surfnet.bod.domain.NbiPort.InterfaceType;
import nl.surfnet.bod.nbi.NbiClient;
import nl.surfnet.bod.nbi.onecontrol.InventoryRetrievalClient;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.tmforum.mtop.msi.xsd.sir.v1.ServiceInventoryDataType;

@Component
@Profile("onecontrol-offline")
public class InventoryRetrievalClientOffline implements InventoryRetrievalClient {

  private static final Function<MockNbiPort, NbiPort> TRANSFORM_FUNCTION = new Function<MockNbiPort, NbiPort>() {
    @Override
    public NbiPort apply(MockNbiPort nbiPort) {
      NbiPort physicalPort = new NbiPort();
      physicalPort.setNmsPortId(nbiPort.getId());
      physicalPort.setSuggestedBodPortId("Mock_" + nbiPort.getName());
      physicalPort.setSuggestedNocLabel("Mock_" + nbiPort.getUserLabel().or(nbiPort.getName()));
      physicalPort.setVlanRequired(isVlanRequired(nbiPort.getName()));
      physicalPort.setInterfaceType(InterfaceType.UNI);

      return physicalPort;
    }

    /**
     * @return true when a VlanId is required for this port. This is only the
     *         case when the name of the port contains NOT
     *         {@link nl.surfnet.bod.nbi.NbiClient#VLAN_REQUIRED_SELECTOR}
     */
    private boolean isVlanRequired(String name) {
      return name == null ? false : !name.toLowerCase().contains(NbiClient.VLAN_REQUIRED_SELECTOR);
    }
  };

  private List<MockNbiPort> ports = new ArrayList<>();

  public InventoryRetrievalClientOffline() {
    ports.add(new MockNbiPort("Ut002A_OME01_ETH-1-1-4", "00-1B-25-2D-DA-65_ETH-1-1-4"));
    ports.add(new MockNbiPort("Ut002A_OME01_ETH-1-2-4", "00-1B-25-2D-DA-65_ETH-1-2-4"));
    ports.add(new MockNbiPort("ETH10G-1-13-1", "00-21-E1-D6-D6-70_ETH10G-1-13-1", "Poort 1de verdieping toren1a"));
    ports.add(new MockNbiPort("ETH10G-1-13-2", "00-21-E1-D6-D6-70_ETH10G-1-13-2", "Poort 2de verdieping toren1b"));
    ports.add(new MockNbiPort("ETH-1-13-4", "00-21-E1-D6-D5-DC_ETH-1-13-4", "Poort 3de verdieping toren1c"));
    ports.add(new MockNbiPort("ETH10G-1-13-2", "00-21-E1-D6-D5-DC_ETH10G-1-13-5"));
    ports.add(new MockNbiPort("ETH10G-1-5-1", "00-20-D8-DF-33-8B_ETH10G-1-5-1"));
    ports.add(new MockNbiPort("OME0039_OC12-1-12-1", "00-21-E1-D6-D6-70_OC12-1-12-1", "Poort 4de verdieping toren1a"));
    ports.add(new MockNbiPort("WAN-1-4-102", "00-20-D8-DF-33-86_WAN-1-4-102", "Poort 5de verdieping toren1a"));
    ports.add(new MockNbiPort("ETH-1-3-1", "00-21-E1-D6-D6-70_ETH-1-3-1"));
    ports.add(new MockNbiPort("ETH-1-1-1", "00-21-E1-D6-D5-DC_ETH-1-1-1", "Poort 1de verdieping toren2"));
    ports.add(new MockNbiPort("ETH-1-2-3", "00-20-D8-DF-33-8B_ETH-1-2-3", "Poort 2de verdieping toren2"));
    ports.add(new MockNbiPort("WAN-1-4-101", "00-20-D8-DF-33-86_WAN-1-4-101"));
    ports.add(new MockNbiPort("ETH-1-1-2", "00-21-E1-D6-D5-DC_ETH-1-1-2"));
    ports.add(new MockNbiPort("OME0039_OC12-1-12-2", "00-21-E1-D6-D6-70_OC12-1-12-2", "Poort 3de verdieping toren2"));
    ports.add(new MockNbiPort("ETH-1-13-5", "00-21-E1-D6-D5-DC_ETH-1-13-5", "Poort 4de verdieping toren3"));
    ports.add(new MockNbiPort("ETH10G-1-13-3", "00-21-E1-D6-D5-DC_ETH10G-1-13-3", "Poort 4de verdieping toren3"));
    ports.add(new MockNbiPort("Asd001A_OME3T_ETH-1-1-1", "00-20-D8-DF-33-59_ETH-1-1-1"));
  }

  @Override
  public List<NbiPort> getPhysicalPorts() {
    return Lists.newArrayList(Lists.transform(ports, TRANSFORM_FUNCTION));
  }

  @Override
  public int getPhysicalPortCount() {
    return ports.size();
  }

  @Override
  public Optional<ServiceInventoryDataType.RfsList> getRfsInventory() {
    return Optional.absent();
  }

  private static final class MockNbiPort {
    private final String name;
    private final Optional<String> userLabel;
    private final String id;

    public MockNbiPort(String name, String id) {
      this(name, id, null);
    }

    public MockNbiPort(String name, String id, String userLabel) {
      this.name = name;
      this.id = id;
      this.userLabel = Optional.fromNullable(userLabel);
    }

    public String getName() {
      return name;
    }

    public String getId() {
      return id;
    }

    public Optional<String> getUserLabel() {
      return userLabel;
    }
  }
}
