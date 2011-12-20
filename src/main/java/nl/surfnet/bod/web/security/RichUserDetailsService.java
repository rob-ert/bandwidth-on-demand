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

import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.service.GroupService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class RichUserDetailsService implements AuthenticationUserDetailsService {

  private final Logger logger = LoggerFactory.getLogger(RichUserDetailsService.class);

  @Value("${os.group.noc}")
  private String nocEngineerGroupId;

  @Autowired
  private GroupService groupService;

  @Override
  public RichUserDetails loadUserDetails(Authentication token) throws UsernameNotFoundException {
    RichPrincipal principal = (RichPrincipal) token.getPrincipal();

    Collection<UserGroup> groups = groupService.getGroups(principal.getNameId());
    logger.debug("Found groups: '{}' for name-id: '{}'", groups, principal.getNameId());

    List<GrantedAuthority> authorities = Lists.newArrayList();
    if (containsNocEngineerGroup(groups)) {
      authorities.add(new GrantedAuthority() {
        @Override
        public String getAuthority() {
          return "NOC_ENGINEER";
        }
      });
    }

    return new RichUserDetails(principal.getNameId(), principal.getDisplayName(), authorities);
  }

  private boolean containsNocEngineerGroup(Collection<UserGroup> groups) {
    return Iterables.any(groups, new Predicate<UserGroup>() {
      @Override
      public boolean apply(UserGroup group) {
        return nocEngineerGroupId.equals(group.getId());
      }
    });
  }

  protected void setGroupService(GroupService groupService) {
    this.groupService = groupService;
  }

  protected void setNocEngineerGroupId(String groupId) {
    this.nocEngineerGroupId = groupId;
  }
}
