/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
package nl.surfnet.bod.support;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.collect.Lists;

import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;

public class PhysicalResourceGroupFactory {
  private static final AtomicLong COUNTER = new AtomicLong();

  private Long id = COUNTER.incrementAndGet();
  private String adminGroup = "urn:my-group";
  private List<PhysicalPort> physicalPorts = Lists.newArrayList();
  private Institute institute = new InstituteFactory().setId(id).setName("Institute " + id).create();
  private Long instituteId = id;
  private String managerEmail = "email@example.com";
  private boolean active;

  private boolean setInstituteIdFirst = false;

  public PhysicalResourceGroup create() {
    PhysicalResourceGroup group = new PhysicalResourceGroup();
    group.setId(id);

    group.setInstitute(institute);
    group.setAdminGroup(adminGroup);
    group.setPhysicalPorts(physicalPorts);
    for (PhysicalPort port : physicalPorts) {
      port.setPhysicalResourceGroup(group);
    }
    group.setManagerEmail(managerEmail);
    group.setActive(active);

    return group;
  }

  public PhysicalResourceGroupFactory addPhysicalPort(PhysicalPort... ports) {
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
    this.setInstituteIdFirst = false;
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

}
