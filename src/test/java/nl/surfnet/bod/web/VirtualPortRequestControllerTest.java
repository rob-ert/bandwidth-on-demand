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
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.service.EmailSender;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.web.VirtualPortRequestController.RequestCommand;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.validation.BeanPropertyBindingResult;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class VirtualPortRequestControllerTest {

  @InjectMocks
  private VirtualPortRequestController subject;

  @Mock
  private PhysicalResourceGroupService physicalResourceGroupServiceMock;

  @Mock
  private EmailSender emailSender;

  @SuppressWarnings("unused")
  @Mock
  private MessageSource messageSourceMock;

  private RichUserDetails user = new RichUserDetailsFactory().addUserGroup("urn:user-group").create();

  @Before
  public void login() {
    Security.setUserDetails(user);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void findAllGroups() {
    ModelStub model = new ModelStub();

    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().create();

    when(physicalResourceGroupServiceMock.findAllWithPorts()).thenReturn(Lists.newArrayList(group));

    subject.selectInstitute("", model);

    assertThat(model.asMap(), hasKey("physicalResourceGroups"));
    assertThat(((Collection<PhysicalResourceGroup>) model.asMap().get("physicalResourceGroups")), contains(group));
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

    verify(emailSender, never()).sendVirtualPortRequestMail(any(RichUserDetails.class),
        any(PhysicalResourceGroup.class), any(UserGroup.class), anyInt(), anyString());
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

    verify(emailSender, never()).sendVirtualPortRequestMail(any(RichUserDetails.class),
        any(PhysicalResourceGroup.class), any(UserGroup.class), anyInt(), anyString());
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

    verify(emailSender, never()).sendVirtualPortRequestMail(any(RichUserDetails.class),
        any(PhysicalResourceGroup.class), any(UserGroup.class), anyInt(), anyString());
  }

  @Test
  public void requestShouldSentAnEmail() {
    ModelStub model = new ModelStub();
    PhysicalResourceGroup pGroup = new PhysicalResourceGroupFactory().setActive(true).create();
    RequestCommand command = new RequestCommand(pGroup);
    command.setPhysicalResourceGroupId(2L);
    command.setUserGroupId("urn:user-group");
    command.setBandwidth(1000);
    command.setMessage("message");

    when(physicalResourceGroupServiceMock.find(2L)).thenReturn(pGroup);

    String page = subject.request(command, new BeanPropertyBindingResult(command, "command"), model, model);

    assertThat(page, is("redirect:/"));

    verify(emailSender).sendVirtualPortRequestMail(user, pGroup, Iterables.getOnlyElement(user.getUserGroups()), 1000, "message");
  }

}
