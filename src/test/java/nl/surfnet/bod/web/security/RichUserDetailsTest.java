/**
 * Copyright (c) 2012, 2013 SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.web.security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.List;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.support.InstituteFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;

import org.hamcrest.Matchers;
import org.junit.Test;

public class RichUserDetailsTest {

  @Test
  public void noGroupsShouldGiveAnEmptyListOfUserGroupIds() {
    RichUserDetails userDetails = new RichUserDetailsFactory().create();

    assertThat(userDetails.getUserGroupIds(), Matchers.<String> empty());
  }

  @Test
  public void shouldGiveBackTheUserGroupIds() {
    RichUserDetails userDetails = new RichUserDetailsFactory().addUserGroup("urn:first").addUserGroup("urn:second")
        .create();

    assertThat(userDetails.getUserGroupIds(), contains("urn:first", "urn:second"));
  }

  @Test
  public void defaultMethods() {
    RichUserDetails user = new RichUserDetailsFactory().create();

    assertThat(user.isAccountNonExpired(), is(true));
    assertThat(user.isAccountNonLocked(), is(true));
    assertThat(user.isCredentialsNonExpired(), is(true));
    assertThat(user.isEnabled(), is(true));
    assertThat(user.getPassword(), is("N/A"));
  }

  @Test
  public void toStringContainsNameId() {
    RichUserDetails user = new RichUserDetailsFactory().setUsername("urn:truus").create();

    assertThat(user.toString(), containsString("urn:truus"));
  }

  @Test
  public void switchRoleShouldChangeSelectedRole() {
    BodRole newUserRole = BodRole.createNewUser();
    PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().create();
    BodRole managerRole = BodRole.createManager(physicalResourceGroup);
    BodRole nocRole = BodRole.createNocEngineer();

    RichUserDetails userDetails = new RichUserDetailsFactory().addBodRoles(nocRole, newUserRole, managerRole).create();

    userDetails.switchToUser();

    assertThat(userDetails.getSelectedRole(), is(newUserRole));
    assertThat(userDetails.getBodRoles(), hasSize(3));
    assertThat(userDetails.getSelectableRoles(), hasSize(2));
    assertThat(userDetails.getSelectableRoles(), not(hasItem(newUserRole)));

    userDetails.switchToManager(physicalResourceGroup);

    assertThat(userDetails.getSelectedRole(), is(managerRole));
    assertThat(userDetails.getBodRoles(), hasSize(3));
    assertThat(userDetails.getSelectableRoles(), hasSize(2));
    assertThat(userDetails.getSelectableRoles(), not(hasItem(managerRole)));

    userDetails.trySwitchToNoc();

    assertThat(userDetails.getSelectedRole(), is(nocRole));
  }

  @Test
  public void shouldSortRolesTestOnSortOrder() {
    BodRole appManager = BodRole.createAppManager();
    BodRole nocEngineer = BodRole.createNocEngineer();
    BodRole manager1 = BodRole.createManager(
      new PhysicalResourceGroupFactory()
        .setInstitute(new InstituteFactory().setName("A").create())
        .create());
    BodRole manager2 = BodRole.createManager(
      new PhysicalResourceGroupFactory()
        .setInstitute(new InstituteFactory().setName("B").create())
        .create());
    BodRole manager3 = BodRole.createUser();

    RichUserDetails userDetails = new RichUserDetailsFactory().addBodRoles(manager3, manager2, appManager, nocEngineer, manager1).create();

    List<BodRole> sortedRoles = userDetails.getBodRoles();

    assertThat(sortedRoles, hasSize(5));

    assertThat(sortedRoles.get(0), is(nocEngineer));
    assertThat(sortedRoles.get(1), is(appManager));
    assertThat(sortedRoles.get(2), is(manager1));
    assertThat(sortedRoles.get(3), is(manager2));
    assertThat(sortedRoles.get(4), is(manager3));
  }

  @Test
  public void switchRoleToAppManager() {
    BodRole appManagerRole = BodRole.createAppManager();

    RichUserDetails userDetails = new RichUserDetailsFactory().addBodRoles(appManagerRole).create();

    userDetails.switchToRoleById(appManagerRole.getId());

    assertThat(userDetails.getSelectedRole(), is(appManagerRole));
    assertThat(userDetails.getBodRoles(), hasSize(1));
    assertThat(userDetails.getSelectableRoles(), hasSize(0));

    userDetails.trySwitchToAppManager();

    assertThat(userDetails.getSelectedRole(), is(appManagerRole));
  }

}
