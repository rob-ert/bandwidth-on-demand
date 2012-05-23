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
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import nl.surfnet.bod.domain.*;
import nl.surfnet.bod.domain.VirtualPortRequestLink.RequestStatus;
import nl.surfnet.bod.repo.VirtualPortRepo;
import nl.surfnet.bod.repo.VirtualPortRequestLinkRepo;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

@Service
@Transactional
public class VirtualPortService {

  @Autowired
  private VirtualPortRepo virtualPortRepo;
  @Autowired
  private VirtualPortRequestLinkRepo virtualPortRequestLinkRepo;
  @Autowired
  private EmailSender emailSender;

  @Autowired
  private ReservationService reservationService;

  public long count() {
    return virtualPortRepo.count();
  }

  public long countForUser(RichUserDetails user) {
    return virtualPortRepo.count(specificationForUser(user));
  }

  public long countForManager(BodRole managerRole) {
    checkArgument(managerRole.isManagerRole());

    return virtualPortRepo.count(specificationForManager(managerRole));
  }

  public void delete(final VirtualPort virtualPort) {
    final List<Reservation> reservations = reservationService.findBySourcePortOrDestinationPort(virtualPort, virtualPort);
    reservationService.deleteReservations(reservations);
    virtualPortRepo.delete(virtualPort);
  }

  public VirtualPort find(final Long id) {
    return virtualPortRepo.findOne(id);
  }

  public List<VirtualPort> findAll() {
    return virtualPortRepo.findAll();
  }

  public List<VirtualPort> findAllForUser(final RichUserDetails user) {
    checkNotNull(user);

    if (user.getUserGroups().isEmpty()) {
      return Collections.emptyList();
    }

    return virtualPortRepo.findAll(specificationForUser(user));
  }

  private Specification<VirtualPort> specificationForUser(final RichUserDetails user) {
    return new Specification<VirtualPort>() {
      @Override
      public javax.persistence.criteria.Predicate toPredicate(Root<VirtualPort> root, CriteriaQuery<?> query,
                                                              CriteriaBuilder cb) {
        return cb.and(root.get(VirtualPort_.virtualResourceGroup).get(VirtualResourceGroup_.surfconextGroupId)
            .in(user.getUserGroupIds()));
      }
    };
  }

  private Specification<VirtualPort> specificationForManager(final BodRole managerRole) {
    return new Specification<VirtualPort>() {

      @Override
      public javax.persistence.criteria.Predicate toPredicate(Root<VirtualPort> root, CriteriaQuery<?> query,
                                                              CriteriaBuilder cb) {
        return cb.equal(
                root.get(VirtualPort_.physicalPort).get(PhysicalPort_.physicalResourceGroup).get(PhysicalResourceGroup_.id),
                managerRole.getPhysicalResourceGroupId());
      }
    };
  }

  public List<VirtualPort> findEntries(final int firstResult, final int maxResults) {
    checkArgument(maxResults > 0);

    return virtualPortRepo.findAll(new PageRequest(firstResult / maxResults, maxResults)).getContent();
  }

  public List<VirtualPort> findEntriesForUser(final RichUserDetails user, final int firstResult, final int maxResults,
                                              Sort sort) {
    checkNotNull(user);

    if (user.getUserGroups().isEmpty()) {
      return Collections.emptyList();
    }

    return virtualPortRepo.findAll(specificationForUser(user),
        new PageRequest(firstResult / maxResults, maxResults, sort)).getContent();
  }

  public List<VirtualPort> findEntriesForManager(BodRole managerRole, int firstResult, int maxResults, Sort sort) {
    checkArgument(maxResults > 0);
    checkArgument(managerRole.isManagerRole());

    return virtualPortRepo.findAll(specificationForManager(managerRole),
        new PageRequest(firstResult / maxResults, maxResults, sort)).getContent();
  }

  public VirtualPort findByManagerLabel(String label) {
    return virtualPortRepo.findByManagerLabel(label);
  }

  public VirtualPort findByUserLabel(String label) {
    return virtualPortRepo.findByUserLabel(label);
  }

  public void save(final VirtualPort virtualPort) {
    virtualPortRepo.save(virtualPort);
  }

  public VirtualPort update(final VirtualPort virtualPort) {
    return virtualPortRepo.save(virtualPort);
  }

  public Collection<VirtualPort> findAllForPhysicalPort(PhysicalPort port) {
    checkNotNull(port);

    return virtualPortRepo.findByPhysicalPort(port);
  }

  public void requestNewVirtualPort(RichUserDetails user, VirtualResourceGroup vGroup, PhysicalResourceGroup pGroup,
                                    Integer minBandwidth, String message) {
    VirtualPortRequestLink link = new VirtualPortRequestLink();
    link.setVirtualResourceGroup(vGroup);
    link.setPhysicalResourceGroup(pGroup);
    link.setMinBandwidth(minBandwidth);
    link.setMessage(message);
    link.setRequestorEmail(user.getEmail());
    link.setRequestorName(user.getDisplayName());
    link.setRequestorUrn(user.getUsername());
    link.setRequestDateTime(LocalDateTime.now());

    virtualPortRequestLinkRepo.save(link);
    emailSender.sendVirtualPortRequestMail(user, link);
  }

  public Collection<VirtualPortRequestLink> findPendingRequests(PhysicalResourceGroup prg) {
    return virtualPortRequestLinkRepo.findByPhysicalResourceGroupAndStatus(prg, RequestStatus.PENDING);
  }

  public VirtualPortRequestLink findRequest(String uuid) {
    return virtualPortRequestLinkRepo.findByUuid(uuid);
  }

  public void requestLinkApproved(VirtualPortRequestLink link, VirtualPort port) {
    link.setStatus(RequestStatus.APPROVED);
    virtualPortRequestLinkRepo.save(link);
    emailSender.sendVirtualPortRequestApproveMail(link, port);
  }

  public VirtualPortRequestLink findRequest(Long id) {
    return virtualPortRequestLinkRepo.findOne(id);
  }

  public Collection<VirtualPortRequestLink> findPendingRequests(Collection<UserGroup> userGroups) {
    return virtualPortRequestLinkRepo.findByVirtualResourceGroupSurfconextGroupIdInAndStatus(
        Collections2.transform(userGroups, new Function<UserGroup, String>() {
          @Override
          public String apply(UserGroup group) {
            return group.getId();
          }
        }), RequestStatus.PENDING);
  }

  public void requestLinkDeclined(VirtualPortRequestLink link, String declineMessage) {
    link.setStatus(RequestStatus.DECLINED);
    virtualPortRequestLinkRepo.save(link);
    emailSender.sendVirtualPortRequestDeclineMail(link, declineMessage);
  }
}
