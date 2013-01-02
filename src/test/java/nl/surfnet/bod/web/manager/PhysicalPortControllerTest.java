/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.web.manager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.InstituteService;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.manager.PhysicalPortController.UpdateManagerLabelCommand;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.PhysicalPortView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class PhysicalPortControllerTest {

  @InjectMocks
  private PhysicalPortController subject;

  @Mock
  private PhysicalPortService physicalPortServiceMock;

  @Mock
  private PhysicalResourceGroupService physicalResourceGroupService;

  @Mock
  private VirtualPortService virtualPortServiceMock;

  @Mock
  private InstituteService instituteService;
  
  @Mock
  private ReservationService reservationService;

  private RichUserDetails user;

  private PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().create();

  @Before
  public void setAuthenticatedUser() {

    BodRole managerRole = BodRole.createManager(physicalResourceGroup);

    user = new RichUserDetailsFactory().addUserGroup("urn:manager-group").addBodRoles(managerRole).create();
    Security.setUserDetails(user);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void listPorts() {
    Model model = new ModelStub();

    when(physicalResourceGroupService.find(anyLong())).thenReturn(physicalResourceGroup);

    when(
        physicalPortServiceMock.findAllocatedEntriesForPhysicalResourceGroup(eq(physicalResourceGroup), eq(0),
            anyInt(), any(Sort.class))).thenReturn(Lists.newArrayList(new PhysicalPortFactory().setId(2L).create()));

    subject.list(null, null, null, model);

    assertThat(model.asMap(), hasKey("list"));
    assertThat(model.asMap(), hasKey(WebUtils.MAX_PAGES_KEY));

    Collection<PhysicalPortView> ports = (Collection<PhysicalPortView>) model.asMap().get("list");
    assertThat(ports, hasSize(1));
    assertThat(ports.iterator().next().getId(), is(2L));
  }

  @SuppressWarnings("unchecked")
  public void listPortsWithFilter() {
    Model model = new ModelStub();

    PhysicalPort portOne = new PhysicalPortFactory().setId(1L).create();

    when(
        physicalPortServiceMock.findAllocatedEntriesForPhysicalResourceGroup(eq(physicalResourceGroup), anyInt(),
            anyInt(), any(Sort.class))).thenReturn(Lists.newArrayList(portOne));

    when(physicalResourceGroupService.find(physicalResourceGroup.getId())).thenReturn(physicalResourceGroup);

    subject.list(null, null, "1", model);

    assertThat(model.asMap(), hasKey("list"));
    assertThat((Long) model.asMap().get(WebUtils.FILTER_SELECT), is(1L));

    Collection<PhysicalPortView> ports = (Collection<PhysicalPortView>) model.asMap().get("list");
    List<PhysicalPortView> portList = new ArrayList<PhysicalPortView>(ports);

    assertThat(portList.size(), is(1));
    assertThat(portList.get(0).getId(), is(portOne.getId()));
  }

  @Test
  public void updateForNullPort() {
    UpdateManagerLabelCommand command = new PhysicalPortController.UpdateManagerLabelCommand();
    command.setId(234L);

    when(physicalPortServiceMock.find(234L)).thenReturn(null);

    subject.update(command, new BeanPropertyBindingResult(command, "updateCommand"), new ModelStub());

    verify(physicalPortServiceMock, never()).update(any(PhysicalPort.class));
  }

  @Test
  public void updateForIllegalPort() {
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setAdminGroup("urn:manager-group").create();
    PhysicalPort port = new PhysicalPortFactory().setPhysicalResourceGroup(group).create();
    PhysicalResourceGroup illegalGroup = new PhysicalResourceGroupFactory().setAdminGroup("urn:illegal-group").create();
    PhysicalPort illegalPort = new PhysicalPortFactory().setPhysicalResourceGroup(illegalGroup).create();

    UpdateManagerLabelCommand command = new PhysicalPortController.UpdateManagerLabelCommand(port);
    command.setId(234L);

    when(physicalPortServiceMock.find(234L)).thenReturn(illegalPort);

    subject.update(command, new BeanPropertyBindingResult(command, "updateCommand"), new ModelStub());

    verify(physicalPortServiceMock, never()).update(any(PhysicalPort.class));
  }

  @Test
  public void updateForPort() {
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setAdminGroup("urn:manager-group").create();
    PhysicalPort port = new PhysicalPortFactory().setId(1L).setPhysicalResourceGroup(group).create();

    UpdateManagerLabelCommand command = new PhysicalPortController.UpdateManagerLabelCommand(port);

    when(physicalPortServiceMock.find(1L)).thenReturn(port);

    subject.update(command, new BeanPropertyBindingResult(command, "updateCommand"), new ModelStub());

    verify(physicalPortServiceMock).update(port);
  }

  @Test
  public void updateFormForNonExistingPort() {
    when(physicalPortServiceMock.find(101L)).thenReturn(null);

    String page = subject.updateForm(101L, new ModelStub());

    assertThat(page, is("manager/physicalports"));
  }

  @Test
  public void updateFormForIllegalPort() {
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setAdminGroup("urn:illegal-group").create();
    PhysicalPort port = new PhysicalPortFactory().setPhysicalResourceGroup(group).create();

    when(physicalPortServiceMock.find(101L)).thenReturn(port);

    String page = subject.updateForm(101L, new ModelStub());

    assertThat(page, is("manager/physicalports"));
  }

  @Test
  public void updateFormForPort() {
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setAdminGroup("urn:manager-group").create();
    PhysicalPort port = new PhysicalPortFactory().setPhysicalResourceGroup(group).create();

    when(physicalPortServiceMock.find(101L)).thenReturn(port);

    ModelStub model = new ModelStub();
    String page = subject.updateForm(101L, model);

    assertThat(page, is("manager/physicalports/update"));
    assertThat(model.asMap(), hasKey("physicalPort"));
  }

}
