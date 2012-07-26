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
package nl.surfnet.bod.web.security;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.support.InstituteFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;

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
    BodRole role1 = BodRole.createNocEngineer();
    BodRole role2 = BodRole.createManager(new PhysicalResourceGroupFactory().setInstitute(
        new InstituteFactory().setName("A").create()).create());
    BodRole role3 = BodRole.createManager(new PhysicalResourceGroupFactory().setInstitute(
        new InstituteFactory().setName("B").create()).create());
    BodRole role4 = BodRole.createUser();

    RichUserDetails userDetails = new RichUserDetailsFactory().addBodRoles(role4, role3, role2, role1).create();

    List<BodRole> sortedRoles = userDetails.getBodRoles();
    assertThat(sortedRoles, hasSize(4));

    // Verify by sortOrder in enum
    assertThat(sortedRoles.get(0), is(role1));
    assertThat(sortedRoles.get(1), is(role2));
    assertThat(sortedRoles.get(2), is(role3));
    assertThat(sortedRoles.get(3), is(role4));
  }

}
