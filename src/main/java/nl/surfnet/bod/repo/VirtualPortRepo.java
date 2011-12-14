package nl.surfnet.bod.repo;

import nl.surfnet.bod.domain.VirtualPort;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface VirtualPortRepo extends JpaSpecificationExecutor<VirtualPort>, JpaRepository<VirtualPort, Long> {

  /**
   * Finds a {@link VirtualPort} by
   * {@link VirtualPort#getName()}
   * 
   * @param name The name to search for
   * @return {@link VirtualPort} or null when no match was found.
   */
  VirtualPort findByName(String name);
  
}
