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

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.List;

import javax.validation.ConstraintViolationException;

import nl.surfnet.bod.repo.PhysicalResourceGroupRepo;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.support.PhysicalResourceGroupDataOnDemand;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/spring/appCtx*.xml")
@Transactional
public class PhysicalResourceGroupDbTest {

  @Autowired
  private PhysicalResourceGroupDataOnDemand dod;

  @Autowired
  private PhysicalResourceGroupService physicalResourceGroupService;

  @Autowired
  private PhysicalResourceGroupRepo physicalResourceGroupRepo;

  @Test
  public void countAllPhysicalResourceGroups() {
    assertNotNull(dod.getRandomPhysicalResourceGroup());

    long count = physicalResourceGroupService.count();

    assertThat(count, greaterThan(0L));
  }

  @Test
  public void findPhysicalResourceGroup() {
    PhysicalResourceGroup randomGroup = dod.getRandomPhysicalResourceGroup();

    PhysicalResourceGroup freshLoadedGroup = physicalResourceGroupService.find(randomGroup.getId());

    assertThat(randomGroup, is(freshLoadedGroup));
  }

  @Test
  public void findAllPhysicalResourceGroups() {
    dod.getRandomPhysicalResourceGroup();

    List<PhysicalResourceGroup> result = physicalResourceGroupService.findAll();

    assertThat(result, hasSize(greaterThan(0)));
  }

  @Test
  public void findPhysicalResourceGroupEntries() {
    dod.getRandomPhysicalResourceGroup();

    long count = physicalResourceGroupService.count();

    int maxResults = count > 20 ? 20 : (int) count;

    List<PhysicalResourceGroup> result = physicalResourceGroupService.findEntries(0, maxResults);

    assertThat(result, hasSize((int) count));
  }

  @Test
  public void testUpdatePhysicalResourceGroupUpdate() {
    PhysicalResourceGroup obj = dod.getRandomPhysicalResourceGroup();

    Integer initialVersion = obj.getVersion();

    obj.setName("New name");

    PhysicalResourceGroup merged = physicalResourceGroupService.update(obj);

    physicalResourceGroupRepo.flush();

    assertThat(merged.getId(), is(obj.getId()));
    assertThat(merged.getVersion(), greaterThan(initialVersion));
  }

  @Test
  public void savePhysicalResourceGroup() {
    PhysicalResourceGroup obj = dod.getNewTransientPhysicalResourceGroup(Integer.MAX_VALUE);

    physicalResourceGroupService.save(obj);

    physicalResourceGroupRepo.flush();

    assertThat(obj.getId(), greaterThan(0L));
  }

  @Test
  public void deletePhysicalResourceGroup() {
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setId(null).create();
    physicalResourceGroupService.save(group);
    physicalResourceGroupRepo.flush();

    physicalResourceGroupService.delete(group);
    physicalResourceGroupRepo.flush();

    assertThat(physicalResourceGroupService.find(group.getId()), nullValue());
  }

  @Test(expected = ConstraintViolationException.class)
  public void physicalResourceGroupWithoutANameShouldNotSave() {
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setName(null).create();

    physicalResourceGroupService.save(group);
  }

  @Test(expected = ConstraintViolationException.class)
  public void physicalResourceGroupWithAnEmptyNameShouldNotSave() {
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setName("").create();

    physicalResourceGroupService.save(group);
  }

  @Test(expected = JpaSystemException.class)
  public void physicalResourceGroupInstituteNameShouldBeUnique() {
    PhysicalResourceGroup group1 = new PhysicalResourceGroupFactory().create();
    PhysicalResourceGroup group2 = new PhysicalResourceGroupFactory().create();

    physicalResourceGroupService.save(group1);
    physicalResourceGroupService.save(group2);
  }

}
