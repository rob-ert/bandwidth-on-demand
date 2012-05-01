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
package nl.surfnet.bod.support;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.web.security.RichUserDetails;

import com.google.common.collect.Lists;

public class RichUserDetailsFactory {

  private static final String MANAGER_GROUP_ID = "urn:manager";

  private String username = "urn:guest:truus";
  private String displayName = "Truus Visscher";
  private String email = "truus@example.com";
  private Collection<UserGroup> userGroups = Lists.newArrayList();
  private List<BodRole> bodRoles = Lists.newArrayList();

  public RichUserDetails create() {
    if (bodRoles.isEmpty()) {
      // should always have one role
      bodRoles.add(BodRole.createNewUser());
    }

    RichUserDetails userDetails = new RichUserDetails(username, displayName, email, userGroups, bodRoles);

    return userDetails;
  }

  public RichUserDetailsFactory addUserRole() {
    BodRole userRole = BodRole.createUser();
    bodRoles.add(userRole);
    return this;
  }

  public RichUserDetailsFactory addNocRole() {
    BodRole nocRole = BodRole.createNocEngineer();
    bodRoles.add(nocRole);
    return this;
  }

  public RichUserDetailsFactory addManagerRole() {
    PhysicalResourceGroup prg = new PhysicalResourceGroupFactory().setAdminGroup(MANAGER_GROUP_ID).create();

    BodRole managerRole = BodRole.createManager(prg);
    bodRoles.add(managerRole);

    return this;
  }

  public RichUserDetailsFactory addManagerRole(PhysicalResourceGroup group) {
    bodRoles.add(BodRole.createManager(group));

    return this;
  }

  public RichUserDetailsFactory addUserGroup(String groupId) {
    userGroups.add(new UserGroupFactory().setId(groupId).create());
    return this;
  }

  public RichUserDetailsFactory addUserGroup(UserGroup... users) {
    this.userGroups.addAll(Arrays.asList(users));

    return this;
  }

  public RichUserDetailsFactory setDisplayname(String name) {
    this.displayName = name;
    return this;
  }

  public RichUserDetailsFactory setEmail(String emailAddress) {
    this.email = emailAddress;
    return this;
  }

  public RichUserDetailsFactory setUsername(String name) {
    this.username = name;
    return this;
  }

  public RichUserDetailsFactory addBodRoles(BodRole... roles) {
    this.bodRoles.addAll(Arrays.asList(roles));

    return this;
  }

}
