package nl.surfnet.bod.service;

import java.util.List;

import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.repo.VirtualResourceGroupRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VirtualResourceGroupService {

  @Autowired
  private VirtualResourceGroupRepo virtualResourceGroupRepo;

  public long count() {
    return virtualResourceGroupRepo.count();
  }

  public void delete(final VirtualResourceGroup virtualResourceGroup) {
    virtualResourceGroupRepo.delete(virtualResourceGroup);
  }

  public VirtualResourceGroup find(final Long id) {
    return virtualResourceGroupRepo.findOne(id);
  }

  public List<VirtualResourceGroup> findAll() {
    return virtualResourceGroupRepo.findAll();
  }

  public List<VirtualResourceGroup> findEntries(final int firstResult, final int maxResults) {
    return virtualResourceGroupRepo.findAll(new PageRequest(firstResult / maxResults, maxResults)).getContent();
  }

  public void save(final VirtualResourceGroup virtualResourceGroup) {
    virtualResourceGroupRepo.save(virtualResourceGroup);
  }

  public VirtualResourceGroup update(final VirtualResourceGroup virtualResourceGroup) {
    return virtualResourceGroupRepo.save(virtualResourceGroup);
  }

  public VirtualResourceGroup findBySurfConnextGroupName(String surfConnextGroupName) {
    return virtualResourceGroupRepo.findBySurfConnextGroupName(surfConnextGroupName);
  }
}
