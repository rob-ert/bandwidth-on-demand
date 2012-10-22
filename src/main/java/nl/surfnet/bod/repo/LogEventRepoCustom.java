package nl.surfnet.bod.repo;

import java.util.List;

import nl.surfnet.bod.event.LogEvent;

import org.springframework.data.jpa.domain.Specification;

import com.google.common.base.Optional;

public interface LogEventRepoCustom {

  List<Long> findIdsWithWhereClause(Optional<Specification<LogEvent>> whereClause);
}
