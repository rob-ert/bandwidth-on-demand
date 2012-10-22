package nl.surfnet.bod.repo;

import java.util.List;

import nl.surfnet.bod.domain.PhysicalResourceGroup;

import org.springframework.data.jpa.domain.Specification;

import com.google.common.base.Optional;

public interface PhysicalResourceGroupRepoCustom {

  List<Long> findIdsWithWhereClause(Optional<Specification<PhysicalResourceGroup>> whereClause);
}
