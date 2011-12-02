package nl.surfnet.bod.service;

import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.domain.PhysicalPort;

public interface PhysicalPortService {

  /**
   * Basic implementation, just serves as a placeholder for the integration with
   * the NMS to retrieve physicalPorts
   *
   * @return List<{@link PhysicalPort}>
   */
  List<PhysicalPort> findAll();

  Collection<PhysicalPort> findUnallocated();

  /**
   * Finds {@link PhysicalPort}s in case paging is used.
   *
   * @param firstResult
   * @param sizeNo
   *          max result size
   * @return List of PhysicalPorts
   */
  List<PhysicalPort> findEntries(final int firstResult, final int sizeNo);

  /**
   * Finds unallocated {@link PhysicalPort}s with a start index and a max number
   * of results.
   *
   * @param firstResult
   * @param sizeNo
   *          max result size
   * @return Collection of unallocated ports
   */
  Collection<PhysicalPort> findUnallocatedEntries(final int firstResult, final int sizeNo);

  long count();

  long countUnallocated();

  void delete(final PhysicalPort physicalPort);

  PhysicalPort find(final Long id);

  PhysicalPort findByName(final String name);

  void save(final PhysicalPort physicalPort);

  PhysicalPort update(final PhysicalPort physicalPort);

}