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
package nl.surfnet.bod.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.List;


import nl.surfnet.bod.repo.InstituteRepo;
import nl.surfnet.bod.repo.PhysicalResourceGroupRepo;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/spring/appCtx.xml", "/spring/appCtx-jpa-test.xml", "/spring/appCtx-nbi-client.xml",
    "/spring/appCtx-idd-client.xml" })
@Transactional
public class PhysicalResourceGroupDbTest {

  @Autowired
  private PhysicalResourceGroupService physicalResourceGroupService;

  @Autowired
  private PhysicalResourceGroupRepo physicalResourceGroupRepo;

  @Autowired
  private InstituteRepo instituteRepo;

  private PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().setId(null).create();

  @Test
  public void countAllPhysicalResourceGroups() {
    long count = physicalResourceGroupService.count();

    instituteRepo.save(physicalResourceGroup.getInstitute());
    physicalResourceGroupService.save(physicalResourceGroup);
    physicalResourceGroupRepo.flush();

    assertThat(count + 1, is(physicalResourceGroupService.count()));
  }

  @Test
  public void findPhysicalResourceGroup() {
    instituteRepo.save(physicalResourceGroup.getInstitute());
    physicalResourceGroupService.save(physicalResourceGroup);

    PhysicalResourceGroup freshLoadedGroup = physicalResourceGroupService.find(physicalResourceGroup.getId());

    assertThat(physicalResourceGroup, is(freshLoadedGroup));
  }

  @Test
  public void findPhysicalResourceGroupEntries() {
    instituteRepo.save(physicalResourceGroup.getInstitute());
    physicalResourceGroupService.save(physicalResourceGroup);
    long count = physicalResourceGroupService.count();

    int maxResults = count > 20 ? 20 : (int) count;

    List<PhysicalResourceGroup> result = physicalResourceGroupService.findEntries(0, maxResults, new Sort("id"));

    assertThat(result, hasSize((int) count));
  }

  @Test
  public void testUpdatePhysicalResourceGroupUpdate() {
    instituteRepo.save(physicalResourceGroup.getInstitute());
    physicalResourceGroupService.save(physicalResourceGroup);

    Integer initialVersion = physicalResourceGroup.getVersion();

    physicalResourceGroup.setAdminGroup("New group");

    PhysicalResourceGroup merged = physicalResourceGroupService.update(physicalResourceGroup);

    physicalResourceGroupRepo.flush();

    assertThat(merged.getId(), is(physicalResourceGroup.getId()));
    assertThat(merged.getVersion(), greaterThan(initialVersion));
  }

  @Test
  public void savePhysicalResourceGroup() {

    assertThat(physicalResourceGroup.getId(), nullValue());
    instituteRepo.save(physicalResourceGroup.getInstitute());
    physicalResourceGroupService.save(physicalResourceGroup);

    physicalResourceGroupRepo.flush();

    assertThat(physicalResourceGroup.getId(), greaterThan(0L));
  }

  @Test
  public void deletePhysicalResourceGroup() {
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setId(null).create();

    instituteRepo.save(group.getInstitute());
    physicalResourceGroupService.save(group);
    physicalResourceGroupRepo.flush();

    physicalResourceGroupService.delete(group);
    physicalResourceGroupRepo.flush();

    assertThat(physicalResourceGroupService.find(group.getId()), nullValue());
  }

  @Test(expected = JpaSystemException.class)
  public void physicalResourceGroupWithoutANameShouldNotSave() {
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().create();
    group.setInstitute(null);

    physicalResourceGroupService.save(group);
  }

}
