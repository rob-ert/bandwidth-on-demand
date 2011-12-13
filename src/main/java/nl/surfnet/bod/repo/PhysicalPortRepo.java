package nl.surfnet.bod.repo;

import java.util.List;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PhysicalPortRepo extends JpaSpecificationExecutor<PhysicalPort>, JpaRepository<PhysicalPort, Long> {

  PhysicalPort findByName(String name);

  List<PhysicalPort> findByPhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup);
}
