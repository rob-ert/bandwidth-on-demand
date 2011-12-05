package nl.surfnet.bod.repo;

import nl.surfnet.bod.domain.VirtualPort;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface VirtualPortRepo extends JpaSpecificationExecutor<VirtualPort>, JpaRepository<VirtualPort, Long> {
  
}
