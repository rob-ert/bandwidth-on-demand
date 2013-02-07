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
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import nl.surfnet.bod.domain.*;
import nl.surfnet.bod.domain.VirtualPortRequestLink.RequestStatus;
import nl.surfnet.bod.domain.validator.VirtualPortValidator;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.support.*;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.base.MessageManager;
import nl.surfnet.bod.web.base.MessageRetriever;
import nl.surfnet.bod.web.base.MessageView;
import nl.surfnet.bod.web.manager.VirtualPortController.VirtualPortCreateCommand;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.security.Security.RoleEnum;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Sort;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class VirtualPortControllerTest {

  private static final String ALLOWED_ADMIN_GROUP = "urn:manager-group";

  @InjectMocks
  private VirtualPortController subject;

  @Mock
  private VirtualPortService virtualPortServiceMock;
  @Mock
  private VirtualPortValidator virtualPortValidatorMock;
  @Mock
  private PhysicalPortService physicalPortServiceMock;
  @Mock
  private PhysicalResourceGroupService physicalResourceGroupServiceMock;
  @Mock
  private VirtualResourceGroupService virtualResourceGroupServiceMock;
  @Mock
  private MessageRetriever messageRetrieverMock;

  private RichUserDetails user;
  private PhysicalPort physicalPort;
  private PhysicalResourceGroup prg;

  private MockMvc mockMvc;

  @Before
  public void setup() {
    FormattingConversionService conversionService = new FormattingConversionService();
    conversionService.addConverter(new Converter<String, PhysicalPort>() {
      @Override
      public PhysicalPort convert(String id) {
        return physicalPortServiceMock.find(Long.valueOf(id));
      }
    });
    conversionService.addConverter(new Converter<String, PhysicalResourceGroup>() {
      @Override
      public PhysicalResourceGroup convert(String id) {
        return physicalResourceGroupServiceMock.find(Long.valueOf(id));
      }
    });
    conversionService.addConverter(new Converter<String, VirtualResourceGroup>() {
      @Override
      public VirtualResourceGroup convert(String id) {
        return virtualResourceGroupServiceMock.find(Long.valueOf(id));
      }
    });

    mockMvc = standaloneSetup(subject).setConversionService(conversionService).build();
    subject.setMessageManager(new MessageManager(messageRetrieverMock));

    physicalPort = new PhysicalPortFactory().create();
    prg = new PhysicalResourceGroupFactory().setAdminGroup(ALLOWED_ADMIN_GROUP).addPhysicalPort(physicalPort).create();
    user = new RichUserDetailsFactory().addManagerRole(prg).addUserRole().addUserGroup(ALLOWED_ADMIN_GROUP).create();
    Security.setUserDetails(user);
  }

  @Test
  public void listShouldFindEntries() throws Exception {
    VirtualPort vp = new VirtualPortFactory().create();
    BodRole managerRole = Iterables.getOnlyElement(user.getManagerRoles());

    when(virtualPortServiceMock
      .findEntriesForManager(eq(managerRole), eq(0), eq(WebUtils.MAX_ITEMS_PER_PAGE), any(Sort.class)))
      .thenReturn(Lists.newArrayList(vp));

    mockMvc.perform(get("/manager/virtualports"))
      .andExpect(status().is(200))
      .andExpect(model().attribute("list", hasSize(1)))
      .andExpect(model().attribute(WebUtils.MAX_PAGES_KEY, 1));
  }

  @Test
  public void shouldUpdatePort() throws Exception {
    VirtualPort port = new VirtualPortFactory().setPhysicalPortAdminGroup(ALLOWED_ADMIN_GROUP).create();

    when(virtualPortServiceMock.find(3L)).thenReturn(port);
    when(physicalPortServiceMock.find(2L)).thenReturn(new PhysicalPortFactory().create());
    when(physicalResourceGroupServiceMock.find(2L)).thenReturn(new PhysicalResourceGroupFactory().create());
    when(virtualResourceGroupServiceMock.find(2L)).thenReturn(new VirtualResourceGroupFactory().create());
    when(messageRetrieverMock.getMessageWithBoldArguments("info_virtualport_updated", "newLabel")).thenReturn("correctMessage");

    mockMvc.perform(put("/manager/virtualports/")
        .param("id", "3")
        .param("version", "0")
        .param("managerLabel", "newLabel")
        .param("maxBandwidth", "1000")
        .param("vlanId", "22")
        .param("physicalPort", "2")
        .param("virtualResourceGroup", "2")
        .param("physicalResourceGroup", "2"))
      .andExpect(status().is(302))
      .andExpect(flash().attribute("infoMessages", hasItem("correctMessage")))
      .andExpect(view().name("redirect:/manager/virtualports"));

    verify(virtualPortServiceMock).update(port);
  }

  @Test
  public void shouldNotUpdateNonExistingPort() throws Exception {
    when(virtualPortServiceMock.find(3L)).thenReturn(null);

    mockMvc.perform(put("/manager/virtualports/")
        .param("id", "3")
        .param("version", "0")
        .param("managerLabel", "newLabel")
        .param("maxBandwidth", "1000")
        .param("vlanId", "22")
        .param("physicalPort", "2")
        .param("virtualResourceGroup", "2")
        .param("physicalResourceGroup", "2"))
      .andExpect(status().is(302))
      .andExpect(flash().attribute("infoMessages", nullValue()))
      .andExpect(view().name("redirect:/manager/virtualports"));

    verify(virtualPortServiceMock, never()).update(any(VirtualPort.class));
  }

  @Test
  public void shouldNotUpdatePortBecauseNotAllowed() throws Exception {
    VirtualPort port = new VirtualPortFactory().setPhysicalPortAdminGroup("urn:wrong-group").create();

    when(virtualPortServiceMock.find(3L)).thenReturn(port);
    when(physicalPortServiceMock.find(2L)).thenReturn(new PhysicalPortFactory().create());
    when(physicalResourceGroupServiceMock.find(2L)).thenReturn(new PhysicalResourceGroupFactory().create());
    when(virtualResourceGroupServiceMock.find(2L)).thenReturn(new VirtualResourceGroupFactory().create());

    mockMvc.perform(put("/manager/virtualports/")
        .param("id", "3")
        .param("version", "0")
        .param("managerLabel", "newLabel")
        .param("maxBandwidth", "1000")
        .param("vlanId", "22")
        .param("physicalPort", "2")
        .param("virtualResourceGroup", "2")
        .param("physicalResourceGroup", "2"))
      .andExpect(status().is(302))
      .andExpect(flash().attribute("infoMessages", nullValue()))
      .andExpect(view().name("redirect:/manager/virtualports"));

    verify(virtualPortServiceMock, never()).update(port);
  }

  @Test
  public void shouldNotUpdateWhenCommandHasErrors() throws Exception {
    VirtualPort port = new VirtualPortFactory().setPhysicalPortAdminGroup(ALLOWED_ADMIN_GROUP).create();

    when(virtualPortServiceMock.find(3L)).thenReturn(port);
    when(physicalPortServiceMock.find(2L)).thenReturn(new PhysicalPortFactory().create());
    when(physicalResourceGroupServiceMock.find(2L)).thenReturn(new PhysicalResourceGroupFactory().create());
    when(virtualResourceGroupServiceMock.find(2L)).thenReturn(new VirtualResourceGroupFactory().create());

    mockMvc.perform(put("/manager/virtualports/")
        .param("id", "3")
        .param("version", "0")
        .param("managerLabel", "newLabel")
        .param("maxBandwidth", "-1")
        .param("vlanId", "22")
        .param("physicalPort", "2")
        .param("virtualResourceGroup", "2")
        .param("physicalResourceGroup", "2"))
      .andExpect(status().is(200))
      .andExpect(model().attributeExists("virtualPortUpdateCommand", "physicalPorts", "physicalResourceGroups", "virtualResourceGroups"))
      .andExpect(view().name("/manager/virtualports/update"));

    verify(virtualPortServiceMock, never()).update(port);
  }

  @Test
  public void updateFormShouldShowVirtualPort() throws Exception {
    VirtualPort virtualPort = new VirtualPortFactory()
      .setId(15L)
      .setPhysicalPortAdminGroup(ALLOWED_ADMIN_GROUP).create();

    when(virtualPortServiceMock.find(15L)).thenReturn(virtualPort);

    mockMvc.perform(get("/manager/virtualports/edit").param("id", "15"))
      .andExpect(status().isOk())
      .andExpect(model().attribute("virtualPortUpdateCommand", hasProperty("id", is(15L))))
      .andExpect(model().attributeExists("physicalPorts", "virtualResourceGroups", "virtualResourceGroups", "physicalResourceGroups"));
  }

  @Test
  public void updateFormShouldRedirectWhenNonExistingVirtualPort() throws Exception {
    when(virtualPortServiceMock.find(15L)).thenReturn(null);

    mockMvc.perform(get("/manager/virtualports/edit").param("id", "15"))
      .andExpect(status().isMovedTemporarily())
      .andExpect(view().name("redirect:/manager/virtualports"));
  }

  @Test
  public void updateFormShouldRedirectWhenUserHasNoPermission() throws Exception {
    VirtualPort virtualPort = new VirtualPortFactory()
      .setPhysicalPortAdminGroup("urn:wrong-group").create();

    when(virtualPortServiceMock.find(15L)).thenReturn(virtualPort);

    mockMvc.perform(get("/manager/virtualports/edit").param("id", "15"))
      .andExpect(status().isMovedTemporarily())
      .andExpect(view().name("redirect:/manager/virtualports"));
  }

  @Test
  public void createWithIllegalPhysicalResourceGroupShouldRedirect() throws Exception {
    PhysicalResourceGroup wrongPrg = new PhysicalResourceGroupFactory()
      .setAdminGroup("urn:manager-group-wrong")
      .create();
    VirtualPortRequestLink link = new VirtualPortRequestLinkFactory().setPhysicalResourceGroup(wrongPrg).create();

    when(virtualPortServiceMock.findRequest("123-abc-456-qwerty")).thenReturn(link);

    mockMvc.perform(get("/manager/virtualports/create/123-abc-456-qwerty"))
      .andExpect(status().isMovedTemporarily())
      .andExpect(view().name("redirect:/"));
  }

  @Test
  public void createWithNonExistinLinkShouldRedirect() throws Exception {
    when(virtualPortServiceMock.findRequest("123-abc-456-qwerty")).thenReturn(null);

    mockMvc.perform(get("/manager/virtualports/create/123-abc-456-qwerty"))
      .andExpect(status().isMovedTemporarily())
      .andExpect(view().name("redirect:/"));
  }

  @Test
  public void createShouldAddCreateCommandToModel() throws Exception {
    VirtualPortRequestLink link = new VirtualPortRequestLinkFactory()
      .setPhysicalResourceGroup(prg)
      .setMinBandwidth(1024).create();

    when(virtualPortServiceMock.findRequest("123-abc-456-qwerty")).thenReturn(link);

    mockMvc.perform(get("/manager/virtualports/create/123-abc-456-qwerty"))
      .andExpect(status().isOk())
      .andExpect(view().name("/manager/virtualports/create"))
      .andExpect(model().attribute("virtualPortCreateCommand", allOf(
          hasProperty("physicalPort", is(physicalPort)),
          hasProperty("physicalResourceGroup", is(prg)),
          hasProperty("virtualResourceGroup", is(link.getVirtualResourceGroup())),
          hasProperty("maxBandwidth", is(link.getMinBandwidth())),
          hasProperty("virtualPortRequestLink", is(link)))));
  }

  @Test
  public void createShouldNotBeAllowdIfLinkIsAlreadyUsed() throws Exception {
    VirtualPortRequestLink link = new VirtualPortRequestLinkFactory()
      .setStatus(RequestStatus.APPROVED)
      .setPhysicalResourceGroup(prg).create();

    when(virtualPortServiceMock.findRequest("123-abc-456-qwerty")).thenReturn(link);

    mockMvc.perform(get("/manager/virtualports/create/123-abc-456-qwerty"))
      .andExpect(status().isOk())
      .andExpect(view().name("message"))
      .andExpect(model().attributeExists(MessageView.MODEL_KEY));
  }

  @Test
  public void createShouldSwitchToRelatedManagerRole() {
    ModelStub model = new ModelStub();
    VirtualPortRequestLink link = new VirtualPortRequestLinkFactory().setPhysicalResourceGroup(prg).create();

    when(virtualPortServiceMock.findRequest("1234567890")).thenReturn(link);

    Security.switchToUser();

    subject.createForm("1234567890", model, model);

    assertThat(user.getSelectedRole().getRole(), is(RoleEnum.ICT_MANAGER));
  }

  @Test
  public void whenAPortIsCreatedLinkShouldChangeStatus() {
    ModelStub model = new ModelStub();
    VirtualPortRequestLink link = new VirtualPortRequestLinkFactory().setPhysicalResourceGroup(prg).create();
    VirtualPortCreateCommand command = new VirtualPortCreateCommand(link);
    BindingResult result = new BeanPropertyBindingResult(command, "createVirtualPortCommand");

    subject.create(command, result, model, model);

    verify(virtualPortServiceMock).save(any(VirtualPort.class));
    verify(virtualPortServiceMock).requestLinkApproved(eq(link), any(VirtualPort.class));
  }

  @Test
  public void whenLinkIsApprovedDeclineMessageMayBeEmpty() {
    ModelStub model = new ModelStub();
    VirtualPortRequestLink link = new VirtualPortRequestLinkFactory().setPhysicalResourceGroup(prg).create();
    VirtualPortCreateCommand command = new VirtualPortCreateCommand(link);
    BindingResult result = mock(BindingResult.class);

    when(result.hasErrors()).thenReturn(true);
    when(result.getErrorCount()).thenReturn(1);
    when(result.hasFieldErrors("declineMessage")).thenReturn(true);

    subject.create(command, result, model, model);

    verify(virtualPortServiceMock).save(any(VirtualPort.class));
    verify(virtualPortServiceMock).requestLinkApproved(eq(link), any(VirtualPort.class));
  }

  @Test
  public void requestCanBeDeclined() {
    ModelStub model = new ModelStub();
    VirtualPortRequestLink link = new VirtualPortRequestLinkFactory().setPhysicalResourceGroup(prg).create();
    VirtualPortCreateCommand command = new VirtualPortCreateCommand(link);
    command.setAcceptOrDecline("decline");
    command.setDeclineMessage("Declined!");
    BindingResult result = new BeanPropertyBindingResult(command, "createVirtualPortCommand");

    String page = subject.create(command, result, model, model);

    assertThat(page, is("redirect:/manager/virtualports"));

    verify(virtualPortServiceMock).requestLinkDeclined(link, "Declined!");
  }

  @Test
  public void deleteNonExistingVirtualPort() throws Exception {
    mockMvc.perform(delete("/manager/virtualports/delete").param("id", "3"))
      .andExpect(status().is(302))
      .andExpect(flash().attribute("infoMessages", nullValue()));

    verify(virtualPortServiceMock, never()).delete(any(VirtualPort.class), any(RichUserDetails.class));
  }

  @Test
  public void deleteVirtualPort() throws Exception {
    VirtualPort vp = new VirtualPortFactory().setPhysicalPortAdminGroup(ALLOWED_ADMIN_GROUP).create();

    when(virtualPortServiceMock.find(3L)).thenReturn(vp);
    when(messageRetrieverMock.getMessageWithBoldArguments("info_virtualport_deleted", vp.getManagerLabel())).thenReturn("correctMessage");

    mockMvc.perform(delete("/manager/virtualports/delete").param("id", "3"))
      .andExpect(status().is(302))
      .andExpect(flash().attribute("infoMessages", hasItem("correctMessage")));

    verify(virtualPortServiceMock).delete(vp, user);
  }

}