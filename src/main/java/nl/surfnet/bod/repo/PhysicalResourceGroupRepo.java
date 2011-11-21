package nl.surfnet.bod.repo;

import nl.surfnet.bod.domain.PhysicalResourceGroup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PhysicalResourceGroupRepo extends JpaSpecificationExecutor<PhysicalResourceGroup>, JpaRepository<PhysicalResourceGroup, Long> {
}
