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

import java.util.Collection;
import java.util.Locale;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualPortRequestLink;
import nl.surfnet.bod.domain.validator.VirtualPortValidator;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.support.VirtualPortRequestLinkFactory;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.manager.VirtualPortController.VirtualPortCreateCommand;
import nl.surfnet.bod.web.manager.VirtualPortController.VirtualPortUpdateCommand;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.security.Security.RoleEnum;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Sort;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VirtualPortControllerTest {

  @InjectMocks
  private VirtualPortController subject;

  @Mock
  private VirtualPortService virtualPortServiceMock;

  @Mock
  private VirtualPortValidator virtualPortValidatorMock;

  @Mock
  private MessageSource messageSourceMock;

  private RichUserDetails user;
  private PhysicalPort physicalPort;
  private PhysicalResourceGroup prg;

  @Before
  public void login() {
    physicalPort = new PhysicalPortFactory().create();
    prg = new PhysicalResourceGroupFactory().setAdminGroup("urn:manager-group").addPhysicalPort(physicalPort).create();

    when(messageSourceMock.getMessage(eq("info_virtualport_updated"), any(Object[].class), any(Locale.class)))
        .thenReturn("Updated");
    when(
        messageSourceMock.getMessage(eq("info_virtualportrequestlink_notmanager"), any(Object[].class),
            any(Locale.class))).thenReturn("Not Manager");
    when(messageSourceMock.getMessage(eq("info_virtualport_created"), any(Object[].class), any(Locale.class)))
        .thenReturn("Created");

    user = new RichUserDetailsFactory().addManagerRole(prg).addUserRole().addUserGroup("urn:manager-group").create();
    Security.setUserDetails(user);
  }

  @Ignore("Fix later, mock tarnsform")
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
    assertThat(model.getFlashAttributes(), hasKey("infoMessages"));

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
