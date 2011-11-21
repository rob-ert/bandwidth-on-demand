package nl.surfnet.bod.service;

import java.util.List;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.repo.PhysicalResourceGroupRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class PhysicalResourceGroupServiceImpl implements
    PhysicalResourceGroupService {


	@Autowired
    PhysicalResourceGroupRepo physicalResourceGroupRepo;

	public long countAllPhysicalResourceGroups() {
        return physicalResourceGroupRepo.count();
    }

	public void deletePhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup) {
        physicalResourceGroupRepo.delete(physicalResourceGroup);
    }

	public PhysicalResourceGroup findPhysicalResourceGroup(Long id) {
        return physicalResourceGroupRepo.findOne(id);
    }

	public List<PhysicalResourceGroup> findAllPhysicalResourceGroups() {
        return physicalResourceGroupRepo.findAll();
    }

	public List<PhysicalResourceGroup> findPhysicalResourceGroupEntries(int firstResult, int maxResults) {
        return physicalResourceGroupRepo.findAll(new org.springframework.data.domain.PageRequest(firstResult / maxResults, maxResults)).getContent();
    }

	public void savePhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup) {
        physicalResourceGroupRepo.save(physicalResourceGroup);
    }

	public PhysicalResourceGroup updatePhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup) {
        return physicalResourceGroupRepo.save(physicalResourceGroup);
    }
}
