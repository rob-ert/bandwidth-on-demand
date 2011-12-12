package nl.surfnet.bod.repo;

import nl.surfnet.bod.domain.VirtualResourceGroup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface VirtualResourceGroupRepo extends JpaSpecificationExecutor<VirtualResourceGroup>,
    JpaRepository<VirtualResourceGroup, Long> {

  /**
   * Finds a {@link VirtualResourceGroup} by
   * {@link VirtualResourceGroup#getSurfConnextGroupName()}
   * 
   * @param surfConnextGroupName The name to search for
   * @return {@link VirtualResourceGroup} or null when no match was found.
   */
  VirtualResourceGroup findBySurfConnextGroupName(String surfConnextGroupName);

}
