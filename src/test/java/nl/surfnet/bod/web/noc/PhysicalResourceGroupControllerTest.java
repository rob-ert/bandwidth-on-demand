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

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.service.*;
import nl.surfnet.bod.support.*;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.noc.PhysicalResourceGroupController.PhysicalResourceGroupCommand;
import nl.surfnet.bod.web.view.PhysicalResourceGroupView;

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
  private InstituteService instituteServiceMock;

  @Mock
  private PhysicalPortService physicalPortServiceMock;

  @Mock
  private MessageSource messageSourceMock;

  @Mock
  private VirtualPortService virtualPortServiceMock;

  @Mock
  private ReservationService reservationServiceMock;

  @Test
  public void listShouldSetGroupsAndMaxPages() {
    Model model = new ModelStub();
    final PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().create();
    List<PhysicalResourceGroup> groups = Lists.newArrayList(physicalResourceGroup, new PhysicalResourceGroupFactory().create());
    List<PhysicalResourceGroupView> groupViews = Lists.newArrayList(new PhysicalResourceGroupView(physicalResourceGroup));
    List<PhysicalPort> physicalPorts = Lists.newArrayList(physicalResourceGroup.getPhysicalPorts());
    List<VirtualPort> virtualPorts = Lists.newArrayList(new VirtualPortFactory().create());
    List<Reservation> reservations = Lists.newArrayList(new ReservationFactory().create());

    when(physicalPortServiceMock.findAllocatedEntriesForPhysicalResourceGroup(any(PhysicalResourceGroup.class),
        anyInt(), anyInt(), any(Sort.class))).thenReturn(physicalPorts);
    when(virtualPortServiceMock.findAllForPhysicalPort(any(PhysicalPort.class))).thenReturn(virtualPorts);
    when(reservationServiceMock.findActiveByPhysicalPort(any(PhysicalPort.class))).thenReturn(reservations);
    when(physicalResourceGroupServiceMock.findEntries(eq(0), anyInt(), any(Sort.class))).thenReturn(groups);

    subject.list(1, null, null, model);

    @SuppressWarnings("unchecked")
    final List<PhysicalResourceGroupView> physicalResourceGroupViews = (List<PhysicalResourceGroupView>) model.asMap().get("list");
    assertThat(groupViews.get(0), is(physicalResourceGroupViews.get(0)));
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
    when(physicalResourceGroupServiceMock.findByAdminGroup("urn:ict-manager")).thenReturn(Lists.newArrayList(group));

    String page = subject.update(command, new BeanPropertyBindingResult(changedGroup,
        PhysicalResourceGroupController.MODEL_KEY), model, redirectAttribs);

    assertThat(page, is("redirect:" + PhysicalResourceGroupController.PAGE_URL));
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
    when(physicalResourceGroupServiceMock.findByAdminGroup(group.getAdminGroup())).thenReturn(Lists.newArrayList(group));

    PhysicalResourceGroupCommand command = new PhysicalResourceGroupController.PhysicalResourceGroupCommand(group);
    String page = subject.update(command, new BeanPropertyBindingResult(group,
        PhysicalResourceGroupController.MODEL_KEY), model, redirectAttribs);

    assertThat(page, is("redirect:" + PhysicalResourceGroupController.PAGE_URL));
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
    when(instituteServiceMock.find(group.getInstitute().getId())).thenReturn(new InstituteFactory().create());
    when(physicalResourceGroupServiceMock.findByAdminGroup(group.getAdminGroup())).thenReturn(Lists.newArrayList(group));

    BeanPropertyBindingResult result = new BeanPropertyBindingResult(group, PhysicalResourceGroupController.MODEL_KEY) {
      @Override
      public boolean hasErrors() {
        return true;
      }
    };

    PhysicalResourceGroupCommand command = new PhysicalResourceGroupController.PhysicalResourceGroupCommand(group);
    String page = subject.update(command, result, model, redirectAtribs);

    assertThat(page, is("noc/" + PhysicalResourceGroupController.PAGE_URL + "/update"));

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

    assertThat(page, is("redirect:" + PhysicalResourceGroupController.PAGE_URL));

    verify(physicalResourceGroupServiceMock, never()).sendActivationRequest(group);
    verify(physicalResourceGroupServiceMock, never()).update(group);
  }

  @Test
  public void commandShouldTrimGroupUrn() {
    PhysicalResourceGroupCommand subject = new PhysicalResourceGroupCommand();
    subject.setAdminGroup("  urn:surfguest:group-one ");

    PhysicalResourceGroup group = new PhysicalResourceGroup();

    subject.copyFieldsTo(group);

    assertThat(group.getAdminGroup(), is("urn:surfguest:group-one"));
  }

  @Test
  public void updateShouldFailIfAdminGroupIsNotUnqiue() {
    RedirectAttributes model = new ModelStub();
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().create();
    PhysicalResourceGroupCommand command = new PhysicalResourceGroupController.PhysicalResourceGroupCommand(group);
    BeanPropertyBindingResult result = new BeanPropertyBindingResult(group, PhysicalResourceGroupController.MODEL_KEY);

    when(physicalResourceGroupServiceMock.find(group.getId())).thenReturn(group);
    when(physicalResourceGroupServiceMock.findByAdminGroup(group.getAdminGroup())).thenReturn(Lists.newArrayList(new PhysicalResourceGroupFactory().create()));

    String page = subject.update(command, result, model, model);

    assertThat(page, is("noc/institutes/update"));
    assertThat(result.hasFieldErrors("adminGroup"), is(true));
  }

  @Test
  public void updateShouldSucceedIfAdminGroupIsNew() {
    RedirectAttributes model = new ModelStub();
    PhysicalResourceGroup group = new PhysicalResourceGroupFactory().create();
    PhysicalResourceGroupCommand command = new PhysicalResourceGroupController.PhysicalResourceGroupCommand(group);
    BeanPropertyBindingResult result = new BeanPropertyBindingResult(group, PhysicalResourceGroupController.MODEL_KEY);

    when(physicalResourceGroupServiceMock.find(group.getId())).thenReturn(group);
    when(physicalResourceGroupServiceMock.findByAdminGroup(group.getAdminGroup())).thenReturn(Collections.<PhysicalResourceGroup>emptyList());

    String page = subject.update(command, result, model, model);

    assertThat(page, is("redirect:institutes"));
  }
}