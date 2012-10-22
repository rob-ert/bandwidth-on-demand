package nl.surfnet.bod.repo;

import java.util.List;

import nl.surfnet.bod.domain.Reservation;

import org.springframework.data.jpa.domain.Specification;

public interface ReservationRepoCustom {

  List<Long> findIdsWithWhereClause(final Specification<Reservation> whereClause);

}