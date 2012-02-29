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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.repo.VirtualResourceGroupRepo;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;

@Service
@Transactional
public class VirtualResourceGroupService {

  @Autowired
  private VirtualResourceGroupRepo virtualResourceGroupRepo;

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

  public List<VirtualResourceGroup> findEntries(final int firstResult, final int maxResults, final Sort sort) {
    checkArgument(maxResults > 0);

    return virtualResourceGroupRepo.findAll(new PageRequest(firstResult / maxResults, maxResults, sort)).getContent();
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

  public VirtualResourceGroup findBySurfConextGroupName(String surfConextGroupName) {
    return virtualResourceGroupRepo.findBySurfConextGroupName(surfConextGroupName);
  }

  public Collection<VirtualResourceGroup> findAllForUser(RichUserDetails user) {
    Collection<String> groups = user.getUserGroupIds();

    return findBySurfConextGroupName(groups);
  }

  public Collection<VirtualResourceGroup> findByUserGroups(Collection<UserGroup> groups) {
    if (groups.isEmpty()) {
      return Collections.emptyList();
    }

    return findBySurfConextGroupName(newArrayList(transform(groups, new Function<UserGroup, String>() {
      @Override
      public String apply(UserGroup group) {
        return group.getId();
      }
    })));
  }

  private Collection<VirtualResourceGroup> findBySurfConextGroupName(Collection<String> groupIds) {
    return virtualResourceGroupRepo.findBySurfConextGroupNameIn(groupIds);
  }

}
