/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.service;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.VirtualResourceGroup;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

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
