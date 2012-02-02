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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import javax.validation.ConstraintViolationException;

import nl.surfnet.bod.repo.PhysicalPortRepo;
import nl.surfnet.bod.repo.PhysicalResourceGroupRepo;
import nl.surfnet.bod.support.PhysicalPortFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring/appCtx.xml", "/spring/appCtx-jpa-test.xml", "/spring/appCtx-nbi-client.xml",
    "/spring/appCtx-idd-client.xml" })
@Transactional
public class PhysicalPortRepoImplDbTest {

  @Autowired
  private PhysicalPortRepo physicalPortRepo;

  @Autowired
  private PhysicalResourceGroupRepo physicalResourceGroupRepo;

  @Test
  public void findPhysicalPort() {
    PhysicalPort physicalPort = new PhysicalPortFactory().create();
    save(physicalPort);

    PhysicalPort freshObj = physicalPortRepo.findOne(physicalPort.getId());

    assertThat(freshObj, is(physicalPort));
  }

  @Test
  public void updatePhysicalPortUpdate() {
    PhysicalPort physicalPort = new PhysicalPortFactory().create();
    save(physicalPort);

    Integer initialVersion = physicalPort.getVersion();
    physicalPort.setNocLabel("New name");

    PhysicalPort merged = physicalPortRepo.saveAndFlush(physicalPort);

    assertThat(merged.getId(), is(physicalPort.getId()));
    assertThat(merged.getVersion(), greaterThan(initialVersion));
  }

  private void save(PhysicalPort port) {
    port.setId(null);
    port.getPhysicalResourceGroup().setId(null);
    physicalResourceGroupRepo.save(port.getPhysicalResourceGroup());
    physicalPortRepo.saveAndFlush(port);
  }

  @Test
  public void savePhysicalPort() {
    PhysicalPort physicalPort = new PhysicalPortFactory().create();
    save(physicalPort);

    physicalPortRepo.flush();

    assertThat(physicalPort.getId(), greaterThan(0L));
  }

  @Test
  public void updatePhysicalPort() {
    PhysicalPort physicalPort = new PhysicalPortFactory().create();
    save(physicalPort);

    physicalPortRepo.saveAndFlush(physicalPort);

    assertThat(physicalPort.getId(), greaterThan(0L));
  }

  @Test
  public void deletePhysicalPort() {
    PhysicalPort physicalPort = new PhysicalPortFactory().create();
    save(physicalPort);

    physicalPortRepo.delete(physicalPort);
    physicalPortRepo.flush();

    assertThat(physicalPortRepo.findOne(physicalPort.getId()), nullValue());
  }

  @Test(expected = ConstraintViolationException.class)
  public void aPortShouldNotSaveWithoutAName() {
    PhysicalPort port = new PhysicalPortFactory().setNocLabel("").create();

    physicalPortRepo.save(port);
  }
}
