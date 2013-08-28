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
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import nl.surfnet.bod.domain.NbiPort;
import nl.surfnet.bod.domain.NmsAlignmentStatus;
import nl.surfnet.bod.domain.PhysicalPort;

class PortAlignmentChecker {
  private final List<PhysicalPort> realignedPorts = Lists.newArrayList();
  private final List<PhysicalPort> unalignedPorts = Lists.newArrayList();
  private final List<PhysicalPort> alignmentChangedPorts = Lists.newArrayList();

  public void updateAlignment(List<PhysicalPort> bodPorts, List<NbiPort> nbiPorts) {
    ImmutableMap<String, NbiPort> nbiPortsMap = buildNbiPortIdMap(nbiPorts);
    for (PhysicalPort bodPort : bodPorts) {
      Optional<NbiPort> nbiPort = Optional.fromNullable(nbiPortsMap.get(bodPort.getNmsPortId()));
      NmsAlignmentStatus oldStatus = bodPort.getNmsAlignmentStatus();
      NmsAlignmentStatus newStatus = alignmentStatus(bodPort, nbiPort);
      if (oldStatus == NmsAlignmentStatus.ALIGNED && newStatus != NmsAlignmentStatus.ALIGNED) {
        unalignedPorts.add(bodPort);
      } else if (oldStatus != NmsAlignmentStatus.ALIGNED && newStatus == NmsAlignmentStatus.ALIGNED) {
        realignedPorts.add(bodPort);
      } else if (oldStatus != newStatus) {
        alignmentChangedPorts.add(bodPort);
      }
      bodPort.setNmsAlignmentStatus(newStatus);
    }
  }

  public List<PhysicalPort> getRealignedPorts() {
    return realignedPorts;
  }

  public List<PhysicalPort> getUnalignedPorts() {
    return unalignedPorts;
  }

  public List<PhysicalPort> getAlignmentChangedPorts() {
    return alignmentChangedPorts;
  }

  private NmsAlignmentStatus alignmentStatus(PhysicalPort bodPort, Optional<NbiPort> optionalNbiPort) {
    if (!optionalNbiPort.isPresent()) {
      return NmsAlignmentStatus.DISAPPEARED;
    }
    NbiPort nbiPort = optionalNbiPort.get();
    if (bodPort.isVlanRequired() != nbiPort.isVlanRequired()) {
      return nbiPort.isVlanRequired() ? NmsAlignmentStatus.TYPE_CHANGED_TO_VLAN : NmsAlignmentStatus.TYPE_CHANGED_TO_LAN;
    } else if (bodPort.getNbiPort().getInterfaceType() != nbiPort.getInterfaceType()) {
      switch (nbiPort.getInterfaceType()) {
      case E_NNI:
        return NmsAlignmentStatus.TYPE_CHANGED_TO_ENNI;
      case UNI:
        return NmsAlignmentStatus.TYPE_CHANGED_TO_UNI;
      }
      throw new IllegalArgumentException("Unknown interface type: " + nbiPort.getInterfaceType());
    } else {
      return NmsAlignmentStatus.ALIGNED;
    }
  }

  private ImmutableMap<String, NbiPort> buildNbiPortIdMap(List<NbiPort> ports) {
    Map<String, NbiPort> physicalPorts = Maps.newHashMap();
    for (NbiPort port : ports) {
      physicalPorts.put(port.getNmsPortId(), port);
    }
    return ImmutableMap.copyOf(physicalPorts);
  }
}