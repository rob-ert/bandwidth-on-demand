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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.support.BodRoleFactory;
import nl.surfnet.bod.support.InstituteFactory;
import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.google.common.collect.Lists;

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
  public void shouldSwitchRoleAndAddCurrentSelected() {
    BodRole role1 = new BodRoleFactory().create();
    BodRole role2 = new BodRoleFactory().setRole(Security.RoleEnum.ICT_MANAGER).create();

    RichUserDetails userDetails = new RichUserDetailsFactory().create();
    userDetails.addBodRoles(Lists.newArrayList(role1, role2));
    userDetails.setSelectedRole(role1);

    assertThat(userDetails.getSelectedRole(), is(role1));
    assertThat(userDetails.getBodRoles(), hasSize(2));

    userDetails.switchRoleTo(role2);
    assertThat(userDetails.getBodRoles(), hasSize(1));
    assertThat(userDetails.getBodRoles(), contains(role1));

    assertThat(userDetails.getSelectedRole(), is(role2));
  }

  @Test
  public void shouldPerformNoActionWhenSwitchToNullRole() {
    BodRole role1 = new BodRoleFactory().create();
    BodRole role2 = new BodRoleFactory().setRole(Security.RoleEnum.ICT_MANAGER).create();

    RichUserDetails userDetails = new RichUserDetailsFactory().create();
    userDetails.addBodRoles(Lists.newArrayList(role1, role2));
    userDetails.setSelectedRole(role1);

    assertThat(userDetails.getSelectedRole(), is(role1));
    assertThat(userDetails.getBodRoles(), hasSize(2));

    userDetails.switchRoleTo(null);
    assertThat(userDetails.getBodRoles(), hasSize(2));

    assertThat(userDetails.getSelectedRole(), is(role1));
  }

  @Test
  public void shouldSortRolesTestOnSortOrder() {
    BodRole role1 = new BodRoleFactory().setRole(Security.RoleEnum.NOC_ENGINEER).create();
    BodRole role2 = new BodRoleFactory().setRole(Security.RoleEnum.ICT_MANAGER).create();
    BodRole role3 = new BodRoleFactory().setRole(Security.RoleEnum.ICT_MANAGER).create();
    BodRole role4 = new BodRoleFactory().setRole(Security.RoleEnum.USER).create();

    RichUserDetails userDetails = new RichUserDetailsFactory().create();
    userDetails.addBodRoles(Lists.newArrayList(role4, role3, role2, role1));

    userDetails.switchRoleTo(role3);
    assertThat(userDetails.getSelectedRole(), is(role3));

    java.util.List<BodRole> sortedRoles = userDetails.getBodRoles();
    assertThat(sortedRoles, hasSize(3));

    // Verify by sortOrder in enum
    assertThat(sortedRoles.get(0), is(role1));
    assertThat(sortedRoles.get(1), is(role2));
    assertThat(sortedRoles.get(2), is(role4));
  }

  @Test
  public void shouldSortRolesTestOnInstituteName() {
    Institute firstInstitute = new InstituteFactory().setName("AAA").create();
    Institute lastInstitute = new InstituteFactory().setName("ZZZ").create();
    PhysicalResourceGroup firstGroup = new PhysicalResourceGroupFactory().setInstitute(firstInstitute).create();
    PhysicalResourceGroup lastGroup = new PhysicalResourceGroupFactory().setInstitute(lastInstitute).create();

    BodRole role1 = new BodRoleFactory().setRole(Security.RoleEnum.NOC_ENGINEER).create();
    BodRole role2 = new BodRoleFactory().setRole(Security.RoleEnum.ICT_MANAGER).setPhysicalResourceGroup(firstGroup)
        .create();
    BodRole role3 = new BodRoleFactory().setRole(Security.RoleEnum.ICT_MANAGER).setPhysicalResourceGroup(lastGroup)
        .create();
    BodRole role4 = new BodRoleFactory().setRole(Security.RoleEnum.USER).setPhysicalResourceGroup(firstGroup).create();

    RichUserDetails userDetails = new RichUserDetailsFactory().setSelectedRole(null).create();
    userDetails.addBodRoles(Lists.newArrayList(role4, role3, role2, role1));

    userDetails.switchRoleTo(role1);
    assertThat(userDetails.getSelectedRole(), is(role1));

    java.util.List<BodRole> sortedRoles = userDetails.getBodRoles();
    assertThat(sortedRoles, hasSize(3));

    // Verify by sortOrder in enum
    assertThat(sortedRoles.get(0), is(role2));
    assertThat(sortedRoles.get(1), is(role3));
    assertThat(sortedRoles.get(2), is(role4));
  }

}
