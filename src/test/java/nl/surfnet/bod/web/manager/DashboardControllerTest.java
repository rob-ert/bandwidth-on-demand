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
import static org.mockito.Mockito.when;
import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.support.BodRoleFactory;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RunWith(MockitoJUnitRunner.class)
public class DashboardControllerTest {

  @InjectMocks
  private DashboardController subject;

  @Mock
  private PhysicalResourceGroupService physicalResourceGroupServiceMock;

  @Mock
  private Environment environmentMock;
  @Mock
  private MessageSource messageSourceMock;

  @Test
  public void managerWhithInactivePhysicalResourceGroupsShouldGetRedirected() {
    PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().setId(101L).setActive(false)
        .create();

    BodRole selectedRole = new BodRoleFactory().setPhysicalResourceGroup(physicalResourceGroup).create();
    RichUserDetails user = new RichUserDetailsFactory().setSelectedRole(selectedRole).create();
    Security.setUserDetails(user);
    RedirectAttributes model = new ModelStub();

    when(physicalResourceGroupServiceMock.find(physicalResourceGroup.getId())).thenReturn(physicalResourceGroup);

    String page = subject.index(model, model);

    assertThat(page, startsWith("redirect:"));
    assertThat(page, endsWith("id=101"));
    assertThat(model.getFlashAttributes(), hasKey(WebUtils.INFO_MESSAGES_KEY));
  }

  @Test
  public void managerWhithActivePhysicalResourceGroupShouldGoToIndex() {
    PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().setActive(true).create();

    BodRole selectedRole = new BodRoleFactory().setPhysicalResourceGroup(physicalResourceGroup).create();
    RichUserDetails user = new RichUserDetailsFactory().setSelectedRole(selectedRole).create();

    Security.setUserDetails(user);
    RedirectAttributes model = new ModelStub();

    when(physicalResourceGroupServiceMock.find(physicalResourceGroup.getId())).thenReturn(physicalResourceGroup);

    String page = subject.index(model, model);

    assertThat(page, is("manager/index"));
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
