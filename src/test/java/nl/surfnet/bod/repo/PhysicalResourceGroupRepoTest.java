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

import java.util.Arrays;
import java.util.Collection;

import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.support.InstituteFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring/appCtx.xml", "/spring/appCtx-jpa-test.xml",
    "/spring/appCtx-nbi-client.xml", "/spring/appCtx-idd-client.xml" })
@Transactional
public class PhysicalResourceGroupRepoTest {

  @Autowired
  private PhysicalResourceGroupRepo subject;

  @Autowired
  private InstituteRepo instituteRepo;

  @Test
  public void testFindByInstituteId() {
    PhysicalResourceGroupFactory physicalResourceGroupFactory = new PhysicalResourceGroupFactory();

    Institute instituteOne = new InstituteFactory().setId(1L).create();
    instituteRepo.save(instituteOne);

    PhysicalResourceGroup physicalResourceGroupOne = physicalResourceGroupFactory.setId(null)
        .setInstitute(instituteOne).create();

    Institute instituteTwo = new InstituteFactory().setId(2L).create();
    instituteRepo.save(instituteTwo);
    PhysicalResourceGroup physicalResourceGroupTwo = physicalResourceGroupFactory.setId(null)
        .setInstitute(instituteTwo).create();

    given(physicalResourceGroupOne, physicalResourceGroupTwo);

    PhysicalResourceGroup foundPhysicalResourceGroup = subject.findByInstituteId(1L);

    assertThat(foundPhysicalResourceGroup.getName(), is("Customer One"));
  }

  @Test
  public void testFindByAdminGroups() {
    PhysicalResourceGroupFactory physicalResourceGroupFactory = new PhysicalResourceGroupFactory();

    String firstAdminGroup = "urn:firstGroup";
    Collection<String> adminGroups = ImmutableList.of(firstAdminGroup, "urn:secondGroup");

    Institute instituteOne = new InstituteFactory().setId(1L).create();
    instituteRepo.save(instituteOne);
    PhysicalResourceGroup firstPhysicalResourceGroup = physicalResourceGroupFactory.setId(null)
        .setAdminGroup(firstAdminGroup).setInstitute(instituteOne).create();

    Institute instituteTwo = new InstituteFactory().setId(2L).create();
    instituteRepo.save(instituteTwo);
    given(firstPhysicalResourceGroup, physicalResourceGroupFactory.setId(null).setInstitute(instituteTwo)
        .setAdminGroup("urn:noMatch").create());

    Collection<PhysicalResourceGroup> foundAdminGroups = subject.findByAdminGroupIn(adminGroups);

    assertThat(foundAdminGroups, hasSize(1));
    assertThat(foundAdminGroups.iterator().next().getAdminGroup(), is(firstAdminGroup));
  }

  private void given(PhysicalResourceGroup... groups) {
    subject.save(Arrays.asList(groups));
  }

}
