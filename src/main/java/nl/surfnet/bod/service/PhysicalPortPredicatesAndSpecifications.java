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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalPort_;
import nl.surfnet.bod.domain.PhysicalResourceGroup;

import org.springframework.data.jpa.domain.Specification;

import com.google.common.base.Predicate;

public final class PhysicalPortPredicatesAndSpecifications {


  static final Specification<PhysicalPort> UNALIGNED_PORT_SPEC = new Specification<PhysicalPort>() {

    @Override
    public javax.persistence.criteria.Predicate toPredicate(Root<PhysicalPort> physicalPort, CriteriaQuery<?> query,
        CriteriaBuilder cb) {
      return cb.equal(physicalPort.get(PhysicalPort_.alignedWithNMS), false);
    }
  };

  static final Predicate<PhysicalPort> UNALLOCATED_PORTS_PRED = new Predicate<PhysicalPort>() {
    @Override
    public boolean apply(PhysicalPort input) {
      return input.getId() == null;
    }
  };

  private PhysicalPortPredicatesAndSpecifications() {
  }

  static Specification<PhysicalPort> byPhysicalResourceGroupSpec(final PhysicalResourceGroup physicalResourceGroup) {
    return new Specification<PhysicalPort>() {

      @Override
      public javax.persistence.criteria.Predicate toPredicate(Root<PhysicalPort> root, CriteriaQuery<?> query,
          CriteriaBuilder cb) {

        return cb.equal(root.get(PhysicalPort_.physicalResourceGroup), physicalResourceGroup);
      }
    };
  }

}
