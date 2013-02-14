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
package nl.surfnet.bod.support;

import java.util.concurrent.atomic.AtomicLong;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;

import org.springframework.util.StringUtils;

public class VirtualPortFactory {

  private static final AtomicLong COUNTER = new AtomicLong();

  private Long id = COUNTER.getAndIncrement();
  private Integer version;
  private String managerLabel = "A virtual port " + id;
  private String userLabel = "A user virtual port " + id;;
  private PhysicalPort physicalPort;
  private Integer maxBandwidth = 10000;
  private Integer vlanId = null;
  private VirtualResourceGroup virtualResourceGroup;

  private String physicalPortAdminGroup = null;
  private boolean noIds = false;

  public VirtualPort create() {

    if (physicalPort == null) {
      physicalPort = createPhysicalPort();
    }

    if (virtualResourceGroup == null) {
      virtualResourceGroup = createVirtualResourceGroup();
    }

    VirtualPort virtualPort = new VirtualPort();

    virtualPort.setId(id);
    virtualPort.setVersion(version);
    virtualPort.setManagerLabel(managerLabel);
    virtualPort.setUserLabel(userLabel);
    virtualPort.setMaxBandwidth(maxBandwidth);
    virtualPort.setVlanId(vlanId);
    virtualPort.setVirtualResourceGroup(virtualResourceGroup);
    virtualPort.setPhysicalPort(physicalPort);

    if (physicalPort != null && StringUtils.hasText(physicalPortAdminGroup)) {
      physicalPort.getPhysicalResourceGroup().setAdminGroup(physicalPortAdminGroup);
    }

    return virtualPort;
  }

  private VirtualResourceGroup createVirtualResourceGroup() {
    VirtualResourceGroupFactory factory = new VirtualResourceGroupFactory();
    if (noIds) {
      factory.withNoIds();
    }
    return factory.create();
  }

  private PhysicalPort createPhysicalPort() {
    PhysicalPortFactory factory = new PhysicalPortFactory();

    if (noIds) {
      factory.withNoIds();
    }

    return factory.create();
  }

  public VirtualPortFactory setManagerLabel(String label) {
    this.managerLabel = label;
    return this;
  }

  public VirtualPortFactory setUserLabel(String label) {
    this.userLabel = label;
    return this;
  }

  public VirtualPortFactory setId(Long id) {
    this.id = id;
    return this;
  }

  public VirtualPortFactory setMaxBandwidth(Integer maxBandwidth) {
    this.maxBandwidth = maxBandwidth;
    return this;
  }

  public VirtualPortFactory setVirtualResourceGroup(VirtualResourceGroup virtualResourceGroup) {
    this.virtualResourceGroup = virtualResourceGroup;
    return this;
  }

  public VirtualPortFactory setPhysicalPort(PhysicalPort physicalPort) {
    this.physicalPort = physicalPort;
    return this;
  }

  public VirtualPortFactory setVlanId(Integer vid) {
    this.vlanId = vid;
    return this;
  }

  public VirtualPortFactory setPhysicalPortAdminGroup(String group) {
    this.physicalPortAdminGroup = group;
    return this;
  }

  public VirtualPortFactory withNodIds() {
    this.id = null;
    this.version = null;
    this.noIds = true;

    return this;
  }

}