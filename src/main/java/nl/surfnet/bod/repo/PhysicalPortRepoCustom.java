package nl.surfnet.bod.repo;

import java.util.List;

import nl.surfnet.bod.domain.PhysicalPort;

import org.springframework.data.jpa.domain.Specification;

import com.google.common.base.Optional;

public interface PhysicalPortRepoCustom {

  List<Long> findIdsWithWhereClause(Optional<Specification<PhysicalPort>> whereClause);
}
