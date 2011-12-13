package nl.surfnet.bod.service;

import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.repo.PhysicalResourceGroupRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

@Service
@Transactional
public class PhysicalResourceGroupService {

  @Autowired
  private PhysicalResourceGroupRepo physicalResourceGroupRepo;

  @Autowired
  private GroupService groupService;

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

  public Collection<PhysicalResourceGroup> findAllForUser(String nameId) {
    Collection<UserGroup> groups = groupService.getGroups(nameId);

    Collection<String> adminGroups = Lists.newArrayList(Collections2.transform(groups, new Function<UserGroup, String>() {
      @Override
      public String apply(UserGroup group) {
        return group.getId();
      }}));
    
    return physicalResourceGroupRepo.findByAdminGroupIn(adminGroups);
  }

  protected void setPhysicalResourceGroupRepo(PhysicalResourceGroupRepo physicalResourceGroupRepo) {
    this.physicalResourceGroupRepo = physicalResourceGroupRepo;
  }

  protected void setGroupService(GroupService groupService) {
    this.groupService = groupService;
  }
}
