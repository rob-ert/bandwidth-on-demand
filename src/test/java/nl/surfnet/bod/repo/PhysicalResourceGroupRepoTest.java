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
package nl.surfnet.bod.repo;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.ImmutableList;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring/appCtx.xml", "/spring/appCtx-jpa.xml", "/spring/appCtx-nbi-client.xml",
"/spring/appCtx-idd-client.xml" })
public class PhysicalResourceGroupRepoTest {

  @Autowired
  private PhysicalResourceGroupRepo subject;

  @Test
  public void testFindByName() {
    String name = "tester";
    PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().setName(name).create();
    given(physicalResourceGroup, new PhysicalResourceGroupFactory().setName("notToBeFound").create());

    PhysicalResourceGroup foundPhysicalResourceGroup = subject.findByName(name);

    assertThat(foundPhysicalResourceGroup.getName(), is(name));

  }

  @Test
  public void testFindByAdminGroups() {
    String firstAdminGroup = "urn:firstGroup";
    Collection<String> adminGroups = ImmutableList.of(firstAdminGroup, "urn:secondGroup");
    PhysicalResourceGroup firstPhysicalResourceGroup = new PhysicalResourceGroupFactory().setName("testName")
        .setAdminGroupName(firstAdminGroup).create();

    given(firstPhysicalResourceGroup, new PhysicalResourceGroupFactory().setAdminGroupName("urn:noMatch").create());

    Collection<PhysicalResourceGroup> foundAdminGroups = subject.findByAdminGroupIn(adminGroups);

    assertThat(foundAdminGroups, hasSize(1));
    assertThat(foundAdminGroups.iterator().next().getAdminGroup(), is(firstAdminGroup));
  }

  private void given(PhysicalResourceGroup... groups) {
    subject.save(Arrays.asList(groups));
  }

}
