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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.InstituteService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.support.InstituteFactory;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.noc.PhysicalResourceGroupController.PhysicalResourceGroupCommand;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Sort;
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

  @Mock
  private InstituteService instituteService;

  @Mock
  private MessageSource messageSourceMock;

  @Test
  public void listShouldSetGroupsAndMaxPages() {
    Model model = new ModelStub();
    List<PhysicalResourceGroup> groups = Lists.newArrayList(new PhysicalResourceGroupFactory().create());
    when(physicalResourceGroupServiceMock.findEntries(eq(0), anyInt(), any(Sort.class))).thenReturn(groups);

    subject.list(1, null, null, model);

    assertThat(model.asMap(), hasEntry("list", Object.class.cast(groups)));
    assertThat(model.asMap(), hasEntry(MAX_PAGES_KEY, Object.class.cast(1)));
  }

  @Test
  public void updateEmailOfPhysicalResourceGroupShouldSentActivationEmail() {
    RedirectAttributes redirectAttribs = new ModelStub();
    Model model = new ModelStub();
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setId(1L).setManagerEmail("old@example.com")
        .setAdminGroup("urn:ict-manager").create();

    PhysicalResourceGroup changedGroup = new PhysicalResourceGroupFactory().setId(1L)
        .setManagerEmail("new@example.com").setAdminGroup("urn:ict-manager").create();

    PhysicalResourceGroupCommand command = new PhysicalResourceGroupController.PhysicalResourceGroupCommand(
        changedGroup);

    when(messageSourceMock.getMessage(eq("info_activation_request_send"), any(Object[].class), any(Locale.class)))
        .thenReturn("SEND");
    when(physicalResourceGroupServiceMock.find(1L)).thenReturn(group);

    String page = subject.update(command, new BeanPropertyBindingResult(changedGroup,
        PhysicalResourceGroupController.MODEL_KEY), model, redirectAttribs);

    assertThat(page, is("redirect:physicalresourcegroups"));
    assertThat(WebUtils.getFirstInfoMessage(redirectAttribs), containsString("SEND"));

    verify(physicalResourceGroupServiceMock).sendActivationRequest(any(PhysicalResourceGroup.class));
  }

  @Test
  public void updateWhenEmailDidNotChangeDontSentActivationEmail() {
    RedirectAttributes redirectAttribs = new ModelStub();
    Model model = new ModelStub();
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setId(1L).setManagerEmail("mail@example.com")
        .create();

    when(physicalResourceGroupServiceMock.find(1L)).thenReturn(group);

    PhysicalResourceGroupCommand command = new PhysicalResourceGroupController.PhysicalResourceGroupCommand(group);
    String page = subject.update(command, new BeanPropertyBindingResult(group,
        PhysicalResourceGroupController.MODEL_KEY), model, redirectAttribs);

    assertThat(page, is("redirect:physicalresourcegroups"));
    assertThat(redirectAttribs.getFlashAttributes().keySet(), hasSize(0));

    verify(physicalResourceGroupServiceMock, never()).sendActivationRequest(group);
    verify(physicalResourceGroupServiceMock).update(group);
  }

  @SuppressWarnings("serial")
  @Test
  public void updateWithErrorsShouldNotUpdate() {
    RedirectAttributes redirectAtribs = new ModelStub();
    Model model = new ModelStub();
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().create();

    when(physicalResourceGroupServiceMock.find(group.getId())).thenReturn(group);
    when(instituteService.find(group.getInstitute().getId())).thenReturn(new InstituteFactory().create());

    BeanPropertyBindingResult result = new BeanPropertyBindingResult(group, PhysicalResourceGroupController.MODEL_KEY) {
      @Override
      public boolean hasErrors() {
        return true;
      }
    };

    PhysicalResourceGroupCommand command = new PhysicalResourceGroupController.PhysicalResourceGroupCommand(group);
    String page = subject.update(command, result, model, redirectAtribs);

    assertThat(page, is("physicalresourcegroups/update"));
    PhysicalResourceGroupCommand newCommand = (PhysicalResourceGroupCommand) model.asMap().get(
        PhysicalResourceGroupController.MODEL_KEY);

    assertThat(newCommand, is(command));

    assertThat(newCommand.getInstitute(), not(nullValue()));

    verify(physicalResourceGroupServiceMock, never()).update(any(PhysicalResourceGroup.class));
  }

  @Test
  public void updateForIllegalGroupShouldNotUpdate() {
    RedirectAttributes model = new ModelStub();
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().create();

    when(physicalResourceGroupServiceMock.find(group.getId())).thenReturn(null);

    PhysicalResourceGroupCommand command = new PhysicalResourceGroupController.PhysicalResourceGroupCommand(group);
    String page = subject.update(command, new BeanPropertyBindingResult(group,
        PhysicalResourceGroupController.MODEL_KEY), model, model);

    assertThat(page, is("redirect:physicalresourcegroups"));

    verify(physicalResourceGroupServiceMock, never()).sendActivationRequest(group);
    verify(physicalResourceGroupServiceMock, never()).update(group);
  }

}