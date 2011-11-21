package nl.surfnet.bod.service;

import java.util.List;

import nl.surfnet.bod.domain.PhysicalPort;

public class PhysicalPortServiceImpl implements PhysicalPortService {

	/**
	 * Basic implementation, just serves as a placeholder for the integration with
	 * the NMS to retrieve physicalPorts
	 * 
	 * @return List<{@link PhysicalPort}>
	 */
	@Override
	public List<PhysicalPort> findAllPhysicalPorts() {

		List<PhysicalPort> physicalPorts = physicalPortRepo.findAll();

		return physicalPorts;
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
	@Override
	public List<PhysicalPort> findPhysicalPortEntries(final int firstResult,
	    final int sizeNo) {
		return findAllPhysicalPorts();
	}

}
