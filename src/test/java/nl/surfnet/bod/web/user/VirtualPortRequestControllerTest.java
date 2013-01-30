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
package nl.surfnet.bod.web.user;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.support.*;
import nl.surfnet.bod.util.MessageManager;
import nl.surfnet.bod.util.MessageRetriever;
import nl.surfnet.bod.web.base.MessageView;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.user.VirtualPortRequestController.RequestCommand;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BeanPropertyBindingResult;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class VirtualPortRequestControllerTest {

  @InjectMocks
  private VirtualPortRequestController subject;

  @Mock
  private PhysicalResourceGroupService physicalResourceGroupServiceMock;
  @Mock
  private VirtualPortService virtualPortServiceMock;
  @Mock
  private VirtualResourceGroupService virtualResourceGroupServiceMock;

  @Mock
  private MessageManager messageManager;

  @Mock
  private MessageRetriever messageRetriever;

  private RichUserDetails user;

  @Before
  public void login() {
    UserGroup group1 = new UserGroupFactory().setName("A").setId("urn:user-group").create();
    UserGroup group2 = new UserGroupFactory().setName("B").create();
    UserGroup group3 = new UserGroupFactory().setName("C").create();

    user = new RichUserDetailsFactory().addUserRole().addUserGroup(group3).addUserGroup(group1).addUserGroup(group2)
        .create();
    Security.setUserDetails(user);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void findAllGroupsShouldBeSorted() {
    ModelStub model = new ModelStub();

    PhysicalResourceGroup group1 = new PhysicalResourceGroupFactory().setInstitute(
        new InstituteFactory().setName("A").create()).create();
    PhysicalResourceGroup group2 = new PhysicalResourceGroupFactory().setInstitute(
        new InstituteFactory().setName("B").create()).create();
    PhysicalResourceGroup group3 = new PhysicalResourceGroupFactory().setInstitute(
        new InstituteFactory().setName("C").create()).create();

    when(physicalResourceGroupServiceMock.findAllWithPorts()).thenReturn(Lists.newArrayList(group3, group1, group2));

    subject.selectInstitute("label", "urn", model);

    assertThat(model.asMap(), hasKey("physicalResourceGroups"));
    assertThat(((Collection<PhysicalResourceGroup>) model.asMap().get("physicalResourceGroups")), contains(group1,
        group2, group3));
  }

  @Test
  public void shouldNotCreateFormIfPhysicalResourceGroupDoesNotExist() {
    ModelStub model = new ModelStub();

    when(physicalResourceGroupServiceMock.find(1L)).thenReturn(null);

    String page = subject.requestForm(1L, null, model, model);

    assertThat(page, is("redirect:/virtualports/request"));
  }

  @Test
  public void shouldNotCreateFormIfPhysicalResourceGroupIsNotActive() {
    ModelStub model = new ModelStub();

    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setActive(false).create();

    when(physicalResourceGroupServiceMock.find(1L)).thenReturn(group);

    String page = subject.requestForm(1L, null, model, model);

    assertThat(page, is("redirect:/virtualports/request"));
  }

  @Test
  public void shouldCreateFormIfPhysicalResourceGroupIsActive() {
    ModelStub model = new ModelStub();

    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setActive(true).create();

    when(physicalResourceGroupServiceMock.find(1L)).thenReturn(group);

    String page = subject.requestForm(1L, "urn:user-group", model, model);

    assertThat(page, is("virtualports/requestform"));

    assertThat(model.asMap(), hasKey("requestCommand"));
    assertThat(model.asMap(), hasKey("user"));
    assertThat(model.asMap(), hasKey("physicalResourceGroup"));
  }

  @Test
  public void whenThePhysicalGroupHasChangedDontMailAndRedirect() {
    ModelStub model = new ModelStub();
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().create();
    RequestCommand command = new RequestCommand(group);
    command.setPhysicalResourceGroupId(2L);
    command.setUserGroupId("urn:user-group");

    when(physicalResourceGroupServiceMock.find(2L)).thenReturn(null);

    String page = subject.request(command, new BeanPropertyBindingResult(command, "command"), model, model);

    assertThat(page, is("redirect:/virtualports/request"));

    verifyNeverRequestNewVirtualPort();
  }

  @Test
  public void whenTheUserIsNotMemberOfVirtualResourceGroupDontMailAndRedirect() {
    ModelStub model = new ModelStub();
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setActive(true).create();
    RequestCommand command = new RequestCommand(group);
    command.setPhysicalResourceGroupId(2L);
    command.setUserGroupId("urn:user-group-wrong");

    when(physicalResourceGroupServiceMock.find(2L)).thenReturn(group);

    String page = subject.request(command, new BeanPropertyBindingResult(command, "command"), model, model);

    assertThat(page, is("redirect:/virtualports/request"));

    verifyNeverRequestNewVirtualPort();
  }

  @Test
  public void whenThePhysicalResourceGroupIsNotActiveDontMailAndRedirect() {
    ModelStub model = new ModelStub();
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setActive(false).create();
    RequestCommand command = new RequestCommand(group);
    command.setPhysicalResourceGroupId(2L);
    command.setUserGroupId("urn:user-group");

    when(physicalResourceGroupServiceMock.find(2L)).thenReturn(group);

    String page = subject.request(command, new BeanPropertyBindingResult(command, "command"), model, model);

    assertThat(page, is("redirect:/virtualports/request"));

    verifyNeverRequestNewVirtualPort();
  }

  @Test
  public void requestShouldSentAnEmail() {
    ModelStub model = new ModelStub();
    PhysicalResourceGroup pGroup = new PhysicalResourceGroupFactory().setActive(true).create();
    VirtualResourceGroup vGroup = new VirtualResourceGroupFactory().create();

    RequestCommand command = new RequestCommand(pGroup);
    command.setUserLabel("new port");
    command.setPhysicalResourceGroupId(2L);
    command.setUserGroupId("urn:user-group");
    command.setBandwidth(1000);
    command.setMessage("message");

    when(virtualResourceGroupServiceMock.findByAdminGroup("urn:user-group")).thenReturn(vGroup);
    when(physicalResourceGroupServiceMock.find(2L)).thenReturn(pGroup);

    String page = subject.request(command, new BeanPropertyBindingResult(command, "command"), model, model);

    assertThat(page, is("redirect:/user"));

    verify(virtualPortServiceMock).requestNewVirtualPort(user, vGroup, pGroup, "new port", 1000, "message");
  }

  @Test
  public void whenVirtualResourceGroupDoesNotExistsItShouldBeCreated() {
    ModelStub model = new ModelStub();
    PhysicalResourceGroup pGroup = new PhysicalResourceGroupFactory().setActive(true).create();

    RequestCommand command = new RequestCommand(pGroup);
    command.setPhysicalResourceGroupId(2L);
    command.setUserGroupId("urn:user-group");
    command.setBandwidth(1111);
    command.setUserLabel("new port");
    command.setMessage("I want!");

    when(virtualResourceGroupServiceMock.findByAdminGroup("urn:user-group")).thenReturn(null);
    when(physicalResourceGroupServiceMock.find(2L)).thenReturn(pGroup);

    String page = subject.request(command, new BeanPropertyBindingResult(command, "command"), model, model);

    assertThat(page, is("redirect:/user"));

    verify(virtualResourceGroupServiceMock).save(any(VirtualResourceGroup.class));
    verify(virtualPortServiceMock).requestNewVirtualPort(eq(user), any(VirtualResourceGroup.class), eq(pGroup),
        eq("new port"), eq(1111), eq("I want!"));
  }

  @Test
  public void doNotSwitchRoleWhenUserHasSelectedRole() {

    ModelStub model = new ModelStub();
    PhysicalResourceGroup pGroup = new PhysicalResourceGroupFactory().setActive(true).create();

    RequestCommand command = new RequestCommand(pGroup);
    command.setUserLabel("new port");
    command.setPhysicalResourceGroupId(2L);
    command.setUserGroupId("urn:user-group");
    command.setBandwidth(1111);
    command.setMessage("I want!");

    when(virtualResourceGroupServiceMock.findByAdminGroup("urn:user-group")).thenReturn(null);
    when(physicalResourceGroupServiceMock.find(2L)).thenReturn(pGroup);

    subject.request(command, new BeanPropertyBindingResult(command, "command"), model, model);

    assertThat("Context should not be cleared", SecurityContextHolder.getContext().getAuthentication(), notNullValue());

    verify(virtualResourceGroupServiceMock).save(any(VirtualResourceGroup.class));
    verify(virtualPortServiceMock).requestNewVirtualPort(eq(user), any(VirtualResourceGroup.class), eq(pGroup),
        eq("new port"), eq(1111), eq("I want!"));
  }

  @Test
  public void doSwitchRoleWhenUserHasNoSelectedRole() {
    user = new RichUserDetailsFactory().addBodRoles(BodRole.createNewUser()).addUserGroup(
        new UserGroupFactory().setId("urn:user-group").create()).create();
    Security.setUserDetails(user);

    ModelStub model = new ModelStub();
    PhysicalResourceGroup pGroup = new PhysicalResourceGroupFactory().setActive(true).create();

    RequestCommand command = new RequestCommand(pGroup);
    command.setPhysicalResourceGroupId(2L);
    command.setUserLabel("port");
    command.setUserGroupId("urn:user-group");
    command.setBandwidth(1111);
    command.setMessage("I want!");

    when(virtualResourceGroupServiceMock.findByAdminGroup("urn:user-group")).thenReturn(null);
    when(physicalResourceGroupServiceMock.find(2L)).thenReturn(pGroup);

    subject.request(command, new BeanPropertyBindingResult(command, "command"), model, model);

    assertThat("Context should not be cleared", SecurityContextHolder.getContext().getAuthentication(), nullValue());

    verify(virtualResourceGroupServiceMock).save(any(VirtualResourceGroup.class));
    verify(virtualPortServiceMock).requestNewVirtualPort(eq(user), any(VirtualResourceGroup.class), eq(pGroup),
        eq("port"), eq(1111), eq("I want!"));
  }

  @Test
  public void requestVirtualPortWithoutEmailShouldGiveErrorMessage() {
    RichUserDetails user = new RichUserDetailsFactory().setEmail("").create();
    Security.setUserDetails(user);

    ModelStub model = new ModelStub();
    when(messageRetriever.getMessage(anyString(), anyString())).thenReturn("test message");

    String resultPage = subject.selectTeam(model);

    assertThat(resultPage, is(MessageView.PAGE_URL));
    assertThat(model.asMap().get(MessageView.MODEL_KEY), notNullValue());
  }

  private void verifyNeverRequestNewVirtualPort() {
    verify(virtualPortServiceMock, never()).requestNewVirtualPort(any(RichUserDetails.class),
        any(VirtualResourceGroup.class), any(PhysicalResourceGroup.class), anyString(), anyInt(), anyString());
  }

}
