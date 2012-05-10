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

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class ActivationEmailControllerTest {

  @InjectMocks
  private ActivationEmailController subject;

  @Mock
  private PhysicalResourceGroupService physicalResourceGroupServiceMock;
  @Mock
  private ActivationEmailLink<PhysicalResourceGroup> linkMock;
  @Mock
  private InstituteService instituteServiceMock;
  @Mock
  private MessageSource messageSourceMock;

  private ActivationEmailLink<PhysicalResourceGroup> link;
  private PhysicalResourceGroup physicalResourceGroup;

  @Before
  public void setUp() {
    physicalResourceGroup = new PhysicalResourceGroupFactory().create();
    link = new ActivationEmailLinkFactory<PhysicalResourceGroup>().setPhysicalResourceGroup(physicalResourceGroup)
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
