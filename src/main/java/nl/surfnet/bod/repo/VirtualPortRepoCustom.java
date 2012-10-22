package nl.surfnet.bod.repo;

import java.util.List;

import nl.surfnet.bod.domain.VirtualPort;

import org.springframework.data.jpa.domain.Specification;

import com.google.common.base.Optional;

public interface VirtualPortRepoCustom {

  List<Long> findIdsWithWhereClause(Optional<Specification<VirtualPort>> whereClause);

}