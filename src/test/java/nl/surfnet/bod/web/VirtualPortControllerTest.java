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
package nl.surfnet.bod.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collection;

import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;
import nl.surfnet.bod.web.VirtualPortController.UpdateUserLabelCommand;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Sort;
import org.springframework.validation.BeanPropertyBindingResult;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class VirtualPortControllerTest {

  @InjectMocks
  private VirtualPortController subject;

  @Mock
  private VirtualPortService virtualPortServiceMock;

  private RichUserDetails user;

  @Before
  public void login() {
    user = new RichUserDetailsFactory().addUserGroup("urn:correct-group").create();
    Security.setUserDetails(user);
  }

  @Test
  public void list() {
    when(virtualPortServiceMock.findEntriesForUser(eq(user), eq(0), anyInt(), any(Sort.class))).thenReturn(
        Lists.newArrayList(new VirtualPortFactory().create()));

    ModelStub model = new ModelStub();
    subject.list(null, null, null, model);

    assertThat(model.asMap(), hasKey("list"));

    @SuppressWarnings("unchecked")
    Collection<VirtualPort> ports = (Collection<VirtualPort>) model.asMap().get("list");
    assertThat(ports, hasSize(1));
  }

  @Test
  public void updateFormForNonExistingPort() {
    when(virtualPortServiceMock.find(201L)).thenReturn(null);

    String page = subject.updateForm(201L, new ModelStub());

    assertThat(page, is("redirect:"));
  }

  @Test
  public void updateFormForIllegalPort() {
    VirtualResourceGroup group = new VirtualResourceGroupFactory().setSurfConextGroupName("urn:wrong-group").create();
    VirtualPort port = new VirtualPortFactory().setVirtualResourceGroup(group).create();

    when(virtualPortServiceMock.find(201L)).thenReturn(port);

    ModelStub model = new ModelStub();
    String page = subject.updateForm(201L, model);

    assertThat(page, is("redirect:"));
  }

  @Test
  public void updateFormForPort() {
    VirtualResourceGroup group = new VirtualResourceGroupFactory().setSurfConextGroupName("urn:correct-group").create();
    VirtualPort port = new VirtualPortFactory().setVirtualResourceGroup(group).create();

    when(virtualPortServiceMock.find(201L)).thenReturn(port);

    ModelStub model = new ModelStub();
    String page = subject.updateForm(201L, model);

    assertThat(model.asMap(), hasKey("virtualPort"));
    assertThat(page, is("virtualports/update"));
  }

  @Test
  public void updateForNonExistingPort() {
    UpdateUserLabelCommand command = new UpdateUserLabelCommand();
    command.setId(201L);

    when(virtualPortServiceMock.find(201L)).thenReturn(null);

    String page = subject.update(command, new BeanPropertyBindingResult(command, "command"), new ModelStub());

    assertThat(page, is("redirect:/virtualports"));
  }

}
