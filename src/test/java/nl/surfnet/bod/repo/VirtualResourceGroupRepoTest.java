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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Collection;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring/appCtx.xml", "/spring/appCtx-jpa-test.xml", "/spring/appCtx-nbi-client.xml",
    "/spring/appCtx-idd-client.xml" })
@Transactional
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class VirtualResourceGroupRepoTest {

  // override bod.properties to run test and bod server at the same time
  static {
    System.setProperty("snmp.host", "localhost/1622");
  }

  @Resource
  private VirtualResourceGroupRepo subject;

  @Test
  public void testSave() {
    String nameOne = "groupOne";
    VirtualResourceGroup vrGroup = new VirtualResourceGroupFactory().setName("one").setSurfconextGroupId(nameOne)
        .create();

    subject.save(vrGroup);
  }

  @Test
  public void testSaveNameNotUnique() {
    String nameOne = "groupOne";
    VirtualResourceGroup vrGroupOne = new VirtualResourceGroupFactory().setName("one").setSurfconextGroupId(nameOne)
        .create();

    subject.save(vrGroupOne);

    VirtualResourceGroup vrGroupTwo = new VirtualResourceGroupFactory().setSurfconextGroupId(nameOne).create();

    try {
      subject.save(vrGroupTwo);

      fail("ConstraintViolation excpected");
    }
    catch (JpaSystemException exc) {

      assertEquals(exc.getCause().getCause().getClass(), ConstraintViolationException.class);
    }
  }

  @Test
  public void testfindBySurfConextGroupName() {
    String firstAdminGroup = "urn:firstGroup";
    Collection<String> adminGroups = Lists.newArrayList(firstAdminGroup, "urn:secondGroup");
    VirtualResourceGroup firstVirtualResourceGroup = new VirtualResourceGroupFactory().setName("testName")
        .setSurfconextGroupId(firstAdminGroup).create();

    Collection<VirtualResourceGroup> virtualResourceGroups = Lists.newArrayList(firstVirtualResourceGroup,
        new VirtualResourceGroupFactory().setName("noMatcher").setSurfconextGroupId("urn:noMatch").create());
    subject.save(virtualResourceGroups);

    Collection<VirtualResourceGroup> foundAdminGroups = subject.findBySurfconextGroupIdIn(adminGroups);

    assertThat(foundAdminGroups, hasSize(1));
    assertThat(foundAdminGroups.iterator().next().getSurfconextGroupId(), is(firstAdminGroup));
  }

}
