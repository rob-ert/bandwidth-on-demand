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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.web.security.Security.RoleEnum;

import org.slf4j.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

@SuppressWarnings("serial")
public class RichUserDetails implements UserDetails {

  private static final Ordering<BodRole> SORT_BY_SORT_ORDER_AND_INSTITUTE_NAME = Ordering
      .from(new Comparator<BodRole>() {
        @Override
        public int compare(BodRole role1, BodRole role2) {
          return (String.valueOf(role1.getRole().getSortOrder()) + role1.getInstituteName()).compareTo(String
              .valueOf(role2.getRole().getSortOrder()) + role2.getInstituteName());
        }
      });

  private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());

  private final String username;
  private final String displayName;
  private final String email;
  private final Collection<UserGroup> userGroups;
  private Collection<GrantedAuthority> authorities;
  private List<BodRole> bodRoles = Lists.newArrayList();
  private BodRole selectedRole;

  public RichUserDetails(String username, String displayName, String email, Collection<UserGroup> userGroups) {
    this.username = username;
    this.displayName = displayName;
    this.userGroups = userGroups;
    this.email = email;
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

  public List<BodRole> getBodRoles() {
    return bodRoles;
  }

  /**
   * 
   * @return All {@link #bodRoles} and the {@link #selectedRole} if not null
   */
  public List<BodRole> getAllBodRoles() {
    ArrayList<BodRole> allRoles = Lists.newArrayList();

    allRoles.addAll(bodRoles);

    if (selectedRole != null) {
      allRoles.add(selectedRole);
    }

    return allRoles;
  }

  public void setBodRoles(List<BodRole> roles) {
    this.bodRoles = roles;

    sortRoles();
  }

  public void setSelectedRole(BodRole role) {
    this.selectedRole = role;
  }

  public BodRole getSelectedRole() {
    return selectedRole;
  }

  @Override
  public Collection<GrantedAuthority> getAuthorities() {
    return authorities;
  }

  public void setAuthorities(List<GrantedAuthority> authorities) {
    this.authorities = authorities;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("nameId", getNameId()).add("displayName", getDisplayName())
        .add("bodRoles", bodRoles).toString();
  }

  public BodRole findBodRoleById(final Long bodRoleId) {

    BodRole foundRole = null;

    Collection<BodRole> filteredRoles = Collections2.filter(bodRoles, new Predicate<BodRole>() {
      @Override
      public boolean apply(BodRole input) {
        return input.getId() == bodRoleId;
      }
    });

    if (!CollectionUtils.isEmpty(filteredRoles)) {
      if (filteredRoles.size() == 1) {
        foundRole = filteredRoles.iterator().next();
      }
      else {
        throw new IllegalStateException("Multiple BodRoles found, while one expected for id: " + bodRoleId);
      }
    }
    else {
      log.warn("No role to switch to for id: {} ", bodRoleId);
    }
    return foundRole;
  }

  BodRole findFirstBodRoleByRole(RoleEnum role) {
    BodRole foundRole = null;

    for (BodRole bodRole : getAllBodRoles()) {
      if (bodRole.getRole() == role) {
        foundRole = bodRole;
        break;
      }
    }

    return foundRole;
  }

  /**
   * Switches the BodRole to the given role. Adds the previous selected rol back
   * to the list of roles and removes the newly selected role from the list. In
   * case the given roll is null, no actions are performed.
   * 
   * @param bodRole
   *          The role to switch to
   */
  public void switchRoleTo(BodRole bodRole) {

    if (bodRole == null) {
      return;
    }

    if ((selectedRole != null) && (!bodRoles.contains(selectedRole))) {
      bodRoles.add(selectedRole);
    }

    bodRoles.remove(bodRole);
    selectedRole = bodRole;

    sortRoles();
  }

  private void sortRoles() {
    bodRoles = SORT_BY_SORT_ORDER_AND_INSTITUTE_NAME.sortedCopy(bodRoles);
  }

  public void switchRoleToUser() {
    BodRole userRole = findFirstBodRoleByRole(RoleEnum.USER);
    switchRoleTo(userRole);
  }

  public void switchRoleToNocEngineer() {
    BodRole nocRole = findFirstBodRoleByRole(RoleEnum.NOC_ENGINEER);
    switchRoleTo(nocRole);
  }

  public void switchRoleToFirstManager() {
    BodRole managerRole = findFirstBodRoleByRole(RoleEnum.ICT_MANAGER);
    switchRoleTo(managerRole);
  }

}
