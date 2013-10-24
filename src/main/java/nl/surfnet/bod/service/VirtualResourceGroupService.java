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
package nl.surfnet.bod.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import nl.surfnet.bod.domain.*;
import nl.surfnet.bod.repo.VirtualResourceGroupRepo;
import nl.surfnet.bod.web.manager.VirtualResourceGroupController;
import nl.surfnet.bod.web.manager.VirtualResourceGroupController.VirtualResourceGroupView;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

@Service
@Transactional
public class VirtualResourceGroupService extends AbstractFullTextSearchService<VirtualResourceGroup> {

  public static final Function<VirtualResourceGroup, VirtualResourceGroupController.VirtualResourceGroupView> TO_MANAGER_VIEW =
    new Function<VirtualResourceGroup, VirtualResourceGroupController.VirtualResourceGroupView>() {
    @Override
    public VirtualResourceGroupController.VirtualResourceGroupView apply(VirtualResourceGroup group) {
      final Optional<Long> managersPrgId = Security.getSelectedRole().getPhysicalResourceGroupId();

      Integer count = FluentIterable.from(group.getVirtualPorts()).filter(new Predicate<VirtualPort>() {
        @Override
        public boolean apply(VirtualPort port) {
          return port.getPhysicalResourceGroup().getId().equals(managersPrgId.get());
        }
      }).size();

      return new VirtualResourceGroupController.VirtualResourceGroupView(group, count);
    }
  };

  public static final Function<VirtualResourceGroup, VirtualResourceGroupView> TO_VIEW =
    new Function<VirtualResourceGroup, VirtualResourceGroupView>() {
    @Override
    public VirtualResourceGroupView apply(VirtualResourceGroup input) {
      return new VirtualResourceGroupView(input, input.getVirtualPortCount());
    }
  };

  @Resource private VirtualResourceGroupRepo virtualResourceGroupRepo;
  @Resource private LogEventService logEventService;

  @PersistenceContext private EntityManager entityManager;

  public long count() {
    return virtualResourceGroupRepo.count();
  }

  public long countForManager(BodRole managerRole) {
    checkArgument(managerRole.isManagerRole());

    return virtualResourceGroupRepo.count(specificationForManager(managerRole));
  }

  public void delete(VirtualResourceGroup virtualResourceGroup) {
    logEventService.logDeleteEvent(Security.getUserDetails(), "", virtualResourceGroup);
    virtualResourceGroupRepo.delete(virtualResourceGroup);
  }

  public VirtualResourceGroup find(Long id) {
    return virtualResourceGroupRepo.findOne(id);
  }

  public List<VirtualResourceGroup> findAll() {
    return virtualResourceGroupRepo.findAll();
  }

  public List<VirtualResourceGroup> findEntries(int firstResult, int maxResults, Sort sort) {
    checkArgument(maxResults > 0);

    return virtualResourceGroupRepo.findAll(new PageRequest(firstResult / maxResults, maxResults, sort)).getContent();
  }

  public List<VirtualResourceGroup> findEntriesForManager(BodRole managerRole) {
    checkArgument(managerRole.isManagerRole(), "Given role is not a manager: %s", managerRole);

    return virtualResourceGroupRepo.findAll(specificationForManager(managerRole));
  }

  public List<VirtualResourceGroup> findEntriesForManager(BodRole managerRole, int firstResult, int maxResults,
      Sort sort) {
    checkArgument(maxResults > 0);
    checkArgument(managerRole.isManagerRole(), "Given role is not a manager: %s", managerRole);

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
        subquery.where(cb.equal(subRoot.get(VirtualPort_.physicalPort).get(UniPort_.physicalResourceGroup).get(
            PhysicalResourceGroup_.id), managerRole.getPhysicalResourceGroupId().get()));

        return cb.in(root.get(VirtualResourceGroup_.id)).value(subquery);
      }
    };
  }

  public void save(VirtualResourceGroup virtualResourceGroup) {
    virtualResourceGroupRepo.save(virtualResourceGroup);

    // Log event after creation, so the ID is set by hibernate
    logEventService.logCreateEvent(Security.getUserDetails(), virtualResourceGroup);
  }

  public VirtualResourceGroup update(VirtualResourceGroup virtualResourceGroup) {
    logEventService.logUpdateEvent(Security.getUserDetails(), "", virtualResourceGroup);
    return virtualResourceGroupRepo.save(virtualResourceGroup);
  }

  public VirtualResourceGroup findByName(String name) {
    return virtualResourceGroupRepo.findByName(name);
  }

  public VirtualResourceGroup findByAdminGroup(String adminGroup) {
    return virtualResourceGroupRepo.findByAdminGroup(adminGroup);
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

  public List<String> determineAdminGroupsForUser(RichUserDetails user) {

    return ImmutableList.copyOf(Collections2.filter(user.getUserGroupIds(), new Predicate<String>() {
      @Override
      public boolean apply(String groupId) {
        return findByAdminGroup(groupId) != null;
      }
    }));
  }

  private Collection<VirtualResourceGroup> findBySurfconextGroupId(Collection<String> groupIds) {
    if (groupIds.isEmpty()) {
      return Collections.emptyList();
    }

    return virtualResourceGroupRepo.findByAdminGroupIn(groupIds);
  }

  public List<Long> findAllTeamIds(Sort sort) {
    return virtualResourceGroupRepo.findIds(Optional.<Sort> fromNullable(sort));
  }

  public List<Long> findTeamIdsForRole(BodRole bodRole, Sort sort) {
    return virtualResourceGroupRepo.findIdsWithWhereClause(specificationForManager(bodRole), Optional.<Sort> fromNullable(sort));
  }

  @Override
  protected EntityManager getEntityManager() {
    return entityManager;
  }

}
