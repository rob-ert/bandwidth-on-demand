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

import static org.springframework.data.jpa.domain.Specifications.where;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import nl.surfnet.bod.domain.*;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.view.ReservationFilterView;

import org.joda.time.DateTime;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

public final class ReservationPredicatesAndSpecifications {

  private ReservationPredicatesAndSpecifications() {
  }

  static Specification<Reservation> forCurrentUser(final RichUserDetails user) {
    return new Specification<Reservation>() {
      @Override
      public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.and(root.get(Reservation_.virtualResourceGroup).get(VirtualResourceGroup_.adminGroup).in(
            user.getUserGroupIds()));
      }
    };
  }

  static Specification<Reservation> forManager(final RichUserDetails manager) {
    return new Specification<Reservation>() {
      @Override
      public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        Long prgId = manager.getSelectedRole().getPhysicalResourceGroupId().get();
        return cb.and(cb.or(cb.equal(root.get(Reservation_.sourcePort).get(VirtualPort_.physicalPort).get(
            PhysicalPort_.physicalResourceGroup).get(PhysicalResourceGroup_.id), prgId), cb.equal(root.get(
            Reservation_.destinationPort).get(VirtualPort_.physicalPort).get(PhysicalPort_.physicalResourceGroup).get(
            PhysicalResourceGroup_.id), prgId)));
      }
    };
  }

  static Specification<Reservation> forStatus(final ReservationStatus... states) {
    return new Specification<Reservation>() {
      @Override
      public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.and(root.get(Reservation_.status).in((Object[]) states));
      }
    };
  }

  static Specification<Reservation> forVirtualResourceGroup(final VirtualResourceGroup vrg) {
    return new Specification<Reservation>() {
      @Override
      public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.equal(root.get(Reservation_.virtualResourceGroup), vrg);
      }

    };
  }

  public static Specification<Reservation> specActiveByVirtualPorts(final List<VirtualPort> virtualPorts) {
    return new Specification<Reservation>() {

      @Override
      public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        return cb.and((cb.or(root.get(Reservation_.sourcePort).in(virtualPorts), root.get(Reservation_.destinationPort)
            .in(virtualPorts))), (root.get(Reservation_.status).in(ReservationStatus.TRANSITION_STATES)));
      }
    };
  }

  static Specification<Reservation> specActiveReservations() {
    return new Specification<Reservation>() {
      @Override
      public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return root.get(Reservation_.status).in(ReservationStatus.TRANSITION_STATES);
      }
    };
  }

  static Specification<Reservation> specByPhysicalPort(final PhysicalPort port) {
    return new Specification<Reservation>() {
      @Override
      public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.or(cb.equal(root.get(Reservation_.sourcePort).get(VirtualPort_.physicalPort), port), cb.equal(root
            .get(Reservation_.destinationPort).get(VirtualPort_.physicalPort), port));
      }
    };
  }

  static Specification<Reservation> specByVirtualPort(final VirtualPort port) {
    return new Specification<Reservation>() {
      @Override
      public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.or(cb.equal(root.get(Reservation_.sourcePort), port), cb.equal(
            root.get(Reservation_.destinationPort), port));
      }
    };
  }

  static Specification<Reservation> specByVirtualPortAndManager(final VirtualPort port, final RichUserDetails user) {
    return new Specification<Reservation>() {
      @Override
      public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        final Long prgId = user.getSelectedRole().getPhysicalResourceGroupId().get();
        return cb.and(cb.or(cb.equal(root.get(Reservation_.sourcePort).get(VirtualPort_.physicalPort).get(
            PhysicalPort_.physicalResourceGroup).get(PhysicalResourceGroup_.id), prgId), cb.equal(root.get(
            Reservation_.destinationPort).get(VirtualPort_.physicalPort).get(PhysicalPort_.physicalResourceGroup).get(
            PhysicalResourceGroup_.id), prgId)));
      }
    };
  }

  static Specification<Reservation> specFilteredReservations(final ReservationFilterView filter) {
    Specification<Reservation> filterSpecOnStart = new Specification<Reservation>() {
      @Override
      public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.between(root.get(Reservation_.startDateTime), filter.getStart(), filter.getEnd());
      }
    };

    Specification<Reservation> filterSpecOnEnd = new Specification<Reservation>() {
      @Override
      public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.or(cb.isNull(root.get(Reservation_.endDateTime)), cb.between(root.get(Reservation_.endDateTime),
            filter.getStart(), filter.getEnd()));
      }
    };

    // Filter on states in filter
    Specification<Reservation> specficiation = forStatus(filter.getStates());
    if (filter.isFilterOnReservationEndOnly()) {
      specficiation = where(specficiation).and(filterSpecOnEnd);
    }
    else if (!filter.isFilterOnStatusOnly()) {
      specficiation =
        where(specficiation)
        .and(where(filterSpecOnStart).or(filterSpecOnEnd));
    }

    return specficiation;
  }

  static Specification<Reservation> specFilteredReservationsForManager(final ReservationFilterView filter,
      final RichUserDetails manager) {

    return Specifications.where(specFilteredReservations(filter)).and(forManager(manager));
  }

  static Specification<Reservation> specFilteredReservationsForUser(final ReservationFilterView filter,
      final RichUserDetails user) {

    return Specifications.where(specFilteredReservations(filter)).and(forCurrentUser(user));
  }

  static Specification<Reservation> specFilteredReservationsForVirtualResourceGroup(final ReservationFilterView filter,
      final VirtualResourceGroup vrg) {

    return Specifications.where(specFilteredReservations(filter)).and(forVirtualResourceGroup(vrg));
  }

  static Specification<Reservation> specReservationByProtectionTypeInIds(final List<Long> reservationIds,
      final ProtectionType protectionType) {

    final Specification<Reservation> spec = new Specification<Reservation>() {

      @Override
      public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        final Predicate protectionTypeIs = cb.equal(root.get(Reservation_.protectionType), protectionType);
        final Predicate reservationIdIn = root.get(Reservation_.id).in(reservationIds);
        return cb.and(protectionTypeIs, reservationIdIn);
      }

    };
    return spec;
  }

  /**
   * Specification to find {@link Reservation}s which have started in or before
   * the given period and which end in or after the given period.
   *
   * @param start
   *          {@link DateTime} start of the period
   * @param end
   *          {@link DateTime} end of the period
   * @return Specification<Reservation>
   */
  public static Specification<Reservation> specReservationStartBeforeAndEndInOrAfter(final DateTime start,
      final DateTime end) {

    Specification<Reservation> spec = new Specification<Reservation>() {
      @Override
      public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        final Predicate startInOrBeforePeriod = cb.lessThanOrEqualTo(root.get(Reservation_.startDateTime), end);

        // Infinite reservations have no endDate
        final Predicate endInOrAfterPeriod = cb.or(cb.isNull(root.get(Reservation_.endDateTime)), cb
            .greaterThanOrEqualTo(root.get(Reservation_.endDateTime), start));

        return cb.and(startInOrBeforePeriod, endInOrAfterPeriod);
      }
    };

    return spec;
  }

  // TODO: Verify
  static Specification<Reservation> specReservationsThatAreTimedOutAndTransitionally(final DateTime startDateTime) {
    return new Specification<Reservation>() {
      @Override
      public javax.persistence.criteria.Predicate toPredicate(Root<Reservation> reservation, CriteriaQuery<?> query,
          CriteriaBuilder cb) {
        // the start time has past
        return cb.and(cb.lessThan(reservation.get(Reservation_.startDateTime), startDateTime),
        // end time has past
            cb.lessThan(reservation.get(Reservation_.endDateTime), DateTime.now()),
            // but reservation is still transitional
            reservation.get(Reservation_.status).in(ReservationStatus.TRANSITION_STATES));
      }
    };
  }

  static Specification<Reservation> specReservationsThatCouldStart(final DateTime startDateTime) {
    return new Specification<Reservation>() {
      @Override
      public javax.persistence.criteria.Predicate toPredicate(Root<Reservation> reservation, CriteriaQuery<?> query,
          CriteriaBuilder cb) {

        return cb.and(cb.lessThanOrEqualTo(reservation.get(Reservation_.startDateTime), startDateTime), reservation
            .get(Reservation_.status).in(ReservationStatus.COULD_START_STATES));
      }
    };
  }

  static Specification<Reservation> specReservationWithConnection(final List<Long> reservationIds) {
    final Specification<Reservation> spec = new Specification<Reservation>() {

      @Override
      public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        final Predicate connectionPredicate = cb.or(
            root.get(Reservation_.connectionV1).get(ConnectionV1_.id).isNotNull(),
            root.get(Reservation_.connectionV2).get(ConnectionV2_.id).isNotNull());
        final Predicate reservationIdIn = root.get(Reservation_.id).in(reservationIds);

        return cb.and(connectionPredicate, reservationIdIn);
      }
    };

    return spec;
  }

}
