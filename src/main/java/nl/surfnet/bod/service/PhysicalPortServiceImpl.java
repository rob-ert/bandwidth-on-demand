package nl.surfnet.bod.service;

import java.util.List;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.repo.PhysicalPortRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PhysicalPortServiceImpl {

    @Autowired
    private PhysicalPortRepo physicalPortRepo;

	/**
	 * Basic implementation, just serves as a placeholder for the integration with
	 * the NMS to retrieve physicalPorts
	 *
	 * @return List<{@link PhysicalPort}>
	 */
	public List<PhysicalPort> findAllPhysicalPorts() {
		return physicalPortRepo.findAll();
	}

	/**
	 * Finds {@link PhysicalPort}s in case paging is used. Delegates to
	 * {@link #findAllPhysicalPorts()} which will ignore the paging mechanism for
	 * now.
	 *
	 * @param firstResult
	 * @param sizeNo
	 * @return List of PhysicalPorts
	 */
	public List<PhysicalPort> findPhysicalPortEntries(final int firstResult, final int sizeNo) {
		return findAllPhysicalPorts();
	}

	public long countAllPhysicalPorts() {
		return physicalPortRepo.count();
	}

	public void deletePhysicalPort(final PhysicalPort physicalPort) {
		physicalPortRepo.delete(physicalPort);
	}

	public PhysicalPort findPhysicalPort(final Long id) {
		return physicalPortRepo.findOne(id);
	}

	public void savePhysicalPort(final PhysicalPort physicalPort) {
		physicalPortRepo.save(physicalPort);
	}

	public PhysicalPort updatePhysicalPort(final PhysicalPort physicalPort) {
		return physicalPortRepo.save(physicalPort);
	}
}
