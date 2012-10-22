package nl.surfnet.bod.repo;

import java.util.List;

import nl.surfnet.bod.domain.VirtualResourceGroup;

import org.springframework.data.jpa.domain.Specification;

import com.google.common.base.Optional;

public interface VirtualResourceGroupRepoCustom {

  List<Long> findIdsWithWhereClause(final Optional<Specification<VirtualResourceGroup>> whereClause);

}
