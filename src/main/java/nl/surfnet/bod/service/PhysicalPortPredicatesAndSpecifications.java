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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;


import nl.surfnet.bod.domain.NmsAlignmentStatus;
import nl.surfnet.bod.domain.UniPort;
import nl.surfnet.bod.domain.PhysicalPort_;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.UniPort_;

public final class PhysicalPortPredicatesAndSpecifications {


  static final Specification<UniPort> UNALIGNED_PORT_SPEC = new Specification<UniPort>() {
    @Override
    public javax.persistence.criteria.Predicate toPredicate(Root<UniPort> physicalPort, CriteriaQuery<?> query,
        CriteriaBuilder cb) {
      return cb.notEqual(physicalPort.get(PhysicalPort_.nmsAlignmentStatus), NmsAlignmentStatus.ALIGNED);
    }
  };

  private PhysicalPortPredicatesAndSpecifications() {
  }

  static Specification<UniPort> byPhysicalResourceGroupSpec(final PhysicalResourceGroup physicalResourceGroup) {
    return new Specification<UniPort>() {

      @Override
      public javax.persistence.criteria.Predicate toPredicate(Root<UniPort> root, CriteriaQuery<?> query,
          CriteriaBuilder cb) {

        return cb.equal(root.get(UniPort_.physicalResourceGroup), physicalResourceGroup);
      }
    };
  }

}
