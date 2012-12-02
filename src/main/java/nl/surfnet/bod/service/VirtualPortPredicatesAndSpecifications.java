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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import nl.surfnet.bod.domain.*;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.springframework.data.jpa.domain.Specification;

public final class VirtualPortPredicatesAndSpecifications {

  private VirtualPortPredicatesAndSpecifications() {
  }

  static Specification<VirtualPort> forUserSpec(final RichUserDetails user) {
    return new Specification<VirtualPort>() {
      @Override
      public javax.persistence.criteria.Predicate toPredicate(Root<VirtualPort> root, CriteriaQuery<?> query,
          CriteriaBuilder cb) {
        return cb.and(root.get(VirtualPort_.virtualResourceGroup).get(VirtualResourceGroup_.adminGroup)
            .in(user.getUserGroupIds()));
      }
    };
  }

  static Specification<VirtualPort> forManagerSpec(final BodRole managerRole) {
    return new Specification<VirtualPort>() {

      @Override
      public javax.persistence.criteria.Predicate toPredicate(Root<VirtualPort> root, CriteriaQuery<?> query,
          CriteriaBuilder cb) {
        return cb
            .equal(
                root.get(VirtualPort_.physicalPort).get(PhysicalPort_.physicalResourceGroup)
                    .get(PhysicalResourceGroup_.id), managerRole.getPhysicalResourceGroupId().get());
      }
    };
  }

  static Specification<VirtualPort> byPhysicalPortSpec(final PhysicalPort physicalPort) {
    return new Specification<VirtualPort>() {

      private final Long physicalPortId = physicalPort.getId();

      @Override
      public javax.persistence.criteria.Predicate toPredicate(Root<VirtualPort> root, CriteriaQuery<?> query,
          CriteriaBuilder cb) {
        return cb.and(cb.equal(root.get(VirtualPort_.physicalPort).get(PhysicalPort_.id), physicalPortId));
      }
    };
  }

  static Specification<VirtualPortRequestLink> byGroupIdInLastMonthSpec(final Collection<String> vrgUrns) {
    return new Specification<VirtualPortRequestLink>() {

      @Override
      public Predicate toPredicate(Root<VirtualPortRequestLink> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        DateTime start = new DateTime(DateMidnight.now().plusDays(1).toDate().getTime());

        return cb
            .and(
                cb.between(root.get(VirtualPortRequestLink_.requestDateTime), start.minusDays(31), start),
                root.get(VirtualPortRequestLink_.virtualResourceGroup).get(VirtualResourceGroup_.adminGroup)
                    .in(vrgUrns));
      }
    };
  }

}
