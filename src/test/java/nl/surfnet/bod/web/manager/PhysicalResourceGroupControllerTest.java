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
package nl.surfnet.bod.web.manager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.web.base.MessageManager;
import nl.surfnet.bod.web.manager.PhysicalResourceGroupController.UpdateEmailCommand;
import nl.surfnet.bod.web.security.Security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RunWith(MockitoJUnitRunner.class)
public class PhysicalResourceGroupControllerTest {

  @InjectMocks
  private PhysicalResourceGroupController subject;

  @Mock
  private PhysicalResourceGroupService physicalResourceGroupServiceMock;

  @Mock
  private MessageManager messageManager;

  @Before
  public void loginUser() {
    Security.setUserDetails(new RichUserDetailsFactory().addUserGroup("urn:ict-manager").create());
  }

  @Test
  public void whenEmailHasChangedShouldCallService() {
    Model model = new ModelStub();
    RedirectAttributes requestAttributes = new ModelStub();

    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setId(1L).setManagerEmail("old@mail.com")
        .setAdminGroup("urn:ict-manager").create();

    UpdateEmailCommand command = new PhysicalResourceGroupController.UpdateEmailCommand(group);
    command.setManagerEmail("new@mail.com");

    when(physicalResourceGroupServiceMock.find(1L)).thenReturn(group);

    String page = subject.update(command, new BeanPropertyBindingResult(command, "updateEmailCommand"), model,
        requestAttributes);

    verify(messageManager).addInfoFlashMessage(any(RedirectAttributes.class), eq("info_activation_request_resend"),
        (String[]) anyVararg());
    assertThat((PhysicalResourceGroup) requestAttributes.getFlashAttributes().get("prg"), is(group));
    assertThat(page, is("redirect:/manager"));
    assertThat(group.getManagerEmail(), is(command.getManagerEmail()));
    verify(physicalResourceGroupServiceMock).sendActivationRequest(group);
  }

  @Test
  public void whenUserIsNotAnIctManagerShouldNotUpdate() {
    Model model = new ModelStub();
    RedirectAttributes redirectAttributes = new ModelStub();
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setId(1L).setManagerEmail("old@mail.com")
        .setAdminGroup("urn:no-ict-manager").create();

    UpdateEmailCommand command = new PhysicalResourceGroupController.UpdateEmailCommand(group);
    command.setManagerEmail("new@mail.com");

    when(physicalResourceGroupServiceMock.find(1L)).thenReturn(group);

    String page = subject.update(command, new BeanPropertyBindingResult(command, "updateEmailCommand"), model,
        redirectAttributes);

    assertThat(page, is("redirect:manager/index"));

    verify(physicalResourceGroupServiceMock, never()).sendActivationRequest(group);
  }

  @Test
  public void whenEmailDidNotChangeShouldNotUpdate() {
    Model model = new ModelStub();
    RedirectAttributes redirectAttributes = new ModelStub();

    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setId(1L).setManagerEmail("old@mail.com")
        .setAdminGroup("urn:ict-manager").create();

    UpdateEmailCommand command = new PhysicalResourceGroupController.UpdateEmailCommand(group);

    when(physicalResourceGroupServiceMock.find(1L)).thenReturn(group);

    String page = subject.update(command, new BeanPropertyBindingResult(command, "updateEmailCommand"), model,
        redirectAttributes);

    assertThat(page, is("redirect:/manager"));

    verify(physicalResourceGroupServiceMock, never()).sendActivationRequest(group);
  }

  @Test
  public void whenGroupNotFoundDontCrashOrUpdate() {
    Model model = new ModelStub();
    RedirectAttributes redirectAttributes = new ModelStub();

    UpdateEmailCommand command = new PhysicalResourceGroupController.UpdateEmailCommand();
    command.setId(1L);

    when(physicalResourceGroupServiceMock.find(1L)).thenReturn(null);

    String page = subject.update(command, new BeanPropertyBindingResult(command, "updateEmailCommand"), model,
        redirectAttributes);

    assertThat(page, is("redirect:manager/index"));

    verify(physicalResourceGroupServiceMock, never()).sendActivationRequest(any(PhysicalResourceGroup.class));
  }

  @SuppressWarnings("serial")
  @Test
  public void whenGroupHasErrors() {
    Model model = new ModelStub();
    RedirectAttributes redirectAttributes = new ModelStub();

    UpdateEmailCommand command = new PhysicalResourceGroupController.UpdateEmailCommand();
    command.setId(1L);
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setId(1L).setAdminGroup("urn:ict-manager")
        .create();

    when(physicalResourceGroupServiceMock.find(1L)).thenReturn(group);

    BeanPropertyBindingResult result = new BeanPropertyBindingResult(command, "updateEmailCommand") {
      @Override
      public boolean hasErrors() {
        return true;
      }
    };

    String page = subject.update(command, result, model, redirectAttributes);

    assertThat(page, is("manager/physicalresourcegroups/update"));

    assertThat(model.asMap(), hasKey("physicalResourceGroup"));
    verify(physicalResourceGroupServiceMock, never()).sendActivationRequest(any(PhysicalResourceGroup.class));
  }

}
