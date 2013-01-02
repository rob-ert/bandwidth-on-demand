/**
 * Copyright (c) 2012, SURFnet BV
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
package nl.surfnet.bod.support;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.domain.oauth.NsiScope;
import nl.surfnet.bod.web.security.RichUserDetails;

import com.google.common.collect.Lists;

public class RichUserDetailsFactory {

  public static final String MANAGER_GROUP_ID = "urn:manager";

  private String username = "urn:guest:truus";
  private String displayName = "Truus Visscher";
  private String email = "truus@example.com";
  private Collection<UserGroup> userGroups = Lists.newArrayList();
  private List<BodRole> bodRoles = Lists.newArrayList();
  private Collection<NsiScope> scopes = Lists.newArrayList();

  public RichUserDetails create() {
    if (bodRoles.isEmpty()) {
      // should always have one role
      bodRoles.add(BodRole.createNewUser());
    }

    RichUserDetails userDetails = new RichUserDetails(username, displayName, email, userGroups, bodRoles, scopes);

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

  public RichUserDetailsFactory setScopes(Collection<NsiScope> scopes) {
    this.scopes = scopes;
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
