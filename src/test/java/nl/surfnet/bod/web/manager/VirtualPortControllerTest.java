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

import java.util.Collection;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualPortRequestLink;
import nl.surfnet.bod.domain.validator.VirtualPortValidator;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.support.*;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.base.MessageManager;
import nl.surfnet.bod.web.manager.VirtualPortController.VirtualPortCreateCommand;
import nl.surfnet.bod.web.manager.VirtualPortController.VirtualPortUpdateCommand;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.security.Security.RoleEnum;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Sort;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class VirtualPortControllerTest {

  @InjectMocks
  private VirtualPortController subject;

  @Mock
  private VirtualPortService virtualPortServiceMock;

  @Mock
  private VirtualPortValidator virtualPortValidatorMock;

  @Mock
  private MessageManager messageManager;

  private RichUserDetails user;
  private PhysicalPort physicalPort;
  private PhysicalResourceGroup prg;

  @Before
  public void login() {
    physicalPort = new PhysicalPortFactory().create();
    prg = new PhysicalResourceGroupFactory().setAdminGroup("urn:manager-group").addPhysicalPort(physicalPort).create();

    user = new RichUserDetailsFactory().addManagerRole(prg).addUserRole().addUserGroup("urn:manager-group").create();
    Security.setUserDetails(user);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void listShouldFindEntries() {
    ModelStub model = new ModelStub();

    when(
        virtualPortServiceMock.findEntriesForManager(eq(Iterables.getOnlyElement(user.getManagerRoles())), eq(0),
            eq(WebUtils.MAX_ITEMS_PER_PAGE), any(Sort.class))).thenReturn(
        Lists.newArrayList(new VirtualPortFactory().create()));

    subject.list(1, null, null, model);

    assertThat(model.asMap(), hasKey("list"));
    assertThat(model.asMap(), hasKey(WebUtils.MAX_PAGES_KEY));

    assertThat((Collection<VirtualPort>) model.asMap().get("list"), hasSize(1));
  }

  @Test
  public void shouldUpdatePort() {
    ModelStub model = new ModelStub();
    VirtualPort port = new VirtualPortFactory().setPhysicalPortAdminGroup("urn:manager-group").create();
    VirtualPortUpdateCommand command = new VirtualPortUpdateCommand(port);

    when(virtualPortServiceMock.find(port.getId())).thenReturn(port);

    String page = subject.update(command, new BeanPropertyBindingResult(port, "port"), model, model);

    assertThat(page, is("redirect:/manager/virtualports"));

    verify(messageManager).addInfoFlashMessage(any(RedirectAttributes.class), eq("info_virtualport_updated"),
        eq(port.getManagerLabel()));
    verify(virtualPortServiceMock).update(port);
  }

  @Test
  public void shouldNotUpdatePortBecauseNotAllowed() {
    ModelStub model = new ModelStub();
    VirtualPort port = new VirtualPortFactory().setPhysicalPortAdminGroup("urn:wrong-group").create();
    VirtualPortUpdateCommand command = new VirtualPortUpdateCommand(port);

    when(virtualPortServiceMock.find(port.getId())).thenReturn(port);

    String page = subject.update(command, new BeanPropertyBindingResult(port, "port"), model, model);

    assertThat(page, is("redirect:/manager/virtualports"));
    assertThat(model.getFlashAttributes(), not(hasKey("infoMessages")));

    verify(virtualPortServiceMock, never()).update(port);
  }

  @Test
  public void createWithIllegalPhysicalResourceGroupShouldRedirect() {
    ModelStub model = new ModelStub();
    PhysicalResourceGroup wrongPrg = new PhysicalResourceGroupFactory().setAdminGroup("urn:manager-group-wrong")
        .create();
    VirtualPortRequestLink link = new VirtualPortRequestLinkFactory().setPhysicalResourceGroup(wrongPrg).create();

    when(virtualPortServiceMock.findRequest("1234567890")).thenReturn(link);

    String page = subject.createForm("1234567890", model, model);

    assertThat(page, is("redirect:/"));
  }

  @Test
  public void createShouldAddCreateCommandToModel() {
    ModelStub model = new ModelStub();
    VirtualPortRequestLink link = new VirtualPortRequestLinkFactory().setPhysicalResourceGroup(prg).create();

    when(virtualPortServiceMock.findRequest("1234567890")).thenReturn(link);

    subject.createForm("1234567890", model, model);

    VirtualPortCreateCommand command = (VirtualPortCreateCommand) model.asMap().get("virtualPortCreateCommand");
    assertThat(command.getVirtualResourceGroup(), is(link.getVirtualResourceGroup()));
    assertThat(command.getPhysicalPort(), is(physicalPort));
    assertThat(command.getPhysicalResourceGroup(), is(prg));
    assertThat(command.getMaxBandwidth(), is(link.getMinBandwidth()));
    assertThat(command.getVirtualPortRequestLink(), is(link));
    assertThat(command.getPhysicalResourceGroup().getInstitute(), notNullValue());
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

}
