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
package nl.surfnet.bod.web.user;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.domain.VirtualPortRequestLink;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.service.InstituteService;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.support.*;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.TeamView;

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
  @Mock
  private VirtualPortService virtualPortServiceMock;
  @Mock
  private InstituteService instituteServiceMock;

  @Test
  public void whenUserHasNoUserRoleShouldGoToSpecialView() {
    Security.setUserDetails(new RichUserDetailsFactory().create());

    Model model = new ModelStub();

    String page = subject.index(model);

    assertThat(page, is("noUserRole"));
    assertThat(model.asMap(), hasKey("userGroups"));
  }

  @Test
  public void whenUserHasNewUserRoleShouldGoToSpecialView() {
    BodRole selectedRole = BodRole.createNewUser();
    RichUserDetails user = new RichUserDetailsFactory().addBodRoles(selectedRole).create();

    Security.setUserDetails(user);
    Model model = new ModelStub();

    String page = subject.index(model);

    assertThat(page, is("noUserRole"));
    assertThat(model.asMap(), hasKey("userGroups"));
  }

  @Test
  public void showDashboardForUserWhichCanNotCreateReservations() {
    VirtualResourceGroup vrg = new VirtualResourceGroupFactory().create();

    RichUserDetails user = new RichUserDetailsFactory().addUserRole().create();
    Security.setUserDetails(user);

    Model model = new ModelStub();

    when(virtualResourceGroupServiceMock.findAllForUser(user)).thenReturn(ImmutableList.of(vrg));

    String page = subject.index(model);

    assertThat(page, is("index"));

    assertThat(model.asMap(), hasKey("canCreateReservation"));
    assertThat(model.asMap(), hasKey("teams"));

    Boolean canCreate = (Boolean) model.asMap().get("canCreateReservation");
    assertThat(canCreate, is(false));

    @SuppressWarnings("unchecked")
    List<TeamView> views = (List<TeamView>) model.asMap().get("teams");
    assertThat(views, hasSize(1));

    verify(reservationServiceMock).countActiveReservationsForGroup(vrg);
    verify(reservationServiceMock).countElapsedReservationsForGroup(vrg);
    verify(reservationServiceMock).countComingReservationsForGroup(vrg);
  }

  @Test
  public void showDashboardForUserWhichCanCreateReservations() {
    VirtualResourceGroup vrgWithPorts = new VirtualResourceGroupFactory().addVirtualPorts(
        new VirtualPortFactory().create(), new VirtualPortFactory().create()).create();

    RichUserDetails user = new RichUserDetailsFactory().addUserRole().create();
    Security.setUserDetails(user);

    Model model = new ModelStub();

    when(virtualResourceGroupServiceMock.findAllForUser(user)).thenReturn(ImmutableList.of(vrgWithPorts));

    subject.index(model);

    assertThat(model.asMap(), hasKey("canCreateReservation"));

    Boolean canCreate = (Boolean) model.asMap().get("canCreateReservation");
    assertThat(canCreate, is(true));
  }

  @Test
  public void groupsShouldBeSorted() {
    UserGroup userGroup1 = new UserGroupFactory().setName("A").setId("urn:a").create();
    UserGroup userGroup2 = new UserGroupFactory().setName("B").setId("urn:b").create();
    UserGroup userGroup3 = new UserGroupFactory().setName("C").setId("urn:c").create();
    UserGroup userGroup4 = new UserGroupFactory().setName("D").setId("urn:d").create();

    VirtualResourceGroup vrg1 = new VirtualResourceGroupFactory().setName("A").setAdminGroup("urn:a").create();
    VirtualResourceGroup vrg2 = new VirtualResourceGroupFactory().setName("B").setAdminGroup("urn:b").create();
    VirtualResourceGroup vrg3 = new VirtualResourceGroupFactory().setName("C").setAdminGroup("urn:c").create();

    RichUserDetails user = new RichUserDetailsFactory().addUserRole()
        .addUserGroup(userGroup1, userGroup2, userGroup3, userGroup4).create();
    Security.setUserDetails(user);

    Model model = new ModelStub();

    when(virtualResourceGroupServiceMock.findAllForUser(user)).thenReturn(ImmutableList.of(vrg3, vrg1, vrg2));

    String page = subject.index(model);

    assertThat(page, is("index"));
    assertThat(model.asMap(), hasKey("teams"));
    @SuppressWarnings("unchecked")
    List<TeamView> teams = (List<TeamView>) model.asMap().get("teams");

    assertThat(teams, hasSize(4));

    assertThat(Lists.transform(teams, new Function<TeamView, String>() {
      @Override
      public String apply(TeamView team) {
        return team.getName();
      }
    }), contains("A", "B", "C", "D"));
  }

  @Test
  public void overviewShouldShowVirutalPortRequests() {
    VirtualPortRequestLink link = new VirtualPortRequestLinkFactory().create();

    RichUserDetails user = new RichUserDetailsFactory().addUserRole().create();
    Security.setUserDetails(user);

    Model model = new ModelStub();

    when(virtualPortServiceMock.findRequestsForLastMonth(user.getUserGroups())).thenReturn(ImmutableList.of(link));

    String page = subject.index(model);

    assertThat(page, is("index"));
    assertThat(model.asMap(), hasKey("requests"));
    @SuppressWarnings("unchecked")
    List<VirtualPortRequestLink> requests = (List<VirtualPortRequestLink>) model.asMap().get("requests");
    assertThat(requests, contains(link));
    assertThat(link.getPhysicalResourceGroup().getInstitute(), notNullValue());
  }

}
