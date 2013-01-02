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
package nl.surfnet.bod.web;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.web.manager.ActivationEmailController;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

@RunWith(MockitoJUnitRunner.class)
public class SwitchRoleControllerTest {

  @Mock
  private Environment environmentMock;

  @Mock
  private HttpServletRequest mockRequest;

  @Mock
  private HttpSession mockSession;

  @Mock
  private MessageSource messageSourceMock;

  @Mock
  private PhysicalResourceGroupService physicalResourceGroupService;

  @InjectMocks
  private SwitchRoleController subject = new SwitchRoleController();

  private RichUserDetails user;

  @Before
  public void setUp() {
    user = new RichUserDetailsFactory().create();
    Security.setUserDetails(user);
  }

  @Test
  public void testSwitchRoleWithActivePrg() {
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setId(101L).setActive(true).create();
    BodRole role = BodRole.createManager(group);

    user = new RichUserDetailsFactory().addBodRoles(role).create();
    Security.setUserDetails(user);

    when(physicalResourceGroupService.find(group.getId())).thenReturn(group);

    Model uiModel = new ModelStub();
    Model redirectAttribs = new ModelStub();
    String view = subject.switchRole(String.valueOf(role.getId()), uiModel, (RedirectAttributes) redirectAttribs);

    assertThat(view, is(role.getRole().getViewName()));
  }

  @Test
  public void testSwitchRoleWithNotActivePrg() {
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setId(101L).create();
    BodRole role = BodRole.createManager(group);

    user = new RichUserDetailsFactory().addBodRoles(role).create();
    Security.setUserDetails(user);

    when(physicalResourceGroupService.find(group.getId())).thenReturn(group);

    Model uiModel = new ModelStub();
    Model redirectAttribs = new ModelStub();
    String view = subject.switchRole(String.valueOf(role.getId()), uiModel, (RedirectAttributes) redirectAttribs);

    assertThat(view, is("redirect:manager/physicalresourcegroups/edit?id=" + group.getId()));
  }

  @Test
  public void testLogout() {
    when(mockRequest.getSession()).thenReturn(mockSession);
    when(environmentMock.getShibbolethLogoutUrl()).thenReturn("shibUrl");

    String view = subject.logout(mockRequest);
    assertThat(view, is("redirect:" + environmentMock.getShibbolethLogoutUrl()));
  }

  @Test
  public void shouldCreateNewLinkForm() {
    PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().create();

    String linkForm = subject.createNewActivationLinkForm(new Object[] {
        environmentMock.getExternalBodUrl() + ActivationEmailController.ACTIVATION_MANAGER_PATH,
        physicalResourceGroup.getId().toString(), "Yes new email was sent" });

    assertThat(linkForm, containsString(physicalResourceGroup.getId().toString()));
    assertThat(linkForm, containsString(ActivationEmailController.ACTIVATION_MANAGER_PATH));
  }

}
