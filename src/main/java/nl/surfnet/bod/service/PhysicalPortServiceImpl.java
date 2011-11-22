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
	public List<PhysicalPort> findAll() {
		return physicalPortRepo.findAll();
	}

	/**
	 * Finds {@link PhysicalPort}s in case paging is used. Delegates to
	 * {@link #findAll()} which will ignore the paging mechanism for
	 * now.
	 *
	 * @param firstResult
	 * @param sizeNo
	 * @return List of PhysicalPorts
	 */
	public List<PhysicalPort> findEntries(final int firstResult, final int sizeNo) {
		return findAll();
	}

	public long count() {
		return physicalPortRepo.count();
	}

	public void delete(final PhysicalPort physicalPort) {
		physicalPortRepo.delete(physicalPort);
	}

	public PhysicalPort find(final Long id) {
		return physicalPortRepo.findOne(id);
	}

	public void save(final PhysicalPort physicalPort) {
		physicalPortRepo.save(physicalPort);
	}

	public PhysicalPort update(final PhysicalPort physicalPort) {
		return physicalPortRepo.save(physicalPort);
	}
}
