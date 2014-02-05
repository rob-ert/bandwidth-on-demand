/**
 * Copyright (c) 2012, 2013 SURFnet BV
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Lists;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.support.InstituteFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.UserGroupFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;
import nl.surfnet.bod.web.base.MessageManager;
import nl.surfnet.bod.web.base.MessageRetriever;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(MockitoJUnitRunner.class)
public class VirtualPortRequestControllerTest {

  @InjectMocks private VirtualPortRequestController subject;

  @Mock private PhysicalResourceGroupService physicalResourceGroupServiceMock;
  @Mock private VirtualPortService virtualPortServiceMock;
  @Mock private VirtualResourceGroupService virtualResourceGroupServiceMock;
  @Mock private MessageManager messageManager;
  @Mock private MessageRetriever messageRetriever;

  private RichUserDetails user;

  private MockMvc mockMvc;

  @Before
  public void setupAndLogin() {
    mockMvc = standaloneSetup(subject).build();

    UserGroup group1 = new UserGroupFactory().setName("A").setId("urn:user-group").create();
    UserGroup group2 = new UserGroupFactory().setName("B").create();
    UserGroup group3 = new UserGroupFactory().setName("C").create();
    user = new RichUserDetailsFactory().addUserRole().addUserGroup(group3).addUserGroup(group1).addUserGroup(group2).create();
    Security.setUserDetails(user);
  }

  @Test
  public void find_all_groups_should_be_sorted() throws Exception {
    PhysicalResourceGroup group1 = new PhysicalResourceGroupFactory().setInstitute(
        new InstituteFactory().setName("A").create()).create();
    PhysicalResourceGroup group2 = new PhysicalResourceGroupFactory().setInstitute(
        new InstituteFactory().setName("B").create()).create();
    PhysicalResourceGroup group3 = new PhysicalResourceGroupFactory().setInstitute(
        new InstituteFactory().setName("C").create()).create();

    when(physicalResourceGroupServiceMock.findAllWithPorts()).thenReturn(Lists.newArrayList(group3, group1, group2));

    mockMvc.perform(
      get("/request")
        .param("teamLabel", "label")
        .param("teamUrn", "urn:group"))
      .andExpect(model().attribute("physicalResourceGroups", contains(group1, group2, group3)));
  }

  @Test
  public void should_not_create_form_if_physical_resource_group_does_not_exist() throws Exception {
    when(physicalResourceGroupServiceMock.find(1L)).thenReturn(null);

    mockMvc.perform(
      get("/request")
        .param("id", "1")
        .param("teamUrn", "urn:group"))
      .andExpect(view().name("redirect:/virtualports/request"));
  }

  @Test
  public void should_not_create_form_if_physical_resource_group_is_not_active() throws Exception {
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setActive(false).create();

    when(physicalResourceGroupServiceMock.find(1L)).thenReturn(group);

    mockMvc.perform(
      get("/request")
        .param("id", "1")
        .param("teamUrn", "urn:group"))
      .andExpect(view().name("redirect:/virtualports/request"));
  }

  @Test
  public void should_create_form_if_physical_resource_group_is_active() throws Exception {
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setActive(true).create();

    when(physicalResourceGroupServiceMock.find(1L)).thenReturn(group);

    mockMvc.perform(
      get("/request")
        .param("id", "1")
        .param("teamUrn", "urn:user-group"))
      .andExpect(view().name("virtualports/requestform"))
      .andExpect(model().attributeExists("requestCommand", "user", "physicalResourceGroup"));
  }

  @Test
  public void when_the_physical_group_has_changed_dont_mail_and_redirect() throws Exception {
    when(physicalResourceGroupServiceMock.find(2L)).thenReturn(null);

    mockMvc.perform(
      post("/request")
        .param("physicalResourceGroupId", "2")
        .param("userGroupId", "urn:user-group-wrong")
        .param("bandwidth", "1000")
        .param("userLabel", "new port")
        .param("message", "message"))
      .andExpect(view().name("redirect:/virtualports/request"));

    verifyNeverRequestNewVirtualPort();
  }

  @Test
  public void when_the_user_is_not_member_of_virtual_resource_group_dont_mail_and_redirect() throws Exception {
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setActive(true).create();

    when(physicalResourceGroupServiceMock.find(2L)).thenReturn(group);

    mockMvc.perform(
      post("/request")
        .param("physicalResourceGroupId", "2")
        .param("userGroupId", "urn:user-group-wrong")
        .param("bandwidth", "1000")
        .param("userLabel", "new port")
        .param("message", "message"))
      .andExpect(view().name("redirect:/virtualports/request"));

    verifyNeverRequestNewVirtualPort();
  }

  @Test
  public void when_the_physical_resource_group_is_not_active_dont_mail_and_redirect() throws Exception {
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setActive(false).create();

    when(physicalResourceGroupServiceMock.find(2L)).thenReturn(group);

    mockMvc.perform(
      post("/request")
        .param("physicalResourceGroupId", "2")
        .param("userGroupId", "urn:user-group")
        .param("bandwidth", "1000")
        .param("userLabel", "new port")
        .param("message", "message"))
      .andExpect(view().name("redirect:/virtualports/request"));

    verifyNeverRequestNewVirtualPort();
  }

  @Test
  public void request_should_sent_an_email() throws Exception {
    PhysicalResourceGroup pGroup = new PhysicalResourceGroupFactory().setActive(true).create();
    VirtualResourceGroup vGroup = new VirtualResourceGroupFactory().create();

    when(virtualResourceGroupServiceMock.findByAdminGroup("urn:user-group")).thenReturn(vGroup);
    when(physicalResourceGroupServiceMock.find(2L)).thenReturn(pGroup);

    mockMvc.perform(
      post("/request")
        .param("physicalResourceGroupId", "2")
        .param("userGroupId", "urn:user-group")
        .param("bandwidth", "1000")
        .param("userLabel", "new port")
        .param("message", "message"))
      .andExpect(view().name("redirect:/user"));

    verify(virtualPortServiceMock).requestCreateVirtualPort(user, vGroup, pGroup, "new port", 1000L, "message");
  }

  @Test
  public void when_virtual_resource_group_does_not_exists_it_should_be_created() throws Exception {
    PhysicalResourceGroup pGroup = new PhysicalResourceGroupFactory().setActive(true).create();

    when(virtualResourceGroupServiceMock.findByAdminGroup("urn:user-group")).thenReturn(null);
    when(physicalResourceGroupServiceMock.find(2L)).thenReturn(pGroup);

    mockMvc.perform(
      post("/request")
        .param("physicalResourceGroupId", "2")
        .param("userGroupId", "urn:user-group")
        .param("bandwidth", "1111")
        .param("userLabel", "new port")
        .param("message", "I want!"))
      .andExpect(view().name("redirect:/user"));

    verify(virtualResourceGroupServiceMock).save(any(VirtualResourceGroup.class));
    verify(virtualPortServiceMock).requestCreateVirtualPort(eq(user), any(VirtualResourceGroup.class), eq(pGroup),
        eq("new port"), eq(1111L), eq("I want!"));
  }

  @Test
  public void do_not_switch_role_when_user_has_selected_role() throws Exception {
    PhysicalResourceGroup pGroup = new PhysicalResourceGroupFactory().setActive(true).create();

    when(virtualResourceGroupServiceMock.findByAdminGroup("urn:user-group")).thenReturn(null);
    when(physicalResourceGroupServiceMock.find(2L)).thenReturn(pGroup);

    mockMvc.perform(
      post("/request")
        .param("physicalResourceGroupId", "2")
        .param("userGroupId", "urn:user-group")
        .param("bandwidth", "1111")
        .param("userLabel", "new port")
        .param("message", "I want!"))
      .andExpect(view().name("redirect:/user"));

    assertThat("Context should not be cleared", SecurityContextHolder.getContext().getAuthentication(), notNullValue());

    verify(virtualResourceGroupServiceMock).save(any(VirtualResourceGroup.class));
    verify(virtualPortServiceMock).requestCreateVirtualPort(eq(user), any(VirtualResourceGroup.class), eq(pGroup), eq("new port"), eq(1111L), eq("I want!"));
  }

  @Test
  public void do_switch_role_when_user_has_no_selected_role() throws Exception {
    user = new RichUserDetailsFactory()
      .addBodRoles(BodRole.createNewUser())
      .addUserGroup(new UserGroupFactory().setId("urn:user-group").create()).create();
    Security.setUserDetails(user);

    PhysicalResourceGroup pGroup = new PhysicalResourceGroupFactory().setActive(true).create();

    when(virtualResourceGroupServiceMock.findByAdminGroup("urn:user-group")).thenReturn(null);
    when(physicalResourceGroupServiceMock.find(2L)).thenReturn(pGroup);

    mockMvc.perform(
      post("/request")
        .param("physicalResourceGroupId", "2")
        .param("userGroupId", "urn:user-group")
        .param("bandwidth", "1111")
        .param("userLabel", "port")
        .param("message", "I want!"))
      .andExpect(view().name("redirect:/user"));

    assertThat("Context should not be cleared", SecurityContextHolder.getContext().getAuthentication(), nullValue());

    verify(virtualResourceGroupServiceMock).save(any(VirtualResourceGroup.class));
    verify(virtualPortServiceMock).requestCreateVirtualPort(eq(user), any(VirtualResourceGroup.class), eq(pGroup), eq("port"), eq(1111L), eq("I want!"));
  }

  @Test
  public void request_virtual_port_without_email_should_continue() throws Exception {
    RichUserDetails user = new RichUserDetailsFactory().setEmail("").create();
    Security.setUserDetails(user);

    mockMvc.perform(get("/request"))
      .andExpect(model().attributeExists("userGroupViews"))
      .andExpect(view().name("virtualports/selectTeam"));
  }

  private void verifyNeverRequestNewVirtualPort() {
    verify(virtualPortServiceMock, never()).requestCreateVirtualPort(any(RichUserDetails.class),
        any(VirtualResourceGroup.class), any(PhysicalResourceGroup.class), anyString(), anyLong(), anyString());
  }

}