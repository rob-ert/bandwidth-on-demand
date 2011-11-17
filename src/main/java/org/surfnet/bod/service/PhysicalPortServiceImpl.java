package org.surfnet.bod.service;

import java.util.List;

import org.surfnet.bod.domain.PhysicalPort;

public class PhysicalPortServiceImpl implements PhysicalPortService {

	/**
	 * Mock implementation which return a fixed List of {@link PhysicalPort}s
	 * 
	 * @return List<{@link PhysicalPort}>
	 */
	@Override
	public List<PhysicalPort> findAllPhysicalPorts() {

		List<PhysicalPort> physicalPorts = PhysicalPort.findAllPhysicalPorts();

		PhysicalPort ppOne = new PhysicalPort();
		ppOne.setName("TestPhysicalPort1");
		// physicalPorts.add(ppOne);

		PhysicalPort ppTwo = new PhysicalPort();
		ppTwo.setName("TestPhysicalPort2");
		// physicalPorts.add(ppTwo);

		return physicalPorts;
	}

	/**
	 * Finds {@link PhysicalPort}s in case paging is used. Delegates to
	 * {@link #findAllPhysicalPorts()} which will ignore the paging mechanism.
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
