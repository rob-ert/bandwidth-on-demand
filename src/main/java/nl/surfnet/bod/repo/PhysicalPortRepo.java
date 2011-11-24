package nl.surfnet.bod.repo;

import nl.surfnet.bod.domain.PhysicalPort;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PhysicalPortRepo extends JpaSpecificationExecutor<PhysicalPort>, JpaRepository<PhysicalPort, Long> {

    PhysicalPort findByPortId(String portId);
}
