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

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import nl.surfnet.bod.domain.*;
import nl.surfnet.bod.repo.VirtualResourceGroupRepo;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;

@Service
@Transactional
public class VirtualResourceGroupService {

  @Resource
  private VirtualResourceGroupRepo virtualResourceGroupRepo;

  @Resource
  private LogEventService logEventService;

  public long count() {
    return virtualResourceGroupRepo.count();
  }

  public long countForManager(BodRole managerRole) {
    checkArgument(managerRole.isManagerRole());

    return virtualResourceGroupRepo.count(specificationForManager(managerRole));
  }

  public void delete(final VirtualResourceGroup virtualResourceGroup) {
    logEventService.logDeleteEvent(Security.getUserDetails(), virtualResourceGroup);
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

  public List<VirtualResourceGroup> findEntriesForManager(BodRole managerRole, int firstResult, int maxResults,
      Sort sort) {
    checkArgument(maxResults > 0);
    checkArgument(managerRole.isManagerRole());

    return virtualResourceGroupRepo.findAll(specificationForManager(managerRole),
        new PageRequest(firstResult / maxResults, maxResults, sort)).getContent();
  }

  private Specification<VirtualResourceGroup> specificationForManager(final BodRole managerRole) {
    return new Specification<VirtualResourceGroup>() {
      @Override
      public javax.persistence.criteria.Predicate toPredicate(Root<VirtualResourceGroup> root, CriteriaQuery<?> query,
          CriteriaBuilder cb) {

        Subquery<Long> subquery = query.subquery(Long.class);
        Root<VirtualPort> subRoot = subquery.from(VirtualPort.class);
        subquery.select(subRoot.get(VirtualPort_.virtualResourceGroup).get(VirtualResourceGroup_.id));
        subquery.where(cb.equal(
            subRoot.get(VirtualPort_.physicalPort).get(PhysicalPort_.physicalResourceGroup)
                .get(PhysicalResourceGroup_.id), managerRole.getPhysicalResourceGroupId().get()));

        return cb.in(root.get(VirtualResourceGroup_.id)).value(subquery);
      }
    };
  }

  public void save(final VirtualResourceGroup virtualResourceGroup) {
    logEventService.logCreateEvent(Security.getUserDetails(), virtualResourceGroup);
    virtualResourceGroupRepo.save(virtualResourceGroup);
  }

  public VirtualResourceGroup update(final VirtualResourceGroup virtualResourceGroup) {
    logEventService.logUpdateEvent(Security.getUserDetails(), virtualResourceGroup);
    return virtualResourceGroupRepo.save(virtualResourceGroup);
  }

  public VirtualResourceGroup findByName(String name) {
    return virtualResourceGroupRepo.findByName(name);
  }

  public VirtualResourceGroup findBySurfconextGroupId(String surfconextGroupId) {
    return virtualResourceGroupRepo.findBySurfconextGroupId(surfconextGroupId);
  }

  public Collection<VirtualResourceGroup> findAllForUser(RichUserDetails user) {
    Collection<String> groups = user.getUserGroupIds();

    return findBySurfconextGroupId(groups);
  }

  public Collection<VirtualResourceGroup> findByUserGroups(Collection<UserGroup> groups) {
    if (groups.isEmpty()) {
      return Collections.emptyList();
    }

    return findBySurfconextGroupId(newArrayList(transform(groups, new Function<UserGroup, String>() {
      @Override
      public String apply(UserGroup group) {
        return group.getId();
      }
    })));
  }

  private Collection<VirtualResourceGroup> findBySurfconextGroupId(Collection<String> groupIds) {
    if (groupIds.isEmpty()) {
      return Collections.emptyList();
    }

    return virtualResourceGroupRepo.findBySurfconextGroupIdIn(groupIds);
  }

}
