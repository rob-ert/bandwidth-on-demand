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
package nl.surfnet.bod.web.security;

import static com.google.common.base.Strings.nullToEmpty;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.*;
import nl.surfnet.bod.repo.BodAccountRepo;
import nl.surfnet.bod.service.GroupService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.service.VirtualResourceGroupService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class RichUserDetailsService implements AuthenticationUserDetailsService<Authentication> {

  private final Logger logger = LoggerFactory.getLogger(RichUserDetailsService.class);

  @Value("${os.group.noc}")
  private String nocEngineerGroupId;

  @Resource
  private GroupService groupService;

  @Resource
  private PhysicalResourceGroupService physicalResourceGroupService;

  @Resource
  private VirtualResourceGroupService virtualResourceGroupService;

  @Resource
  private BodAccountRepo bodAccountRepo;

  @Override
  @Transactional
  public RichUserDetails loadUserDetails(Authentication token) {
    RichPrincipal principal = (RichPrincipal) token.getPrincipal();
    Collection<UserGroup> groups = groupService.getGroups(principal.getNameId());

    logger.debug("Found groups: '{}' for name-id: '{}'", groups, principal.getNameId());

    updateVirtualResourceGroups(groups);
    createAccountIfNotExists(principal.getNameId());

    Collection<BodRole> roles = determineRoles(groups);

    RichUserDetails userDetails = new RichUserDetails(principal.getNameId(), principal.getDisplayName(),
        principal.getEmail(), groups, roles, principal.getNsiScopes());

    return userDetails;
  }

  private void createAccountIfNotExists(String nameId) {
    BodAccount account = bodAccountRepo.findByNameId(nameId);
    if (account == null) {
      account = new BodAccount();
      account.setNameId(nameId);
      bodAccountRepo.save(account);
    }
  }

  /**
   * Determines for which userGroups there is a {@link PhysicalResourceGroup}
   * related and creates a {@link BodRole} based on that information.
   *
   * @param Collection
   *          <UserGroup> groups to process
   * @return List<BodRole> List with roles sorted on the roleName
   */
  Collection<BodRole> determineRoles(Collection<UserGroup> userGroups) {
    Collection<BodRole> roles = Lists.newArrayList();

    roles.addAll(determineNocRole(userGroups));
    roles.addAll(determineUserRole(userGroups));
    roles.addAll(determineManagerRoles(userGroups));

    return roles;
  }

  private Collection<BodRole> determineNocRole(Collection<UserGroup> userGroups) {
    return isNocEngineerGroup(userGroups) ? ImmutableList.of(BodRole.createNocEngineer()) : Collections
        .<BodRole> emptyList();
  }

  private Collection<BodRole> determineManagerRoles(Collection<UserGroup> userGroups) {
    Collection<UserGroup> managerUserGroups = Collections2.filter(userGroups, new Predicate<UserGroup>() {
      @Override
      public boolean apply(UserGroup userGroup) {
        return isIctManager(userGroup);
      }
    });

    Collection<BodRole> roles = Lists.newArrayList();

    for (UserGroup userGroup : managerUserGroups) {
      List<PhysicalResourceGroup> prgs = physicalResourceGroupService.findByAdminGroup(userGroup.getId());
      for (PhysicalResourceGroup prg : prgs) {
        roles.add(BodRole.createManager(prg));
      }
    }

    return roles;
  }

  private Collection<BodRole> determineUserRole(Collection<UserGroup> userGroups) {
    BodRole userRole = isUser(userGroups) ? BodRole.createUser() : BodRole.createNewUser();

    return ImmutableList.of(userRole);
  }

  private void updateVirtualResourceGroups(Collection<UserGroup> userGroups) {
    for (UserGroup userGroup : userGroups) {
      VirtualResourceGroup vrg = virtualResourceGroupService.findByAdminGroup(userGroup.getId());

      if (vrg == null) {
        continue;
      }

      if (!vrg.getName().equals(userGroup.getName())
          || !nullToEmpty(vrg.getDescription()).equals(nullToEmpty(userGroup.getDescription()))) {
        logger.info(
            "Updating virtualResourceGroup ({} -> {}) ({} -> {})",
            new Object[] {vrg.getName(), userGroup.getName(), vrg.getDescription(), userGroup.getDescription()});
        vrg.setDescription(userGroup.getDescription());
        vrg.setName(userGroup.getName());

        virtualResourceGroupService.update(vrg);
      }
    }
  }

  private boolean isIctManager(UserGroup group) {
    return physicalResourceGroupService.hasRelatedPhysicalResourceGroup(group);
  }

  public boolean isUser(Collection<UserGroup> groups) {
    return !virtualResourceGroupService.findByUserGroups(groups).isEmpty();
  }

  private boolean isNocEngineerGroup(Collection<UserGroup> groups) {
    return Iterables.any(groups, new Predicate<UserGroup>() {
      @Override
      public boolean apply(UserGroup group) {
        return nocEngineerGroupId.equals(group.getId());
      }
    });
  }

  protected void setNocEngineerGroupId(String groupId) {
    this.nocEngineerGroupId = groupId;
  }

  String getNocEngineerGroupId() {
    return nocEngineerGroupId;
  }
}
