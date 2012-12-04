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
package nl.surfnet.bod.web.manager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;

import nl.surfnet.bod.domain.ActivationEmailLink;
import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.InstituteService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.support.ActivationEmailLinkFactory;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

@RunWith(MockitoJUnitRunner.class)
public class ActivationEmailControllerTest {

  @InjectMocks
  private ActivationEmailController subject;

  @Mock
  private PhysicalResourceGroupService physicalResourceGroupServiceMock;
  @Mock
  private ActivationEmailLink linkMock;
  @Mock
  private InstituteService instituteServiceMock;
  @Mock
  private MessageSource messageSourceMock;

  private ActivationEmailLink link;
  private PhysicalResourceGroup physicalResourceGroup;

  @Before
  public void setUp() {
    physicalResourceGroup = new PhysicalResourceGroupFactory().create();
    link = new ActivationEmailLinkFactory().setPhysicalResourceGroup(physicalResourceGroup)
        .create();
    BodRole managerRole = BodRole.createManager(link.getSourceObject());

    Security.setUserDetails(new RichUserDetailsFactory().addUserGroup(physicalResourceGroup.getAdminGroup())
        .addBodRoles(managerRole).create());
  }

  @Test
  public void physicalResourceGroupShouldBeActivated() {
    when(physicalResourceGroupServiceMock.findActivationLink("1234567890")).thenReturn(link);

    ModelStub model = new ModelStub();

    String page = subject.activateEmail("1234567890", model, model);

    assertThat(page, is("manager/emailConfirmed"));
    assertThat(model.asMap(), hasEntry("physicalResourceGroup", Object.class.cast(link.getSourceObject())));

    verify(physicalResourceGroupServiceMock, times(1)).activate(any((ActivationEmailLink.class)));
  }

  @Test
  public void shouldSwitchToCorrectManagerRole() {
    BodRole managerRole = BodRole.createManager(link.getSourceObject());
    BodRole nocRole = BodRole.createNocEngineer();

    RichUserDetails user = new RichUserDetailsFactory().addUserGroup(link.getSourceObject().getAdminGroup())
        .addBodRoles(managerRole, nocRole).create();
    user.trySwitchToNoc();
    Security.setUserDetails(user);

    when(physicalResourceGroupServiceMock.findActivationLink("1234567890")).thenReturn(link);

    ModelStub model = new ModelStub();
    subject.activateEmail("1234567890", model, model);

    assertThat(user.getSelectedRole(), is(managerRole));
  }

  @Test
  public void activationLinkIsNotValidAnymore() {
    when(linkMock.isValid()).thenReturn(false);
    when(linkMock.getToEmail()).thenReturn(physicalResourceGroup.getManagerEmail());
    when(linkMock.getSourceObject()).thenReturn(physicalResourceGroup);

    when(physicalResourceGroupServiceMock.findActivationLink("1234567890")).thenReturn(linkMock);

    ModelStub model = new ModelStub();
    String page = subject.activateEmail("1234567890", model, model);

    verify(physicalResourceGroupServiceMock, times(0)).activate(any((ActivationEmailLink.class)));
    assertThat(page, is("manager/linkNotValid"));
  }

  @Test
  public void activationLinkIsNotValid() {
    when(physicalResourceGroupServiceMock.findActivationLink("1234567890")).thenReturn(null);

    ModelStub model = new ModelStub();
    String page = subject.activateEmail("1234567890", model, model);

    verify(physicalResourceGroupServiceMock, times(0)).activate(any((ActivationEmailLink.class)));

    assertThat(page, is("redirect:/"));
  }

  @Test
  public void sourceObjectHasBeenDeleted() {
    ActivationEmailLink link = new ActivationEmailLink();

    when(physicalResourceGroupServiceMock.findActivationLink("123")).thenReturn(link);

    ModelStub model = new ModelStub();
    String page = subject.activateEmail("123", model, model);

    assertThat(page, is("redirect:/"));
  }

  @Test
  public void activationLinkIsAlreadyActivated() {
    when(linkMock.isActivated()).thenReturn(true);
    when(physicalResourceGroupServiceMock.findActivationLink("1234567890")).thenReturn(linkMock);
    when(linkMock.getSourceObject()).thenReturn(link.getSourceObject());

    ModelStub model = new ModelStub();
    String page = subject.activateEmail("1234567890", model, model);
    verify(physicalResourceGroupServiceMock, times(0)).activate(any((ActivationEmailLink.class)));
    assertThat(page, is("manager/linkActive"));
  }

  @Test
  public void shouldRequestNewLink() {
    PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().create();

    when(physicalResourceGroupServiceMock.find(anyLong())).thenReturn(physicalResourceGroup);
    when(linkMock.getToEmail()).thenReturn(physicalResourceGroup.getManagerEmail());
    when(linkMock.getSourceObject()).thenReturn(physicalResourceGroup);
    when(physicalResourceGroupServiceMock.sendActivationRequest(physicalResourceGroup)).thenReturn(linkMock);

    subject.create(physicalResourceGroup);

    verify(physicalResourceGroupServiceMock).sendActivationRequest(physicalResourceGroup);
  }

  @Test
  public void emailInLinkDiffersFromPhysicalResourceGroup() {
    physicalResourceGroup.setManagerEmail("manager@surfnet.nl");

    when(physicalResourceGroupServiceMock.findActivationLink("1234567890")).thenReturn(linkMock);
    when(linkMock.getToEmail()).thenReturn("link@surfnet.nl");
    when(linkMock.getSourceObject()).thenReturn(physicalResourceGroup);

    ModelStub model = new ModelStub();
    String page = subject.activateEmail("1234567890", model, model);

    verify(physicalResourceGroupServiceMock, times(0)).activate(any((ActivationEmailLink.class)));

    assertThat(page, is("manager/linkChanged"));
  }

  @Test
  public void managerHasNoRightToAccessPhysicalResourceGroup() {
    PhysicalResourceGroup physicalResourceGroupMock = mock(PhysicalResourceGroup.class);
    when(physicalResourceGroupMock.getAdminGroup()).thenReturn("wrong:group");
    when(physicalResourceGroupMock.getName()).thenReturn("prgOne");
    when(linkMock.getSourceObject()).thenReturn(physicalResourceGroupMock);

    when(messageSourceMock.getMessage(eq("info_activation_request_notallowed"), any(Object[].class), any(Locale.class)))
        .thenReturn("not allowed");

    when(physicalResourceGroupServiceMock.findActivationLink("1234567890")).thenReturn(linkMock);

    ModelStub model = new ModelStub();
    String page = subject.activateEmail("1234567890", model, model);

    verify(physicalResourceGroupServiceMock, times(0)).activate(any((ActivationEmailLink.class)));
    assertThat(page, is("redirect:/"));
  }

}
