package org.surfnet.bod.service;

import java.util.List;

import org.springframework.roo.addon.layers.service.RooService;
import org.surfnet.bod.domain.PhysicalPort;

@RooService(domainTypes = { org.surfnet.bod.domain.PhysicalPort.class })
public interface PhysicalPortService {

	List<PhysicalPort> findPhysicalPortEntries(final int firstResult,
	    final int sizeNo);

	List<PhysicalPort> findAllPhysicalPorts();
}
