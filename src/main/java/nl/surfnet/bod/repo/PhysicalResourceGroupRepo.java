package nl.surfnet.bod.repo;

import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.domain.PhysicalResourceGroup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PhysicalResourceGroupRepo extends JpaSpecificationExecutor<PhysicalResourceGroup>,
    JpaRepository<PhysicalResourceGroup, Long> {

  /**
   * Finds {@link PhysicalResourceGroup}s by a Collection of adminGroups
   * {@link PhysicalResourceGroup#getAdminGroup()}
   * 
   * @param Collection<String> adminGroups to search for
   * 
   * @return {@link PhysicalResourceGroup} or null when no match was found.
   */
  List<PhysicalResourceGroup> findByAdminGroupIn(Collection<String> adminGroups);

  /**
   * Finds a {@link PhysicalResourceGroup} by
   * {@link PhysicalResourceGroup#getName()}
   * 
   * @param name
   *          The name to search for
   * @return {@link PhysicalResourceGroup} or null when no match was found.
   */
  PhysicalResourceGroup findByName(String name);
}
