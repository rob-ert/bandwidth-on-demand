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
package nl.surfnet.bod.service;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.VirtualResourceGroup;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

import static com.google.common.base.Preconditions.checkArgument;

@Service
@Transactional(readOnly = true)
public class ManagerService {

  @Resource
  private VirtualResourceGroupService virtualResourceGroupService;

  /**
   * 
   * @param bodRole
   *          Role to search the adminGroups for
   * 
   * @return Set<String> set of all different admin groups from the role and the
   *         {@link VirtualResourceGroup}s which are related to the given role.
   */
  public Set<String> findAllAdminGroupsForManager(final BodRole bodRole) {
    checkArgument(bodRole.isManagerRole(), "Given role is not a manager: %s", bodRole);

    //Add group from role
    Set<String> adminGroups = Sets.newHashSet(bodRole.getAdminGroup().get());

    List<VirtualResourceGroup> vrgsForManager = virtualResourceGroupService.findEntriesForManager(bodRole);
    for (VirtualResourceGroup vrg : vrgsForManager) {
      adminGroups.add(vrg.getAdminGroup());
    }
    return adminGroups;
  }
}
