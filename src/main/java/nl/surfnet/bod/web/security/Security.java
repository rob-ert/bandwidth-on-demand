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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.UniPort;
import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.domain.oauth.NsiScope;

import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public final class Security {

  public enum RoleEnum {
    NOC_ENGINEER("redirect:noc", 1),
    APP_MANAGER("redirect:appmanager", 2),
    ICT_MANAGER("redirect:manager", 3),
    USER("redirect:user", 4),
    NEW_USER("redirect:user", 5);

    private String viewName;
    private int sortOrder;

    private RoleEnum(String viewName, int sortOrder) {
      this.viewName = viewName;
      this.sortOrder = sortOrder;
    }

    public String getViewName() {
      return viewName;
    }

    public int getSortOrder() {
      return sortOrder;
    }
  }

  private Security() {
  }

  public static RichUserDetails getUserDetails() {
    Optional<Authentication> authentication = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());

    return authentication.map(a -> (RichUserDetails) a.getPrincipal()).orElse(null);
  }

  public static UserGroup getUserGroup(final String groupId) {
    Preconditions.checkNotNull(groupId);

    Collection<UserGroup> groups = getUserDetails().getUserGroups();
    return Iterables.find(groups, new Predicate<UserGroup>() {
      @Override
      public boolean apply(UserGroup group) {
        return group.getId().equals(groupId);
      }
    }, null);
  }

  public static String getNameId() {
    return getUserDetails().getNameId();
  }

  public static void switchToUser() {
    getUserDetails().switchToUser();
  }

  public static void switchToManager() {
    getUserDetails().trySwitchToManager();
  }

  public static void switchToManager(PhysicalResourceGroup prg) {
    getUserDetails().switchToManager(prg);
  }

  public static void switchToNocEngineer() {
    getUserDetails().trySwitchToNoc();
  }

  public static void switchToAppManager() {
    getUserDetails().trySwitchToAppManager();
  }

  public static boolean isUserMemberOf(String groupId) {
    return getUserDetails().isMemberOf(groupId);
  }

  public static boolean isUserNotMemberOf(String groupId) {
    return !isUserMemberOf(groupId);
  }

  public static boolean isSelectedUserRole() {
    return getUserDetails().isSelectedUserRole();
  }

  public static BodRole getSelectedRole() {
    return getUserDetails().getSelectedRole();
  }

  public static boolean isSelectedManagerRole() {
    return getUserDetails().isSelectedManagerRole();
  }

  public static boolean isSelectedNocRole() {
    return getUserDetails().isSelectedNocRole();
  }

  public static boolean isSelectedAppManagerRole() {
    return getUserDetails().isSelectedAppManagerRole();
  }

  public static boolean hasUserRole() {
    return getUserDetails().hasUserRole();
  }

  public static boolean hasOauthScope(NsiScope scope) {
    return getUserDetails().getNsiScopes().contains(scope);
  }

  public static boolean isUserMemberOf(VirtualResourceGroup group) {
    return group == null ? false : isUserMemberOf(group.getAdminGroup());
  }

  public static boolean isManagerMemberOf(PhysicalResourceGroup group) {
    return group == null ? false : isUserMemberOf(group.getAdminGroup());
  }

  public static boolean userMayEdit(VirtualPort virtualPort) {
    return virtualPort == null ? false : isUserMemberOf(virtualPort.getVirtualResourceGroup());
  }

  public static boolean userMayNotEdit(VirtualPort virtualPort) {
    return !userMayEdit(virtualPort);
  }

  public static boolean managerMayEdit(UniPort port) {
    return port == null ? false : isManagerMemberOf(port.getPhysicalResourceGroup());
  }

  public static boolean managerMayNotEdit(UniPort port) {
    return !managerMayEdit(port);
  }

  public static boolean managerMayEdit(PhysicalResourceGroup group) {
    return isManagerMemberOf(group);
  }

  public static boolean managerMayEdit(VirtualPort virtualPort) {
    if (virtualPort.getPhysicalPort() == null || virtualPort.getPhysicalPort().getPhysicalResourceGroup() == null) {
      return false;
    }
    return isManagerMemberOf(virtualPort.getPhysicalPort().getPhysicalResourceGroup());
  }

  public static boolean managerMayNotEdit(VirtualPort virtualPort) {
    return !managerMayEdit(virtualPort);
  }

  /**
   * Set the current logged in user. (Should only be used from tests).
   *
   * @param richUserDetails
   *          the user details
   */
  public static void setUserDetails(RichUserDetails richUserDetails) {
    checkNotNull(richUserDetails);

    RunAsUserToken authentication = new RunAsUserToken("A Run As User", richUserDetails, "N/A", Collections
        .<GrantedAuthority> emptyList(), PreAuthenticatedAuthenticationToken.class);

    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  public static void clearUserDetails() {
    SecurityContextHolder.getContext().setAuthentication(null);
  }

}
