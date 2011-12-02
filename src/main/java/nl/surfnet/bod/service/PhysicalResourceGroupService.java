package nl.surfnet.bod.service;

import java.util.List;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.repo.PhysicalResourceGroupRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PhysicalResourceGroupService {

  @Autowired
  private PhysicalResourceGroupRepo physicalResourceGroupRepo;

  public long count() {
    return physicalResourceGroupRepo.count();
  }

  public void delete(final PhysicalResourceGroup physicalResourceGroup) {
    physicalResourceGroupRepo.delete(physicalResourceGroup);
  }

  public PhysicalResourceGroup find(final Long id) {
    return physicalResourceGroupRepo.findOne(id);
  }

  public List<PhysicalResourceGroup> findAll() {
    return physicalResourceGroupRepo.findAll();
  }

  public List<PhysicalResourceGroup> findEntries(final int firstResult, final int maxResults) {
    return physicalResourceGroupRepo.findAll(new PageRequest(firstResult / maxResults, maxResults)).getContent();
  }

  public void save(final PhysicalResourceGroup physicalResourceGroup) {
    physicalResourceGroupRepo.save(physicalResourceGroup);
  }

  public PhysicalResourceGroup update(final PhysicalResourceGroup physicalResourceGroup) {
    return physicalResourceGroupRepo.save(physicalResourceGroup);
  }
}
