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
package nl.surfnet.bod.support;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.domain.UniPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;

import com.google.common.collect.Lists;

public class PhysicalResourceGroupFactory {
  private static final AtomicLong COUNTER = new AtomicLong();

  private Long id = COUNTER.incrementAndGet();
  private Integer version = 0;
  private String adminGroup = "urn:my-group-"+id;
  private final List<UniPort> physicalPorts = Lists.newArrayList();
  private Institute institute = new InstituteFactory().setId(id).setName("Institute " + id).create();
  private String managerEmail = "email@example.com";
  private boolean active;

  public PhysicalResourceGroup create() {
    PhysicalResourceGroup group = new PhysicalResourceGroup();
    group.setId(id);
    group.setVersion(version);

    group.setInstitute(institute);
    group.setAdminGroup(adminGroup);
    group.setPhysicalPorts(physicalPorts);
    for (UniPort port : physicalPorts) {
      port.setPhysicalResourceGroup(group);
    }
    group.setManagerEmail(managerEmail);
    group.setActive(active);

    return group;
  }

  public PhysicalResourceGroupFactory addPhysicalPort(UniPort... ports) {
    this.physicalPorts.addAll(Arrays.asList(ports));
    return this;
  }

  public PhysicalResourceGroupFactory setAdminGroup(String adminGroup) {
    this.adminGroup = adminGroup;
    return this;
  }

  public PhysicalResourceGroupFactory setId(Long id) {
    this.id = id;
    return this;
  }

  public PhysicalResourceGroupFactory setInstitute(Institute institute) {
    this.institute = institute;
    return this;
  }

  public PhysicalResourceGroupFactory setActive(boolean active) {
    this.active = active;
    return this;
  }

  public PhysicalResourceGroupFactory setManagerEmail(String managerEmail) {
    this.managerEmail = managerEmail;
    return this;
  }

  public PhysicalResourceGroupFactory withNoIds() {
    this.id = null;
    this.version = null;
    return this;
  }

}
