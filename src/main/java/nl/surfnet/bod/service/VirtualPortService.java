package nl.surfnet.bod.service;

import java.util.List;

import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.repo.VirtualPortRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VirtualPortService {

  @Autowired
  private VirtualPortRepo virtualPortRepo;

  public long count() {
    return virtualPortRepo.count();
  }

  public void delete(final VirtualPort virtualPort) {
    virtualPortRepo.delete(virtualPort);
  }

  public VirtualPort find(final Long id) {
    return virtualPortRepo.findOne(id);
  }

  public List<VirtualPort> findAll() {
    return virtualPortRepo.findAll();
  }

  public List<VirtualPort> findEntries(final int firstResult, final int maxResults) {
    return virtualPortRepo.findAll(new PageRequest(firstResult / maxResults, maxResults)).getContent();
  }

  public VirtualPort findByName(String name) {

    return virtualPortRepo.findByName(name);
  }

  public void save(final VirtualPort virtualPort) {
    virtualPortRepo.save(virtualPort);
  }

  public VirtualPort update(final VirtualPort virtualPort) {
    return virtualPortRepo.save(virtualPort);
  }

}
