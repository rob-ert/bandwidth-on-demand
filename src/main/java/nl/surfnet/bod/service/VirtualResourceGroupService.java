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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.repo.VirtualResourceGroupRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

@Service
@Transactional
public class VirtualResourceGroupService {

  @Autowired
  private VirtualResourceGroupRepo virtualResourceGroupRepo;

  @Autowired
  private GroupService groupService;

  public long count() {
    return virtualResourceGroupRepo.count();
  }

  public void delete(final VirtualResourceGroup virtualResourceGroup) {
    virtualResourceGroupRepo.delete(virtualResourceGroup);
  }

  public VirtualResourceGroup find(final Long id) {
    return virtualResourceGroupRepo.findOne(id);
  }

  public List<VirtualResourceGroup> findAll() {
    return virtualResourceGroupRepo.findAll();
  }

  public List<VirtualResourceGroup> findEntries(final int firstResult, final int maxResults) {
    return virtualResourceGroupRepo.findAll(new PageRequest(firstResult / maxResults, maxResults)).getContent();
  }

  public void save(final VirtualResourceGroup virtualResourceGroup) {
    virtualResourceGroupRepo.save(virtualResourceGroup);
  }

  public VirtualResourceGroup update(final VirtualResourceGroup virtualResourceGroup) {
    return virtualResourceGroupRepo.save(virtualResourceGroup);
  }

  public VirtualResourceGroup findByName(String name) {
    return virtualResourceGroupRepo.findByName(name);
  }

  public VirtualResourceGroup findBySurfConnextGroupName(String surfConnextGroupName) {
    return virtualResourceGroupRepo.findBySurfConnextGroupName(surfConnextGroupName);
  }

  public Collection<VirtualResourceGroup> findAllForUser(String nameId) {
    Collection<VirtualResourceGroup> virtualResourceGroups = new ArrayList<VirtualResourceGroup>();
    Collection<UserGroup> groups = groupService.getGroups(nameId);

    Collection<String> adminGroups = Lists.newArrayList(Collections2.transform(groups,
        new Function<UserGroup, String>() {
          @Override
          public String apply(UserGroup group) {
            return group.getId();
          }
        }));

    if (!CollectionUtils.isEmpty(adminGroups)) {
      virtualResourceGroups = virtualResourceGroupRepo.findBySurfConnextGroupNameIn(adminGroups);
    }

    return virtualResourceGroups;
  }

  void setGroupService(GroupService groupService) {
    this.groupService = groupService;
  }

  void setVirtualResourceGroupRepo(VirtualResourceGroupRepo virtualResourceGroupRepo) {
    this.virtualResourceGroupRepo = virtualResourceGroupRepo;
  }

}
