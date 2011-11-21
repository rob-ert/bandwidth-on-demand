package nl.surfnet.bod.service;

import java.util.List;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.repo.PhysicalResourceGroupRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PhysicalResourceGroupServiceImpl {

	@Autowired
	PhysicalResourceGroupRepo physicalResourceGroupRepo;

	public long countAllPhysicalResourceGroups() {
		return physicalResourceGroupRepo.count();
	}

	public void deletePhysicalResourceGroup(
	    final PhysicalResourceGroup physicalResourceGroup) {
		physicalResourceGroupRepo.delete(physicalResourceGroup);
	}

	public PhysicalResourceGroup findPhysicalResourceGroup(final Long id) {
		return physicalResourceGroupRepo.findOne(id);
	}

	public List<PhysicalResourceGroup> findAllPhysicalResourceGroups() {
		return physicalResourceGroupRepo.findAll();
	}

	public List<PhysicalResourceGroup> findPhysicalResourceGroupEntries(
	    final int firstResult, final int maxResults) {
		return physicalResourceGroupRepo.findAll(
		    new org.springframework.data.domain.PageRequest(firstResult
		        / maxResults, maxResults)).getContent();
	}

	public void savePhysicalResourceGroup(
	    final PhysicalResourceGroup physicalResourceGroup) {
		physicalResourceGroupRepo.save(physicalResourceGroup);
	}

	public PhysicalResourceGroup updatePhysicalResourceGroup(
	    final PhysicalResourceGroup physicalResourceGroup) {
		return physicalResourceGroupRepo.save(physicalResourceGroup);
	}
}
