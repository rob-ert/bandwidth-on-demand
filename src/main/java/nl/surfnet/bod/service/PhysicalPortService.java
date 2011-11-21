package nl.surfnet.bod.service;

import java.util.List;
import nl.surfnet.bod.domain.PhysicalPort;

public interface PhysicalPortService {


	public abstract long countAllPhysicalPorts();


	public abstract void deletePhysicalPort(PhysicalPort physicalPort);


	public abstract PhysicalPort findPhysicalPort(Long id);


	public abstract List<PhysicalPort> findAllPhysicalPorts();


	public abstract List<PhysicalPort> findPhysicalPortEntries(int firstResult, int maxResults);


	public abstract void savePhysicalPort(PhysicalPort physicalPort);


	public abstract PhysicalPort updatePhysicalPort(PhysicalPort physicalPort);

}
