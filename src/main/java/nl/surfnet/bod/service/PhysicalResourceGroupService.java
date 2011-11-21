package nl.surfnet.bod.service;

import java.util.List;
import nl.surfnet.bod.domain.PhysicalResourceGroup;

public interface PhysicalResourceGroupService {

	public abstract long countAllPhysicalResourceGroups();


	public abstract void deletePhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup);


	public abstract PhysicalResourceGroup findPhysicalResourceGroup(Long id);


	public abstract List<PhysicalResourceGroup> findAllPhysicalResourceGroups();


	public abstract List<PhysicalResourceGroup> findPhysicalResourceGroupEntries(int firstResult, int maxResults);


	public abstract void savePhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup);


	public abstract PhysicalResourceGroup updatePhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup);

}
