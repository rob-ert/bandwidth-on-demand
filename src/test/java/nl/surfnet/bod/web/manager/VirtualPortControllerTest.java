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
package nl.surfnet.bod.web.manager;

import static nl.surfnet.bod.web.manager.VirtualPortController.MODEL_KEY_LIST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.support.*;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.Model;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class VirtualPortControllerTest {

  @InjectMocks
  private VirtualPortController subject;

  @Mock
  private VirtualPortService virtualPortServiceMock;

  @Mock
  private VirtualResourceGroupService virtualResourceGroupServiceMock;

  @Mock
  private PhysicalResourceGroupService physicalResourceGroupServiceMock;

  @Mock
  private PhysicalPortService physicalPortServiceMock;

  @Before
  public void login() {
    Security.setUserDetails(new RichUserDetailsFactory().addUserGroup("urn:manager-group").create());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void listShouldFindEntries() {
    ModelStub model = new ModelStub();

    when(virtualPortServiceMock.findEntries(0, WebUtils.MAX_ITEMS_PER_PAGE)).thenReturn(
        Lists.newArrayList(new VirtualPortFactory().create()));

    subject.list(1, model);

    assertThat(model.asMap(), hasKey(MODEL_KEY_LIST));
    assertThat(model.asMap(), hasKey(WebUtils.MAX_PAGES_KEY));

    assertThat((Collection<VirtualPort>) model.asMap().get(MODEL_KEY_LIST), hasSize(1));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void populatePhysicalResourceGroupAndPortsShouldFilterOutEmptyGroups() {
    RichUserDetails user = new RichUserDetailsFactory().create();
    Security.setUserDetails(user);
    Model model = new ModelStub();

    PhysicalPort port = new PhysicalPortFactory().create();
    PhysicalResourceGroup groupWithPorts = new PhysicalResourceGroupFactory().addPhysicalPort(port).create();
    PhysicalResourceGroup groupWithoutPorts = new PhysicalResourceGroupFactory().create();

    when(physicalResourceGroupServiceMock.findAllForManager(user)).thenReturn(
        Lists.newArrayList(groupWithPorts, groupWithoutPorts));

    subject.populatePhysicalResourceGroups(model);

    assertThat(model.asMap(), hasKey("physicalResourceGroups"));
    List<PhysicalResourceGroup> groups = (List<PhysicalResourceGroup>) model.asMap().get("physicalResourceGroups");
    assertThat(groups, hasSize(1));
    assertThat(groups, contains(groupWithPorts));

    assertThat(model.asMap(), hasKey("physicalPorts"));
    List<PhysicalPort> ports = (List<PhysicalPort>) model.asMap().get("physicalPorts");
    assertThat(ports, contains(port));
  }

  @Test
  public void shouldSetVirtualResourceGroupOnPort() {
    ModelStub model = new ModelStub();
    VirtualResourceGroup vGroup = new VirtualResourceGroupFactory().setSurfConextGroupName("urn:some-user-group").create();

    when(virtualResourceGroupServiceMock.find(1L)).thenReturn(vGroup);

    subject.createForm(null, null, 1L, model);

    VirtualPort port = (VirtualPort) model.asMap().get("virtualPort");
    assertThat(port.getVirtualResourceGroup(), is(vGroup));
  }

  @Test
  public void whenBothPhysicalGroupAndPortAreSetIgnoreGroup() {
    ModelStub model = new ModelStub();
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setAdminGroupName("urn:manager-group").create();
    PhysicalPort pPort = new PhysicalPortFactory().setPhysicalResourceGroup(group).create();

    when(physicalPortServiceMock.find(1L)).thenReturn(pPort);

    subject.createForm(1L, 3L, null, model);

    VirtualPort port = (VirtualPort) model.asMap().get("virtualPort");

    assertThat(port.getPhysicalPort(), is(pPort));

    verify(physicalResourceGroupServiceMock, never()).find(3L);
  }

}
