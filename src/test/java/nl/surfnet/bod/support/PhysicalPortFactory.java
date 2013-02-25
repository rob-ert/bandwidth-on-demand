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

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;

public class PhysicalPortFactory {

  private static final AtomicLong COUNTER = new AtomicLong();

  private Long id = COUNTER.incrementAndGet();
  private String nocLabel = "nameDefault " + id;
  private String managerLabel = "managedLabel " + id;
  private PhysicalResourceGroup physicalResourceGroup;
  private Integer version = 0;
  private String nmsPortId = UUID.randomUUID().toString();
  private String bodPortId = "Asd001A_OME3T_ETH-1-1-4";
  private boolean vlanRequired = false;
  private boolean noIds;

  private String nmsSapName;

  private String nmsNeId;

  public PhysicalPort create() {
    PhysicalPort port = new PhysicalPort(vlanRequired);
    port.setId(id);
    port.setVersion(version);
    port.setBodPortId(bodPortId);

    port.setNocLabel(nocLabel);
    port.setManagerLabel(managerLabel);
    port.setNmsPortId(nmsPortId);
    port.setNmsSapName(nmsSapName);
    port.setNmsNeId(nmsNeId);

    if (physicalResourceGroup == null) {
      physicalResourceGroup = createPhysicalResourceGroup();
    }
    port.setPhysicalResourceGroup(physicalResourceGroup);

    return port;
  }

  private PhysicalResourceGroup createPhysicalResourceGroup() {
    PhysicalResourceGroupFactory factory = new PhysicalResourceGroupFactory();
    if (noIds) {
      factory.withNoIds();
    }
    return factory.create();
  }

  public PhysicalPortFactory setId(Long id) {
    this.id = id;
    return this;
  }

  public PhysicalPortFactory setVersion(Integer version) {
    this.version = version;
    return this;
  }

  public PhysicalPortFactory setManagerLabel(String managerLabel) {
    this.managerLabel = managerLabel;
    return this;
  }

  public PhysicalPortFactory setNocLabel(String nocLabel) {
    this.nocLabel = nocLabel;
    return this;
  }

  public PhysicalPortFactory setPhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup) {
    this.physicalResourceGroup = physicalResourceGroup;
    return this;
  }

  public PhysicalPortFactory setNmsPortId(String nmsPortId) {
    this.nmsPortId = nmsPortId;
    return this;
  }

  public PhysicalPortFactory setVlanRequired(boolean vlanRequired) {
    this.vlanRequired = vlanRequired;
    return this;
  }

  public void setBodPortId(String portId) {
    this.bodPortId = portId;
  }

  public PhysicalPortFactory setNmsSapName(String nmsSapName) {
    this.nmsSapName = nmsSapName;
    return this;
  }

  public PhysicalPortFactory withNoIds() {
    this.id = null;
    this.version = null;
    this.noIds = true;

    return this;
  }

  public PhysicalPortFactory setNmsNeId(String nsmNeId) {
    this.nmsNeId = nsmNeId;
    return this;
  }
}
