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

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalPort_;
import nl.surfnet.bod.domain.PhysicalResourceGroup_;
import nl.surfnet.bod.domain.ProtectionType;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.domain.Reservation_;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualPort_;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.domain.VirtualResourceGroup_;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.view.ReservationFilterView;

import org.joda.time.DateTime;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

import com.google.common.collect.ImmutableList;

public class ReservationPredicatesAndSpecifications {

  static Specification<Reservation> forVirtualResourceGroup(final VirtualResourceGroup vrg) {
    return new Specification<Reservation>() {
      @Override
      public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.equal(root.get(Reservation_.virtualResourceGroup), vrg);
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

  static Specification<Reservation> forCurrentUser(final RichUserDetails user) {
    return new Specification<Reservation>() {
      @Override
      public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.and(root.get(Reservation_.virtualResourceGroup).get(VirtualResourceGroup_.surfconextGroupId).in(
            user.getUserGroupIds()));
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

  static Specification<Reservation> specReservationsThatCouldStart(final DateTime startDateTime) {
    return new Specification<Reservation>() {
      @Override
      public javax.persistence.criteria.Predicate toPredicate(Root<Reservation> reservation, CriteriaQuery<?> query,
          CriteriaBuilder cb) {

        return cb.and(cb.lessThanOrEqualTo(reservation.get(Reservation_.startDateTime), startDateTime), reservation
            .get(Reservation_.status).in(ImmutableList.of(ReservationStatus.REQUESTED, ReservationStatus.AUTO_START)));
      }
    };
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

  static Specification<Reservation> specByPhysicalPort(final PhysicalPort port) {
    return new Specification<Reservation>() {
      @Override
      public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.or(cb.equal(root.get(Reservation_.sourcePort).get(VirtualPort_.physicalPort), port), cb.equal(root
            .get(Reservation_.destinationPort).get(VirtualPort_.physicalPort), port));
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

  static Specification<Reservation> specByVirtualPort(final VirtualPort port) {
    return new Specification<Reservation>() {
      @Override
      public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.or(cb.equal(root.get(Reservation_.sourcePort), port), cb.equal(
            root.get(Reservation_.destinationPort), port));
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

  static Specification<Reservation> specFilteredReservationsForUser(final ReservationFilterView filter,
      final RichUserDetails user) {

    return Specifications.where(specFilteredReservations(filter)).and(forCurrentUser(user));
  }

  static Specification<Reservation> specFilteredReservationsForVirtualResourceGroup(final ReservationFilterView filter,
      final VirtualResourceGroup vrg) {

    return Specifications.where(specFilteredReservations(filter)).and(forVirtualResourceGroup(vrg));
  }

  static Specification<Reservation> specFilteredReservationsForManager(final ReservationFilterView filter,
      final RichUserDetails manager) {

    return Specifications.where(specFilteredReservations(filter)).and(forManager(manager));
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

  static Specification<Reservation> specReservationByProtectionTypeInIds(final Class<Reservation> reservationClass,
      final ProtectionType protectionType, final List<Long> reservationIds) {

    final Specification<Reservation> spec = new Specification<Reservation>() {

      @Override
      public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate protectionTypeIs = cb.equal(root.get(Reservation_.protectionType), protectionType);
        final Predicate reservationIdIn = root.get(Reservation_.id).in(reservationIds);
        return cb.and(protectionTypeIs, reservationIdIn);
      }

    };
    return spec;
  }

  static Specification<Reservation> specReservationWithConnection(final Class<Reservation> reservationClass,
      final List<Long> reservationIds) {

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

  public static Specification<Reservation> specReservationStartBeforeEndInOrAfterWithState(
      final ReservationStatus state, final DateTime start, final DateTime end) {

    Specification<Reservation> spec = new Specification<Reservation>() {
      @Override
      public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        final Predicate startInOrBeforePeriod = cb.lessThanOrEqualTo(root.get(Reservation_.startDateTime), end);
        final Predicate endInOrAfterPeriod = cb.greaterThanOrEqualTo(root.get(Reservation_.endDateTime), start);
        final Predicate status = cb.equal(root.get(Reservation_.status), state);

        return cb.and(startInOrBeforePeriod, endInOrAfterPeriod, status);
      }
    };

    return spec;

  }

}
