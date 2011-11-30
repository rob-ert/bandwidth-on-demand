package nl.surfnet.bod.service;

import java.util.List;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.repo.PhysicalPortRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("physicalPortServiceRepoImpl")
@Transactional
class PhysicalPortServiceRepoImpl implements PhysicalPortService {

  @Autowired
  private PhysicalPortRepo physicalPortRepo;

  @Override
  public List<PhysicalPort> findAll() {
    return physicalPortRepo.findAll();
  }

  @Override
  public List<PhysicalPort> findEntries(final int firstResult, final int sizeNo) {
    return findAll();
  }

  @Override
  public long count() {
    return physicalPortRepo.count();
  }

  @Override
  public void delete(final PhysicalPort physicalPort) {
    physicalPortRepo.delete(physicalPort);
  }

  @Override
  public PhysicalPort find(final Long id) {
    return physicalPortRepo.findOne(id);
  }

  @Override
  public PhysicalPort findByName(final String name) {
    return physicalPortRepo.findByName(name);
  }

  @Override
  public void save(final PhysicalPort physicalPort) {
    physicalPortRepo.save(physicalPort);
  }

  @Override
  public PhysicalPort update(final PhysicalPort physicalPort) {
    return physicalPortRepo.save(physicalPort);
  }

}
