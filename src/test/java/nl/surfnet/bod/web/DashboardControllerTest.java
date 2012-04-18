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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.util.List;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.support.*;
import nl.surfnet.bod.web.DashboardController.TeamView;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.security.Security.RoleEnum;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.Model;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class DashboardControllerTest {

  @InjectMocks
  private DashboardController subject;

  @Mock
  private VirtualResourceGroupService virtualResourceGroupServiceMock;
  @Mock
  private ReservationService reservationServiceMock;

  @Test
  public void whenUserHasNoVirtualResourceGroupsShouldGoToSpecialView() {
    RichUserDetails user = new RichUserDetailsFactory().create();

    Security.setUserDetails(user);
    Model model = new ModelStub();

    String page = subject.index(model);

    assertThat(page, is("noUserRole"));
    assertThat(model.asMap(), hasKey("userGroups"));
  }

  @Test
  public void showDashboardForUser() {
    BodRole selectedRole = new BodRoleFactory().setRole(RoleEnum.USER).create();
    RichUserDetails user = new RichUserDetailsFactory().setSelectedRole(selectedRole).create();
    VirtualResourceGroup vrg = new VirtualResourceGroupFactory().create();

    Security.setUserDetails(user);
    Model model = new ModelStub();

    when(virtualResourceGroupServiceMock.findAllForUser(user)).thenReturn(ImmutableList.of(vrg));

    String page = subject.index(model);

    assertThat(page, is("index"));
    assertThat(model.asMap(), hasKey("teams"));
  }

  @Test
  public void groupsShouldBeSorted() {
    BodRole selectedRole = new BodRoleFactory().setRole(RoleEnum.USER).create();

    UserGroup userGroup1 = new UserGroupFactory().setName("A").setId("urn:a").create();
    UserGroup userGroup2 = new UserGroupFactory().setName("B").setId("urn:b").create();
    UserGroup userGroup3 = new UserGroupFactory().setName("C").setId("urn:c").create();
    UserGroup userGroup4 = new UserGroupFactory().setName("D").setId("urn:d").create();

    RichUserDetails user = new RichUserDetailsFactory().setSelectedRole(selectedRole)
        .addUserGroup(userGroup1, userGroup2, userGroup3, userGroup4).create();

    VirtualResourceGroup vrg1 = new VirtualResourceGroupFactory().setName("A").setSurfconextGroupId("urn:a").create();
    VirtualResourceGroup vrg2 = new VirtualResourceGroupFactory().setName("B").setSurfconextGroupId("urn:b").create();
    VirtualResourceGroup vrg3 = new VirtualResourceGroupFactory().setName("C").setSurfconextGroupId("urn:c").create();

    Security.setUserDetails(user);
    Model model = new ModelStub();

    when(virtualResourceGroupServiceMock.findAllForUser(user)).thenReturn(ImmutableList.of(vrg3, vrg1, vrg2));

    String page = subject.index(model);

    assertThat(page, is("index"));
    assertThat(model.asMap(), hasKey("teams"));
    List<TeamView> teams = (List<TeamView>) model.asMap().get("teams");

    assertThat(teams, hasSize(4));

    assertThat(Lists.transform(teams, new Function<TeamView, String>() {
      @Override
      public String apply(TeamView team) {
        return team.getName();
      }
    }), contains("A", "B", "C", "D"));

  }

}
