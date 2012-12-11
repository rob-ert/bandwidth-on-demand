/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.support;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;

import com.google.common.collect.Lists;

public class VirtualResourceGroupFactory {

  public static final String SURFCONEXT_GROUP_ID = "urn:bandwidth-on-demand";

  private static final AtomicLong COUNTER = new AtomicLong();

  private Long id = COUNTER.incrementAndGet();
  private Integer version;
  private String adminGroup = SURFCONEXT_GROUP_ID + id;
  private String name = "VRG " + id;
  private String description = "Some virtual resource group";

  private final Collection<VirtualPort> virtualPorts = Lists.newArrayList();
  private final Collection<Reservation> reservations = Lists.newArrayList();

  public VirtualResourceGroup create() {
    VirtualResourceGroup vrg = new VirtualResourceGroup();

    vrg.setId(id);
    vrg.setVersion(version);
    vrg.setName(name);
    vrg.setDescription(description);
    vrg.setAdminGroup(adminGroup);
    vrg.setVirtualPorts(virtualPorts);
    vrg.setReservations(reservations);

    return vrg;
  }

  public VirtualResourceGroupFactory setId(Long id) {
    this.id = id;
    return this;
  }

  public VirtualResourceGroupFactory setDescription(String description) {
    this.description = description;
    return this;
  }

  public VirtualResourceGroupFactory setVersion(Integer version) {
    this.version = version;
    return this;
  }

  public VirtualResourceGroupFactory setName(String name) {
    this.name = name;
    return this;
  }

  public VirtualResourceGroupFactory setAdminGroup(String adminGroup) {
    this.adminGroup = adminGroup;
    return this;
  }

  public VirtualResourceGroupFactory addVirtualPorts(VirtualPort... ports) {
    this.virtualPorts.addAll(Arrays.asList(ports));
    return this;
  }

  public VirtualResourceGroupFactory addReservations(Reservation... reservations) {
    this.reservations.addAll(Arrays.asList(reservations));
    return this;
  }

  public VirtualResourceGroupFactory withNoId() {
    this.version = null;
    this.id = null;
    return this;
  }

}
