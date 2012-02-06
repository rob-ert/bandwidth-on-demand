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

import java.util.Collections;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.VirtualPort;

import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public final class Security {

  public static final String NOC_ENGINEER = "NOC_ENGINEER";
  public static final String ICT_MANAGER = "ICT_MANAGER";
  public static final String USER = "USER";

  private Security() {
  }

  public static RichUserDetails getUserDetails() {
    return (RichUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  public static boolean isUserMemberOf(String groupId) {
    return getUserDetails().getUserGroupIds().contains(groupId);
  }

  public static boolean isUserNotMemberOf(String groupId) {
    return !isUserMemberOf(groupId);
  }

  public static boolean userMayEdit(VirtualPort virtualPort) {
    return isUserMemberOf(virtualPort.getVirtualResourceGroup().getSurfConextGroupName());
  }

  public static boolean managerMayEdit(PhysicalPort port) {
    return isUserMemberOf(port.getPhysicalResourceGroup().getAdminGroup());
  }

  /**
   * Set the current logged in user. (Should only be used from tests).
   *
   * @param richUserDetails
   *          the user details
   */
  public static void setUserDetails(RichUserDetails richUserDetails) {
    RunAsUserToken authentication = new RunAsUserToken("A Run As User", richUserDetails, "N/A",
        Collections.<GrantedAuthority> emptyList(), PreAuthenticatedAuthenticationToken.class);

    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

}
