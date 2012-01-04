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

import java.util.Collection;

import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.springframework.security.core.GrantedAuthority;

import com.google.common.collect.Lists;

public class RichUserDetailsFactory {

  private String username = "urn:guest:truus";
  private String displayName = "Truus Visscher";
  private Collection<GrantedAuthority> authorities = Lists.newArrayList();
  private Collection<UserGroup> userGroups = Lists.newArrayList();

  public RichUserDetails create() {
    return new RichUserDetails(username, displayName, authorities, userGroups);
  }

  public RichUserDetailsFactory addUserGroup(String groupId) {
    userGroups.add(new UserGroupFactory().setId(groupId).create());
    return this;
  }

  public RichUserDetailsFactory setNameId(String username) {
    this.username = username;
    return this;
  }

}
