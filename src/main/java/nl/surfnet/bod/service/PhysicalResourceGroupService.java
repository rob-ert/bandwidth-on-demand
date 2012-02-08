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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newArrayList;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
import nl.surfnet.bod.web.security.RichUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;

@Service
@Transactional
public class PhysicalResourceGroupService {

  @Autowired
  private InstituteService instituteService;

  @Autowired
  private PhysicalResourceGroupRepo physicalResourceGroupRepo;

  @Autowired
  private ActivationEmailLinkRepo activateEmailLinkRepo;

  // TODO @Autowired
  private MailSender mailSender;

  @Value("${activation.email.from}")
  private String fromAddress;

  public long count() {
    return physicalResourceGroupRepo.count();
  }

  public void delete(final PhysicalResourceGroup physicalResourceGroup) {
    physicalResourceGroupRepo.delete(physicalResourceGroup);
  }

  public PhysicalResourceGroup find(final Long id) {
    PhysicalResourceGroup physicalResourceGroup = physicalResourceGroupRepo.findOne(id);
    instituteService.fillInstituteForPhysicalResourceGroup(physicalResourceGroup);

    return physicalResourceGroup;
  }

  public List<PhysicalResourceGroup> findAll() {
    List<PhysicalResourceGroup> prgs = physicalResourceGroupRepo.findAll();

    instituteService.fillInstituteForPhysicalResourceGroups(prgs);

    return prgs;
  }

  public List<PhysicalResourceGroup> findEntries(final int firstResult, final int maxResults) {
    List<PhysicalResourceGroup> prgs = physicalResourceGroupRepo.findAll(
        new PageRequest(firstResult / maxResults, maxResults)).getContent();

    instituteService.fillInstituteForPhysicalResourceGroups(prgs);

    return prgs;
  }

  public List<PhysicalResourceGroup> findEntriesForManager(final RichUserDetails user, final int firstResult,
      final int maxResults) {
    checkNotNull(user);

    if (user.getUserGroups().isEmpty()) {
      return Collections.emptyList();
    }

    List<PhysicalResourceGroup> prgs = physicalResourceGroupRepo.findAll(forCurrentManager(user),
        new PageRequest(firstResult / maxResults, maxResults)).getContent();

    instituteService.fillInstituteForPhysicalResourceGroups(prgs);

    return prgs;
  }

  public long countForManager(RichUserDetails user) {
    return physicalResourceGroupRepo.count(forCurrentManager(user));
  }

  private Specification<PhysicalResourceGroup> forCurrentManager(final RichUserDetails user) {
    return new Specification<PhysicalResourceGroup>() {
      @Override
      public Predicate toPredicate(Root<PhysicalResourceGroup> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.and(root.get(PhysicalResourceGroup_.adminGroup).in(user.getUserGroupIds()));
      }
    };
  }

  public void save(final PhysicalResourceGroup physicalResourceGroup) {
    physicalResourceGroupRepo.save(physicalResourceGroup);
  }

  public PhysicalResourceGroup update(final PhysicalResourceGroup physicalResourceGroup) {
    return physicalResourceGroupRepo.save(physicalResourceGroup);
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

    List<PhysicalResourceGroup> prgs = physicalResourceGroupRepo.findByAdminGroupIn(groupIds);
    instituteService.fillInstituteForPhysicalResourceGroups(prgs);

    return prgs;
  }

  public Collection<PhysicalResourceGroup> findAllForManager(RichUserDetails user) {
    checkNotNull(user);

    if (user.getUserGroups().isEmpty()) {
      return Collections.emptyList();
    }

    List<PhysicalResourceGroup> groups = physicalResourceGroupRepo.findAll(forCurrentManager(user));

    instituteService.fillInstituteForPhysicalResourceGroups(groups);

    return groups;
  }

  public PhysicalResourceGroup findByInstituteId(Long instituteId) {
    PhysicalResourceGroup prg = physicalResourceGroupRepo.findByInstituteId(instituteId);

    instituteService.fillInstituteForPhysicalResourceGroup(prg);

    return prg;
  }

  @SuppressWarnings("unchecked")
  public ActivationEmailLink<PhysicalResourceGroup> findActivationLink(String uuid) {
    return (ActivationEmailLink<PhysicalResourceGroup>) activateEmailLinkRepo.findByUuid(uuid);
  }

  public Collection<PhysicalResourceGroup> findAllWithPorts() {
    Specification<PhysicalResourceGroup> withPhysicalPorts = new Specification<PhysicalResourceGroup>() {
      @Override
      public Predicate toPredicate(Root<PhysicalResourceGroup> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.isNotEmpty(root.get(PhysicalResourceGroup_.physicalPorts));
      }
    };

    List<PhysicalResourceGroup> groups = physicalResourceGroupRepo.findAll(withPhysicalPorts);
    instituteService.fillInstituteForPhysicalResourceGroups(groups);

    return groups;
  }

  public void activate(ActivationEmailLink<PhysicalResourceGroup> activationEmailLink) {
    activationEmailLink.activate();

    activateEmailLinkRepo.save(activationEmailLink);
    update(activationEmailLink.getSourceObject());
  }

  public ActivationEmailLink<PhysicalResourceGroup> sendAndPersistActivationRequest(
      PhysicalResourceGroup physicalResourceGroup, String userEmail) {

    ActivationEmailLink<PhysicalResourceGroup> activationEmailLink = new ActivationEmailLink<PhysicalResourceGroup>(
        physicalResourceGroup);
    activateEmailLinkRepo.save(activationEmailLink);

    SimpleMailMessage activationMessage = createActivationMessage(physicalResourceGroup, userEmail);

    try {
      mailSender.send(activationMessage);

      activationEmailLink.emailWasSent();
      activateEmailLinkRepo.save(activationEmailLink);
    }
    catch (MailException exc) {
      throw new RuntimeException(exc);
    }

    return activationEmailLink;
  }

  private SimpleMailMessage createActivationMessage(PhysicalResourceGroup physicalResourceGroup, String userEmail) {
    SimpleMailMessage activationMessage = new SimpleMailMessage();
    activationMessage.setTo(physicalResourceGroup.getManagerEmail());
    activationMessage.setFrom(fromAddress);
    activationMessage.setReplyTo(userEmail);
    activationMessage.setSubject("Activation mail for Physical Resource Group" + physicalResourceGroup.getName());

    URL activationUrl = generateActivationUrl(physicalResourceGroup);

    StringBuffer text = new StringBuffer(
        "Please click the link below to activate this email adres for physical resource group: ");
    text.append(activationUrl.toExternalForm());

    activationMessage.setText(text.toString());

    return activationMessage;
  }

  private URL generateActivationUrl(PhysicalResourceGroup physicalResourceGroup) {

    try {
      return new URL("http://localhost:8082/bod/activate/physicalresourcegroup/" + UUID.randomUUID().toString());
    }
    catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
}