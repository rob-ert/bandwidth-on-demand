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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.support.VirtualPortFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Lists;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring/appCtx.xml", "/spring/appCtx-jpa.xml", "/spring/appCtx-nbi-client.xml",
    "/spring/appCtx-idd-client.xml" })
public class VirtualPortRepoTest {

  @Autowired
  private VirtualPortRepo subject;

  @Test
  public void testSave() {
    String name = "vpOne";
    VirtualPort virtualPortOne = new VirtualPortFactory().setName(name).setPhysicalPortAdminGroup(null)
        .setPhysicalPort(null).setVirtualResourceGroup(null).create();

    subject.save(virtualPortOne);
  }

  @Test
  public void testFindByName() {
    String name = "tester";

    VirtualPort virtualPort = new VirtualPortFactory().setName(name).setVirtualResourceGroup(null)
        .setPhysicalPortAdminGroup(null).setPhysicalPort(null).create();
    Collection<VirtualPort> virtualPorts = Lists.newArrayList(virtualPort,
        new VirtualPortFactory().setName("notToBeFound").setVirtualResourceGroup(null).setPhysicalPortAdminGroup(null)
            .setPhysicalPort(null).create());

    subject.save(virtualPorts);

    VirtualPort foundPhysicalResourceGroup = subject.findByName(name);

    assertThat(foundPhysicalResourceGroup.getName(), is(name));

  }
}
