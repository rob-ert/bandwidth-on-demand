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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import nl.surfnet.bod.domain.ActivationEmailLink;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.PhysicalResourceGroup_;
import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.repo.ActivationEmailLinkRepo;
import nl.surfnet.bod.repo.PhysicalResourceGroupRepo;
import nl.surfnet.bod.web.security.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.base.Optional;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newArrayList;

@Service
@Transactional
public class PhysicalResourceGroupService extends AbstractFullTextSearchService<PhysicalResourceGroup> {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Resource
  private PhysicalResourceGroupRepo physicalResourceGroupRepo;

  @Resource
  private ActivationEmailLinkRepo activationEmailLinkRepo;

  @Resource
  private EmailSender emailSender;

  @Resource
  private LogEventService logEventService;

  @PersistenceContext
  private EntityManager entityManager;

  public long count() {
    return physicalResourceGroupRepo.count();
  }

  public void delete(final long primaryKey) {
    physicalResourceGroupRepo.delete(primaryKey);
  }

  public PhysicalResourceGroup find(final Long id) {
    return physicalResourceGroupRepo.findOne(id);
  }

  public List<PhysicalResourceGroup> findAll() {
    return physicalResourceGroupRepo.findAll();
  }

  public List<PhysicalResourceGroup> findEntries(int firstResult, int maxResults, Sort sort) {
    return physicalResourceGroupRepo.findAll(new PageRequest(firstResult / maxResults, maxResults, sort)).getContent();
  }

  public void save(final PhysicalResourceGroup physicalResourceGroup) {
    physicalResourceGroupRepo.save(physicalResourceGroup);

    //Log event after creation, so the ID is set by hibernate
    logEventService.logCreateEvent(Security.getUserDetails(), physicalResourceGroup);
  }

  public PhysicalResourceGroup update(final PhysicalResourceGroup physicalResourceGroup) {
    logEventService.logUpdateEvent(Security.getUserDetails(), physicalResourceGroup);
    return physicalResourceGroupRepo.save(physicalResourceGroup);
  }

  public boolean hasRelatedPhysicalResourceGroup(UserGroup group) {
    return (group != null) && (physicalResourceGroupRepo.findByAdminGroup(group.getId()) != null);
  }

  public Collection<PhysicalResourceGroup> findAllForAdminGroups(Collection<UserGroup> groups) {
    if (groups.isEmpty()) {
      return Collections.emptyList();
    }

    Collection<String> groupIds = newArrayList(transform(groups, new Function<UserGroup, String>() {
      @Override
      public String apply(UserGroup group) {
        return group.getId();
      }
    }));

    return physicalResourceGroupRepo.findByAdminGroupIn(groupIds);
  }

  public List<PhysicalResourceGroup> findByAdminGroup(String groupId) {
    return physicalResourceGroupRepo.findByAdminGroup(groupId);
  }

  @SuppressWarnings("unchecked")
  public ActivationEmailLink<PhysicalResourceGroup> findActivationLink(String uuid) {
    ActivationEmailLink<PhysicalResourceGroup> activationEmailLink = (ActivationEmailLink<PhysicalResourceGroup>) activationEmailLinkRepo
        .findByUuid(uuid);

    if (activationEmailLink != null) {
      activationEmailLink.setSourceObject(find(activationEmailLink.getSourceId()));
    }

    return activationEmailLink;
  }

  public Collection<PhysicalResourceGroup> findAllWithPorts() {
    Specification<PhysicalResourceGroup> withPhysicalPorts = new Specification<PhysicalResourceGroup>() {
      @Override
      public Predicate toPredicate(Root<PhysicalResourceGroup> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.isNotEmpty(root.get(PhysicalResourceGroup_.physicalPorts));
      }
    };

    return physicalResourceGroupRepo.findAll(withPhysicalPorts);
  }

  public void activate(ActivationEmailLink<PhysicalResourceGroup> activationEmailLink) {
    log.info("Activating link [{}] for physical resource group: {}", activationEmailLink.getUuid(), activationEmailLink
        .getSourceObject().getName());
    activationEmailLink.activate();
    activationEmailLink.getSourceObject().setActive(true);

    activationEmailLinkRepo.save(activationEmailLink);
    update(activationEmailLink.getSourceObject());

    //Log event after creation, so the ID is set by hibernate
    logEventService.logCreateEvent(Security.getUserDetails(), activationEmailLink, "Activating link");
  }

  @Transactional
  public ActivationEmailLink<PhysicalResourceGroup> sendActivationRequest(PhysicalResourceGroup physicalResourceGroup) {
    deActivatePhysicalResourceGroup(physicalResourceGroup);

    ActivationEmailLink<PhysicalResourceGroup> link = activationEmailLinkRepo
        .save(new ActivationEmailLink<PhysicalResourceGroup>(physicalResourceGroup));

    emailSender.sendActivationMail(link);

    link.emailWasSent();
    return activationEmailLinkRepo.save(link);
  }

  private void deActivatePhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup) {
    physicalResourceGroup.setActive(false);
    physicalResourceGroupRepo.save(physicalResourceGroup);
  }

  public List<Long> findAllIds() {
    return physicalResourceGroupRepo.findIdsWithWhereClause(Optional.<Specification<PhysicalResourceGroup>> absent());
  }

  @Override
  protected EntityManager getEntityManager() {
    return entityManager;
  }

}