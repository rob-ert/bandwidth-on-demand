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

public class ReservationPredicatesAndSpecifications {

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
    if (filter.isFilterOnStatusOnly()) {
      // Do nothing, specification is already set with states from filter
    }
    else if (filter.isFilterOnReservationEndOnly()) {
      specficiation = Specifications.where(specficiation).and(filterSpecOnEnd);
    }
    else {
      specficiation = Specifications.where(specficiation).and(
          Specifications.where(filterSpecOnStart).or(filterSpecOnEnd));
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
        final Predicate connectionPredicate = cb.isNotNull(root.get(Reservation_.connection));

        final Predicate reservationIdIn = root.get(Reservation_.id).in(reservationIds);

        return cb.and(connectionPredicate, reservationIdIn);
      }
    };
    return spec;
  }

}
