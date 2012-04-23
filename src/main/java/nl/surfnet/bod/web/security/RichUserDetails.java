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
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.util.Orderings;
import nl.surfnet.bod.web.security.Security.RoleEnum;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@SuppressWarnings("serial")
public class RichUserDetails implements UserDetails {

  private final String username;
  private final String displayName;
  private final String email;
  private final Collection<UserGroup> userGroups;
  private final List<BodRole> bodRoles;

  private BodRole selectedRole;

  public RichUserDetails(String username, String displayName, String email, Collection<UserGroup> userGroups,
      Collection<BodRole> roles) {
    this.username = username;
    this.displayName = displayName;
    this.userGroups = userGroups;
    this.email = email;

    bodRoles = Orderings.bodRoleOrdering().sortedCopy(roles);

    switchToRole(bodRoles.get(0));
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

  public List<BodRole> getSelectableRoles() {
    return Lists.newArrayList(Iterables.filter(bodRoles, new Predicate<BodRole>() {
      @Override
      public boolean apply(BodRole role) {
        return !role.equals(selectedRole);
      }
    }));
  }

  public List<BodRole> getBodRoles() {
    return bodRoles;
  }

  public BodRole getSelectedRole() {
    return selectedRole;
  }

  @Override
  public Collection<GrantedAuthority> getAuthorities() {
    return Sets.newHashSet(Collections2.transform(bodRoles, new Function<BodRole, GrantedAuthority>() {
      @Override
      public GrantedAuthority apply(BodRole role) {
        if (role.getRole() == RoleEnum.NEW_USER) {
          return new GrantedAuthorityImpl(RoleEnum.USER.name());
        }
        return new GrantedAuthorityImpl(role.getRoleName());
      }
    }));
  }

  private BodRole findBodRoleById(final Long bodRoleId) {
    return Iterables.find(bodRoles, new Predicate<BodRole>() {
      @Override
      public boolean apply(BodRole bodRole) {
        return bodRole.getId().equals(bodRoleId);
      }
    });
  }

  private BodRole findFirstBodRoleByRole(final RoleEnum role) {
    return Iterables.find(bodRoles, new Predicate<BodRole>() {
      @Override
      public boolean apply(BodRole bodRole) {
        return bodRole.getRole() == role;
      }
    }, null);
  }

  public void switchToManager(PhysicalResourceGroup physicalResourceGroup) {
    BodRole managerRole = null;

    for (BodRole bodRole : bodRoles) {
      if ((bodRole.getRole() == RoleEnum.ICT_MANAGER)
          && (physicalResourceGroup.getId().equals(bodRole.getPhysicalResourceGroupId()))) {

        managerRole = bodRole;
        break;
      }
    }

    switchToRole(managerRole);
  }

  public void switchToRoleById(Long bodRoleId) {
    BodRole bodRole = findBodRoleById(bodRoleId);
    switchToRole(bodRole);
  }

  private void switchToRole(BodRole bodRole) {
    Preconditions.checkNotNull(bodRole);

    selectedRole = bodRole;
  }

  public void trySwitchToNoc() {
    BodRole nocRole = findFirstBodRoleByRole(RoleEnum.NOC_ENGINEER);
    if (nocRole != null) {
      switchToRole(nocRole);
    }
  }

  public void switchToUser() {
    BodRole userRole = findFirstBodRoleByRole(RoleEnum.USER);

    if (userRole == null) {
      userRole = findFirstBodRoleByRole(RoleEnum.NEW_USER);
    }

    switchToRole(userRole);
  }

  public boolean hasUserRole() {
    return findFirstBodRoleByRole(RoleEnum.USER) != null;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("nameId", getNameId()).add("displayName", getDisplayName())
        .add("bodRoles", bodRoles).add("selectedRole", selectedRole).toString();
  }


}
