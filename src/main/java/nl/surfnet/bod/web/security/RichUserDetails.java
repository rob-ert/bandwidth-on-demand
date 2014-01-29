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

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.domain.oauth.NsiScope;
import nl.surfnet.bod.util.Orderings;
import nl.surfnet.bod.web.security.Security.RoleEnum;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.google.common.base.*;
import com.google.common.collect.*;

@SuppressWarnings("serial")
public class RichUserDetails implements UserDetails {

  private final String username;
  private final String displayName;
  private final Optional<InternetAddress> email;
  private final String providedEmail;
  private final Collection<UserGroup> userGroups;
  private final List<BodRole> bodRoles;
  private final List<NsiScope> nsiScopes;

  private BodRole selectedRole;

  public RichUserDetails(String username, String displayName, String emailString, Collection<UserGroup> userGroups,
      Collection<BodRole> roles, Collection<NsiScope> nsiScopes) {
    Preconditions.checkArgument(roles.size() > 0, "A user should at least have one role");

    this.username = username;
    this.displayName = displayName;
    this.userGroups = userGroups;
    this.providedEmail = emailString;
    this.email = validateEmail(emailString);
    this.nsiScopes = Lists.newArrayList(nsiScopes);

    bodRoles = Orderings.bodRoleOrdering().sortedCopy(roles);

    switchToRole(bodRoles.get(0));
  }

  private Optional<InternetAddress> validateEmail(String email) {
    if (Strings.isNullOrEmpty(email)) {
      return Optional.absent();
    }

    Optional<InternetAddress> emailOpt;
    try {
      InternetAddress[] addresses = InternetAddress.parse(email);

      if (addresses.length == 1) {
        emailOpt = Optional.of(addresses[0]);
      } else {
        emailOpt = Optional.absent();
      }
    } catch (AddressException e) {
      emailOpt = Optional.absent();
    }

    return emailOpt;
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

  public Optional<InternetAddress> getEmail() {
    return email;
  }

  public String getProvidedEmail() {
    return providedEmail;
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

  public List<BodRole> getManagerRoles() {
    return FluentIterable.from(getBodRoles()).filter(new Predicate<BodRole>() {
      @Override
      public boolean apply(BodRole bodRole) {
        return bodRole.getRole() == RoleEnum.ICT_MANAGER;
      }
    }).toList();
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
          return new SimpleGrantedAuthority(RoleEnum.USER.name());
        }
        return new SimpleGrantedAuthority(role.getRoleName());
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

  public boolean isSelectedUserRole() {
    return isSelectedRole(selectedRole, RoleEnum.USER);
  }

  public boolean isSelectedManagerRole() {
    return isSelectedRole(selectedRole, RoleEnum.ICT_MANAGER);
  }

  public boolean isSelectedNocRole() {
    return isSelectedRole(selectedRole, RoleEnum.NOC_ENGINEER);
  }

  public boolean isSelectedAppManagerRole() {
    return isSelectedRole(selectedRole, RoleEnum.APP_MANAGER);
  }

  public void switchToManager(PhysicalResourceGroup physicalResourceGroup) {
    for (BodRole bodRole : bodRoles) {
      if ((bodRole.getRole() == RoleEnum.ICT_MANAGER)
          && (physicalResourceGroup.getId().equals(bodRole.getPhysicalResourceGroupId().get()))) {

        switchToRole(bodRole);
        return;
      }
    }
  }

  public void trySwitchToManager() {
    BodRole managerRole = findFirstBodRoleByRole(RoleEnum.ICT_MANAGER);
    if (managerRole != null) {
      switchToRole(managerRole);
    }
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

  public void trySwitchToAppManager() {
    BodRole appManagerRole = findFirstBodRoleByRole(RoleEnum.APP_MANAGER);
    if (appManagerRole != null) {
      switchToRole(appManagerRole);
    }
  }

  public void switchToUser() {
    BodRole userRole = findFirstBodRoleByRole(RoleEnum.USER);

    if (userRole == null) {
      userRole = findFirstBodRoleByRole(RoleEnum.NEW_USER);
    }

    switchToRole(userRole);
  }

  public boolean isMemberOf(String groupId) {
    return getUserGroupIds().contains(groupId);
  }

  public boolean hasUserRole() {
    return findFirstBodRoleByRole(RoleEnum.USER) != null;
  }

  public List<NsiScope> getNsiScopes() {
    return nsiScopes;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("nameId", getNameId()).add("displayName", getDisplayName()).add("bodRoles",
        bodRoles).add("selectedRole", selectedRole).toString();
  }

  private boolean isSelectedRole(BodRole currentRole, RoleEnum role) {
    return currentRole != null ? currentRole.getRole() == role : false;
  }

}
