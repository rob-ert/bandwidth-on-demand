package nl.surfnet.bod.repo;

import nl.surfnet.bod.domain.VirtualResourceGroup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface VirtualResourceGroupRepo extends JpaSpecificationExecutor<VirtualResourceGroup>,
    JpaRepository<VirtualResourceGroup, Long> {

}
