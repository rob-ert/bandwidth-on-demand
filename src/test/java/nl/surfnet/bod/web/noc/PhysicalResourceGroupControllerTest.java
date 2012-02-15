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
package nl.surfnet.bod.web.noc;

import static nl.surfnet.bod.web.WebUtils.MAX_PAGES_KEY;
import static nl.surfnet.bod.web.noc.PhysicalResourceGroupController.MODEL_KEY_LIST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.validator.PhysicalResourceGroupValidator;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.noc.PhysicalResourceGroupController.PhysicalResourceGroupCommand;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class PhysicalResourceGroupControllerTest {

  @InjectMocks
  private PhysicalResourceGroupController subject;

  @Mock
  private PhysicalResourceGroupService physicalResourceGroupServiceMock;

  @SuppressWarnings("unused")
  @Mock
  private PhysicalResourceGroupValidator physicalResourceGroupValidatorMock;

  @Test
  public void listShouldSetGroupsAndMaxPages() {
    Model model = new ModelStub();
    List<PhysicalResourceGroup> groups = Lists.newArrayList(new PhysicalResourceGroupFactory().create());
    when(physicalResourceGroupServiceMock.findEntries(eq(0), anyInt())).thenReturn(groups);

    subject.list(1, model);

    assertThat(model.asMap(), hasEntry(MODEL_KEY_LIST, Object.class.cast(groups)));
    assertThat(model.asMap(), hasEntry(MAX_PAGES_KEY, Object.class.cast(1)));
  }

  @Test
  public void listPortsForNonExistingGroup() {
    when(physicalResourceGroupServiceMock.find(12L)).thenReturn(null);

    Collection<PhysicalPort> ports = subject.listPortsJson(12L);

    assertThat(ports, hasSize(0));
  }

  @Test
  public void listPortsForGroup() {
    PhysicalPort port1 = new PhysicalPortFactory().create();
    PhysicalPort port2 = new PhysicalPortFactory().create();
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().addPhysicalPort(port1, port2).create();

    when(physicalResourceGroupServiceMock.find(12L)).thenReturn(group);

    Collection<PhysicalPort> ports = subject.listPortsJson(12L);

    assertThat(ports, hasSize(2));
    assertThat(ports, hasItems(port1, port2));
  }

  @Test
  public void updateEmailOfPhysicalResourceGroupShouldSentActivationEmail() {
    RedirectAttributes model = new ModelStub();
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setId(1L).setManagerEmail("old@example.com")
        .setAdminGroupName("urn:ict-manager").create();

    PhysicalResourceGroup changedGroup = new PhysicalResourceGroupFactory().setId(1L)
        .setManagerEmail("new@example.com").setAdminGroupName("urn:ict-manager").create();

    PhysicalResourceGroupCommand command = new PhysicalResourceGroupController.PhysicalResourceGroupCommand(
        changedGroup);

    when(physicalResourceGroupServiceMock.find(1L)).thenReturn(group);

    String page = subject.update(command, new BeanPropertyBindingResult(changedGroup, "physicalResrouceGroup"), model);

    assertThat(page, is("redirect:physicalresourcegroups"));
    assertThat(WebUtils.getFirstInfoMessage(model), containsString("has been sent"));
    assertThat(WebUtils.getFirstInfoMessage(model), containsString("new@example.com"));

    verify(physicalResourceGroupServiceMock).sendAndPersistActivationRequest(any(PhysicalResourceGroup.class));
  }

  @Test
  public void updateWhenEmailDidNotChangeDontSentActivationEmail() {
    RedirectAttributes model = new ModelStub();
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setId(1L).setManagerEmail("mail@example.com")
        .create();

    when(physicalResourceGroupServiceMock.find(1L)).thenReturn(group);

    PhysicalResourceGroupCommand command = new PhysicalResourceGroupController.PhysicalResourceGroupCommand(group);
    String page = subject.update(command, new BeanPropertyBindingResult(group, "physicalResrouceGroup"), model);

    assertThat(page, is("redirect:physicalresourcegroups"));
    assertThat(model.getFlashAttributes().keySet(), hasSize(0));

    verify(physicalResourceGroupServiceMock, never()).sendAndPersistActivationRequest(group);
    verify(physicalResourceGroupServiceMock).update(group);
  }

  @SuppressWarnings("serial")
  @Test
  public void updateWithErrorsShouldNotUpdate() {
    RedirectAttributes model = new ModelStub();
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().create();
    when(physicalResourceGroupServiceMock.find(group.getId())).thenReturn(group);

    BeanPropertyBindingResult result = new BeanPropertyBindingResult(group, "physicalResrouceGroup") {
      @Override
      public boolean hasErrors() {
        return true;
      }
    };

    PhysicalResourceGroupCommand command = new PhysicalResourceGroupController.PhysicalResourceGroupCommand(group);
    String page = subject.update(command, result, model);

    assertThat(page, is("physicalresourcegroups/update"));
    assertThat(model.asMap().get("physicalResourceGroup"), is(Object.class.cast(command)));
  }

}