package nl.surfnet.bod.repo;

import java.util.Collection;
import java.util.List;

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
   * @param surfConnextGroupName
   *          The name to search for
   * @return {@link VirtualResourceGroup} or null when no match was found.
   */
  VirtualResourceGroup findBySurfConnextGroupName(String surfConnextGroupName);

  /**
   * Finds a {@link VirtualResourceGroup} by
   * {@link VirtualResourceGroup#getName()}
   * 
   * @param Name
   *          The name to search for
   * @return {@link VirtualResourceGroup} or null when no match was found.
   */
  VirtualResourceGroup findByName(String name);

  /**
   * Finds {@link VirtualResourceGroup}s by a Collection of adminGroups
   * {@link VirtualResourceGroup#getSurfConnextGroupName())}
   * 
   * @param Collection
   *          <String> adminGroups to search for
   * 
   * @return List<VirtualResourceGroup> or empty list when no match was found.
   */
  List<VirtualResourceGroup> findBySurfConnextGroupNameIn(Collection<String> adminGroups);
}
