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
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.domain.*;
import nl.surfnet.bod.domain.validator.VirtualPortValidator;
import nl.surfnet.bod.service.*;
import nl.surfnet.bod.support.*;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.manager.VirtualPortController.VirtualPortUpdateCommand;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.ReservationView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class VirtualPortControllerTest {

  @InjectMocks
  private VirtualPortController subject;

  @Mock
  private VirtualPortService virtualPortServiceMock;

  @Mock
  private VirtualResourceGroupService virtualResourceGroupServiceMock;

  @Mock
  private PhysicalResourceGroupService physicalResourceGroupServiceMock;

  @Mock
  private PhysicalPortService physicalPortServiceMock;

  @Mock
  private ReservationService reservationsServiceMock;

  @SuppressWarnings("unused")
  @Mock
  private VirtualPortValidator virtualPortValidatorMock;

  @SuppressWarnings("unused")
  @Mock
  private MessageSource messageSourceMock;

  private RichUserDetails manager = new RichUserDetailsFactory().addUserGroup("urn:manager-group").create();

  @Before
  public void login() {
    Security.setUserDetails(manager);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void listShouldFindEntries() {
    ModelStub model = new ModelStub();

    when(
        virtualPortServiceMock.findEntriesForManager(eq(manager), eq(0), eq(WebUtils.MAX_ITEMS_PER_PAGE),
            any(Sort.class))).thenReturn(Lists.newArrayList(new VirtualPortFactory().create()));

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

  @SuppressWarnings("unchecked")
  @Test
  public void populatePhysicalResourceGroupAndPortsShouldFilterOutEmptyGroups() {
    RichUserDetails user = new RichUserDetailsFactory().create();
    Security.setUserDetails(user);
    Model model = new ModelStub();

    PhysicalPort port = new PhysicalPortFactory().create();
    PhysicalResourceGroup groupWithPorts = new PhysicalResourceGroupFactory().addPhysicalPort(port).create();
    PhysicalResourceGroup groupWithoutPorts = new PhysicalResourceGroupFactory().create();

    when(physicalResourceGroupServiceMock.findAllForManager(user)).thenReturn(
        Lists.newArrayList(groupWithPorts, groupWithoutPorts));

    subject.populatePhysicalResourceGroups(model);

    assertThat(model.asMap(), hasKey("physicalResourceGroups"));
    List<PhysicalResourceGroup> groups = (List<PhysicalResourceGroup>) model.asMap().get("physicalResourceGroups");
    assertThat(groups, hasSize(1));
    assertThat(groups, contains(groupWithPorts));

    assertThat(model.asMap(), hasKey("physicalPorts"));
    List<PhysicalPort> ports = (List<PhysicalPort>) model.asMap().get("physicalPorts");
    assertThat(ports, contains(port));
  }

  @Test
  public void createWithGroupShouldSetVirtualResourceGroupOnPort() {
    ModelStub model = new ModelStub();
    VirtualResourceGroup vGroup = new VirtualResourceGroupFactory().setSurfConextGroupName("urn:some-user-group")
        .create();

    when(virtualResourceGroupServiceMock.find(1L)).thenReturn(vGroup);

    subject.createForm(null, null, 1L, null, model);

    VirtualPort port = (VirtualPort) model.asMap().get("virtualPort");
    assertThat(port.getVirtualResourceGroup(), is(vGroup));
  }

  @Test
  public void createWithIllegalGroupShouldNotSetVirtualResourceGroupOnPort() {
    ModelStub model = new ModelStub();

    when(virtualResourceGroupServiceMock.find(1L)).thenReturn(null);

    subject.createForm(null, null, 1L, null, model);

    VirtualPort port = (VirtualPort) model.asMap().get("virtualPort");
    assertThat(port.getVirtualResourceGroup(), nullValue());
  }

  @Test
  public void createWithPhysicalGroupShoulSetPhysicalPort() {
    ModelStub model = new ModelStub();
    PhysicalPort physicalPort = new PhysicalPortFactory().create();
    PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().setAdminGroup("urn:manager-group")
        .addPhysicalPort(physicalPort).create();

    when(physicalResourceGroupServiceMock.find(2L)).thenReturn(physicalResourceGroup);

    subject.createForm(null, 2L, null, null, model);

    VirtualPort port = (VirtualPort) model.asMap().get("virtualPort");

    assertThat(port.getPhysicalPort(), is(physicalPort));
  }

  @Test
  public void createWithBandwidthShouldSetBandwidth() {
    ModelStub model = new ModelStub();

    subject.createForm(null, null, null, 1200, model);

    VirtualPort port = (VirtualPort) model.asMap().get("virtualPort");
    assertThat(port.getMaxBandwidth(), is(1200));
  }

  @Test
  public void whenBothPhysicalGroupAndPortAreSetIgnoreGroup() {
    ModelStub model = new ModelStub();
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setAdminGroup("urn:manager-group").create();
    PhysicalPort pPort = new PhysicalPortFactory().setPhysicalResourceGroup(group).create();

    when(physicalPortServiceMock.find(1L)).thenReturn(pPort);

    subject.createForm(1L, 3L, null, null, model);

    VirtualPort port = (VirtualPort) model.asMap().get("virtualPort");

    assertThat(port.getPhysicalPort(), is(pPort));

    verify(physicalResourceGroupServiceMock, never()).find(3L);
  }

  @Test
  public void listReservationsForNonExistingPort() {
    when(virtualPortServiceMock.find(2L)).thenReturn(null);

    Collection<ReservationView> reservations = subject.listReservationsForPort(2L);

    assertThat(reservations, hasSize(0));
  }

  @Test
  public void listReservationsForIllegalPort() {
    VirtualPort port = new VirtualPortFactory().setPhysicalPortAdminGroup("urn:wrong-group").create();

    when(virtualPortServiceMock.find(2L)).thenReturn(port);

    Collection<ReservationView> reservations = subject.listReservationsForPort(2L);

    assertThat(reservations, hasSize(0));
  }

  @Test
  public void listReservationsForPort() {
    VirtualPort port = new VirtualPortFactory().setPhysicalPortAdminGroup("urn:manager-group").create();
    Reservation reservation = new ReservationFactory().create();

    when(virtualPortServiceMock.find(2L)).thenReturn(port);
    when(reservationsServiceMock.findByVirtualPort(port)).thenReturn(Lists.newArrayList(reservation));

    Collection<ReservationView> reservations = subject.listReservationsForPort(2L);

    assertThat(reservations, hasSize(1));
  }

}
