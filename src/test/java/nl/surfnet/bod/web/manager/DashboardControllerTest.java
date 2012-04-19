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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;
import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.support.BodRoleFactory;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RunWith(MockitoJUnitRunner.class)
public class DashboardControllerTest {

  @InjectMocks
  private DashboardController subject;

  @Mock
  private PhysicalResourceGroupService physicalResourceGroupServiceMock;

  @Test
  public void shouldAddPrgToModel() {
    PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().setId(101L).create();
    BodRole selectedRole = new BodRoleFactory().setPhysicalResourceGroup(physicalResourceGroup).create();
    RichUserDetails user = new RichUserDetailsFactory().setSelectedRole(selectedRole).create();

    Security.setUserDetails(user);
    RedirectAttributes model = new ModelStub();

    when(physicalResourceGroupServiceMock.find(physicalResourceGroup.getId())).thenReturn(physicalResourceGroup);

    String page = subject.index(model);

    assertThat((PhysicalResourceGroup) model.asMap().get("prg"), is(physicalResourceGroup));
    assertThat(page, is("manager/index"));
  }

  @Test
  public void shouldAddNullPrgToModel() {
    BodRole selectedRole = new BodRoleFactory().create();
    RichUserDetails user = new RichUserDetailsFactory().setSelectedRole(selectedRole).create();

    Security.setUserDetails(user);
    RedirectAttributes model = new ModelStub();

    String page = subject.index(model);

    assertThat((PhysicalResourceGroup) model.asMap().get("prg"), nullValue());
    assertThat(page, is("manager/index"));
  }
}