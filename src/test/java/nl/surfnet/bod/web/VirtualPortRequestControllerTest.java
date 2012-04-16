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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.support.*;
import nl.surfnet.bod.web.VirtualPortRequestController.RequestCommand;
import nl.surfnet.bod.web.VirtualPortRequestController.UserGroupView;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
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

  @SuppressWarnings("unused")
  @Mock
  private MessageSource messageSourceMock;

  private RichUserDetails user;

  @Before
  public void login() {
    UserGroup group1 = new UserGroupFactory().setName("A").setId("urn:user-group").create();
    UserGroup group2 = new UserGroupFactory().setName("B").create();
    UserGroup group3 = new UserGroupFactory().setName("C").create();

    user = new RichUserDetailsFactory().addUserGroup(group3).addUserGroup(group1).addUserGroup(group2).create();

    Security.setUserDetails(user);
  }

  @Test
  public void findAllTeamsShouldBeSorted() {
    ModelStub model = new ModelStub();

    subject.selectTeam(model);

    List<UserGroupView> groups = (List<UserGroupView>) model.asMap().get("userGroups");

    assertThat(groups, hasSize(3));
    assertThat(groups.get(0).getName(), is("A"));
    assertThat(groups.get(1).getName(), is("B"));
    assertThat(groups.get(2).getName(), is("C"));
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

    subject.selectInstitute("", model);

    assertThat(model.asMap(), hasKey("physicalResourceGroups"));
    assertThat(((Collection<PhysicalResourceGroup>) model.asMap().get("physicalResourceGroups")),
        contains(group1, group2, group3));
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
    command.setPhysicalResourceGroupId(2L);
    command.setUserGroupId("urn:user-group");
    command.setBandwidth(1000);
    command.setMessage("message");

    when(virtualResourceGroupServiceMock.findBySurfconextGroupId("urn:user-group")).thenReturn(vGroup);
    when(physicalResourceGroupServiceMock.find(2L)).thenReturn(pGroup);

    String page = subject.request(command, new BeanPropertyBindingResult(command, "command"), model, model);

    assertThat(page, is("redirect:/"));

    verify(virtualPortServiceMock).requestNewVirtualPort(user, vGroup, pGroup, 1000, "message");
  }

  @Test
  public void whenVirtualResourceGroupDoesNotExistsItShouldBeCreated() {
    ModelStub model = new ModelStub();
    PhysicalResourceGroup pGroup = new PhysicalResourceGroupFactory().setActive(true).create();

    RequestCommand command = new RequestCommand(pGroup);
    command.setPhysicalResourceGroupId(2L);
    command.setUserGroupId("urn:user-group");
    command.setBandwidth(1111);
    command.setMessage("I want!");

    when(virtualResourceGroupServiceMock.findBySurfconextGroupId("urn:user-group")).thenReturn(null);
    when(physicalResourceGroupServiceMock.find(2L)).thenReturn(pGroup);

    String page = subject.request(command, new BeanPropertyBindingResult(command, "command"), model, model);

    assertThat(page, is("redirect:/"));

    verify(virtualResourceGroupServiceMock).save(any(VirtualResourceGroup.class));
    verify(virtualPortServiceMock).requestNewVirtualPort(eq(user), any(VirtualResourceGroup.class), eq(pGroup),
        eq(1111), eq("I want!"));
  }

  @Test
  public void doNotSwitchRoleWhenUserHasSelectedRole() {
    ModelStub model = new ModelStub();
    PhysicalResourceGroup pGroup = new PhysicalResourceGroupFactory().setActive(true).create();

    RequestCommand command = new RequestCommand(pGroup);
    command.setPhysicalResourceGroupId(2L);
    command.setUserGroupId("urn:user-group");
    command.setBandwidth(1111);
    command.setMessage("I want!");

    when(virtualResourceGroupServiceMock.findBySurfconextGroupId("urn:user-group")).thenReturn(null);
    when(physicalResourceGroupServiceMock.find(2L)).thenReturn(pGroup);

    // Give user a selectedRole
    user.setSelectedRole(new BodRoleFactory().create());
    String page = subject.request(command, new BeanPropertyBindingResult(command, "command"), model, model);

    assertThat("Context should not be cleared", SecurityContextHolder.getContext().getAuthentication(), notNullValue());

    verify(virtualResourceGroupServiceMock).save(any(VirtualResourceGroup.class));
    verify(virtualPortServiceMock).requestNewVirtualPort(eq(user), any(VirtualResourceGroup.class), eq(pGroup),
        eq(1111), eq("I want!"));
  }

  @Test
  public void doSwitchRoleWhenUserHasNoSelectedRole() {
    ModelStub model = new ModelStub();
    PhysicalResourceGroup pGroup = new PhysicalResourceGroupFactory().setActive(true).create();

    RequestCommand command = new RequestCommand(pGroup);
    command.setPhysicalResourceGroupId(2L);
    command.setUserGroupId("urn:user-group");
    command.setBandwidth(1111);
    command.setMessage("I want!");

    when(virtualResourceGroupServiceMock.findBySurfconextGroupId("urn:user-group")).thenReturn(null);
    when(physicalResourceGroupServiceMock.find(2L)).thenReturn(pGroup);

    // Give user a selectedRole
    user.setSelectedRole(null);
    String page = subject.request(command, new BeanPropertyBindingResult(command, "command"), model, model);

    assertThat("Context should not be cleared", SecurityContextHolder.getContext().getAuthentication(), nullValue());

    verify(virtualResourceGroupServiceMock).save(any(VirtualResourceGroup.class));
    verify(virtualPortServiceMock).requestNewVirtualPort(eq(user), any(VirtualResourceGroup.class), eq(pGroup),
        eq(1111), eq("I want!"));
  }

  private void verifyNeverRequestNewVirtualPort() {
    verify(virtualPortServiceMock, never()).requestNewVirtualPort(any(RichUserDetails.class),
        any(VirtualResourceGroup.class), any(PhysicalResourceGroup.class), anyInt(), anyString());
  }

}
