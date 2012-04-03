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

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.UserGroup;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

@SuppressWarnings("serial")
public class RichUserDetails implements UserDetails {

  private final String username;
  private final String displayName;
  private final String email;
  private final Collection<GrantedAuthority> authorities;
  private final Collection<UserGroup> userGroups;
  private final List<BodRole> bodRoles = Lists.newArrayList();
  private BodRole selectedRole;

  public RichUserDetails(String username, String displayName, String email, Collection<GrantedAuthority> authorities,
      Collection<UserGroup> userGroups) {
    this.username = username;
    this.displayName = displayName;
    this.authorities = authorities;
    this.userGroups = userGroups;
    this.email = email;
  }

  @Override
  public Collection<GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return "N/A";
  }

  @Override
  public String getUsername() {
    return username;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getNameId() {
    return getUsername();
  }

  public Collection<UserGroup> getUserGroups() {
    return userGroups;
  }

  public String getEmail() {
    return email;
  }

  public Collection<String> getUserGroupIds() {
    return newArrayList(transform(getUserGroups(), new Function<UserGroup, String>() {
      @Override
      public String apply(UserGroup group) {
        return group.getId();
      }
    }));
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public void addRole(BodRole bodRole) {
    this.bodRoles.add(bodRole);
  }

  public List<BodRole> getBodRoles() {
    return bodRoles;
  }

  public void setSelectedRole(BodRole role) {
    this.selectedRole = role;
  }

  public BodRole getSelectedRole() {
    return selectedRole;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("nameId", getNameId()).add("displayName", getDisplayName())
        .add("bodRoles", bodRoles).toString();
  }

}
