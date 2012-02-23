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
package nl.surfnet.bod.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.util.Collection;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.PhysicalPortJsonView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Iterables;

@RunWith(MockitoJUnitRunner.class)
public class PhyscialResourceGroupControllerTest {

  @InjectMocks
  private PhysicalResourceGroupController subject;

  @Mock
  private PhysicalResourceGroupService physicalResourceGroupServiceMock;

  @Test
  public void whenPortDoesNotExistPortAreEmpty() {
    when(physicalResourceGroupServiceMock.find(1L)).thenReturn(null);

    Collection<PhysicalPortJsonView> ports = subject.listForPhysicalResourceGroup(1L);

    assertThat(ports, hasSize(0));
  }

  @Test
  public void whenUserIsNotMemberOfAdminGroupPortsAreEmpty() {
    RichUserDetails user = new RichUserDetailsFactory().create();
    Security.setUserDetails(user);
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory()
        .addPhysicalPort(new PhysicalPortFactory().create()).create();

    when(physicalResourceGroupServiceMock.find(1L)).thenReturn(group);

    Collection<PhysicalPortJsonView> ports = subject.listForPhysicalResourceGroup(1L);

    assertThat(ports, hasSize(0));
  }

  @Test
  public void whenUserIsMemberOfAdminGroupReturnPorts() {
    RichUserDetails user = new RichUserDetailsFactory().addUserGroup("urn:group").create();
    Security.setUserDetails(user);

    PhysicalPort onlyPort = new PhysicalPortFactory().create();
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setAdminGroupName("urn:group")
        .addPhysicalPort(onlyPort).create();

    when(physicalResourceGroupServiceMock.find(1L)).thenReturn(group);

    Collection<PhysicalPortJsonView> ports = subject.listForPhysicalResourceGroup(1L);

    assertThat(ports, hasSize(1));
    assertThat(Iterables.getOnlyElement(ports).getId(), is(onlyPort.getId()));
  }

}
