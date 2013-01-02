/**
 * Copyright (c) 2012, SURFnet BV
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
