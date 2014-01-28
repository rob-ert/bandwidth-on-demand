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

import static nl.surfnet.bod.matchers.OptionalMatchers.isAbsent;
import static nl.surfnet.bod.matchers.OptionalMatchers.isPresent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.domain.oauth.NsiScope;
import nl.surfnet.bod.support.InstituteFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;

import org.hamcrest.Matchers;
import org.junit.Test;

public class RichUserDetailsTest {

  @Test
  public void no_groups_should_give_an_empty_list_of_user_group_ids() {
    RichUserDetails userDetails = new RichUserDetailsFactory().create();

    assertThat(userDetails.getUserGroupIds(), Matchers.<String> empty());
  }

  @Test
  public void should_give_back_the_user_group_ids() {
    RichUserDetails userDetails = new RichUserDetailsFactory().addUserGroup("urn:first").addUserGroup("urn:second")
        .create();

    assertThat(userDetails.getUserGroupIds(), contains("urn:first", "urn:second"));
  }

  @Test
  public void default_methods() {
    RichUserDetails user = new RichUserDetailsFactory().create();

    assertThat(user.isAccountNonExpired(), is(true));
    assertThat(user.isAccountNonLocked(), is(true));
    assertThat(user.isCredentialsNonExpired(), is(true));
    assertThat(user.isEnabled(), is(true));
    assertThat(user.getPassword(), is("N/A"));
  }

  @Test
  public void to_string_contains_name_id() {
    RichUserDetails user = new RichUserDetailsFactory().setUsername("urn:truus").create();

    assertThat(user.toString(), containsString("urn:truus"));
  }

  @Test
  public void switch_role_should_change_selected_role() {
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
  public void should_sort_roles_test_on_sort_order() {
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
  public void switch_role_to_app_manager() {
    BodRole appManagerRole = BodRole.createAppManager();

    RichUserDetails userDetails = new RichUserDetailsFactory().addBodRoles(appManagerRole).create();

    userDetails.switchToRoleById(appManagerRole.getId());

    assertThat(userDetails.getSelectedRole(), is(appManagerRole));
    assertThat(userDetails.getBodRoles(), hasSize(1));
    assertThat(userDetails.getSelectableRoles(), hasSize(0));

    userDetails.trySwitchToAppManager();

    assertThat(userDetails.getSelectedRole(), is(appManagerRole));
  }

  @Test
  public void when_email_is_not_valid_email_should_be_absent() {

    RichUserDetails userDetails = new RichUserDetails("johns", "John Smith", "Smith, John <john@example.com>",
        Collections.<UserGroup> emptyList(),
        Lists.newArrayList(BodRole.createAppManager()),
        Collections.<NsiScope> emptyList());

    assertThat(userDetails.getEmail(), isAbsent());
  }

  @Test
  public void when_email_is_valid_email_should_be_present() {
    RichUserDetails userDetails = new RichUserDetails("johns", "John Smith", "\"Smith, John\" <john@example.com>",
        Collections.<UserGroup> emptyList(),
        Lists.newArrayList(BodRole.createAppManager()),
        Collections.<NsiScope> emptyList());

    assertThat(userDetails.getEmail().transform(Functions.toStringFunction()), isPresent("\"Smith, John\" <john@example.com>"));
  }
}
