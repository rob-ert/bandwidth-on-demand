/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Set;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class ManagerServiceTest {

  @Mock
  private PhysicalResourceGroupService physicalResourceGroupService;

  @Mock
  private VirtualResourceGroupService virtualResourceGroupService;

  @Mock
  private VirtualPortService virtualPortService;

  @InjectMocks
  private ManagerService subject;

  @Test
  public void shouldNotFindDuplicateAdminGroupForManager() {
    String adminGroupOne = "adminGroupOne";
    String adminGroupThree = "adminGroupThree";

    PhysicalResourceGroup prgOne = new PhysicalResourceGroupFactory().setId(1L).setAdminGroup(adminGroupOne).create();
    RichUserDetails manager = new RichUserDetailsFactory().addManagerRole(prgOne).create();

    VirtualResourceGroup vrgOne = new VirtualResourceGroupFactory().setAdminGroup(adminGroupOne).create();

    // Link to physicalports with groups one and three
    VirtualPort vpOneVrgOne = new VirtualPortFactory().setVirtualResourceGroup(vrgOne).setPhysicalPortAdminGroup(
        adminGroupOne).create();
    VirtualPort vpTwoVrgOne = new VirtualPortFactory().setVirtualResourceGroup(vrgOne).setPhysicalPortAdminGroup(
        adminGroupThree).create();
    Collection<VirtualPort> vrgOneVirtualPorts = Lists.newArrayList(vpOneVrgOne, vpTwoVrgOne);
    vrgOne.setVirtualPorts(vrgOneVirtualPorts);

    when(virtualResourceGroupService.findEntriesForManager(manager.getSelectedRole())).thenReturn(
        Lists.newArrayList(vrgOne));

    Set<String> groupsForManager = subject.findAllAdminGroupsForManager(manager.getSelectedRole());
    assertThat(groupsForManager, hasSize(1));
    assertThat(groupsForManager, hasItem(adminGroupOne));
  }


  @Test
  public void shouldFindAdminGroupsForManager() {
    String adminGroupOne = "adminGroupOne";
    String adminGroupThree = "adminGroupThree";

    PhysicalResourceGroup prgOne = new PhysicalResourceGroupFactory().setId(1L).setAdminGroup(adminGroupOne).create();
    RichUserDetails manager = new RichUserDetailsFactory().addManagerRole(prgOne).create();

    VirtualResourceGroup vrgThree = new VirtualResourceGroupFactory().setAdminGroup(adminGroupThree).create();

    // Link to physicalports with groups one and three
    VirtualPort vpOneVrgOne = new VirtualPortFactory().setVirtualResourceGroup(vrgThree).setPhysicalPortAdminGroup(
        adminGroupOne).create();
    VirtualPort vpTwoVrgOne = new VirtualPortFactory().setVirtualResourceGroup(vrgThree).setPhysicalPortAdminGroup(
        adminGroupThree).create();
    Collection<VirtualPort> vrgOneVirtualPorts = Lists.newArrayList(vpOneVrgOne, vpTwoVrgOne);
    vrgThree.setVirtualPorts(vrgOneVirtualPorts);

    when(virtualResourceGroupService.findEntriesForManager(manager.getSelectedRole())).thenReturn(
        Lists.newArrayList(vrgThree));

    Set<String> groupsForManager = subject.findAllAdminGroupsForManager(manager.getSelectedRole());
    assertThat(groupsForManager, hasSize(2));
    assertThat(groupsForManager, hasItems(adminGroupOne, adminGroupThree));
  }

}
