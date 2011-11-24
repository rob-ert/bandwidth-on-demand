package nl.surfnet.bod.service;

import java.util.List;

import nl.surfnet.bod.domain.PhysicalPort;

public interface PhysicalPortService {

    /**
     * Basic implementation, just serves as a placeholder for the integration
     * with the NMS to retrieve physicalPorts
     * 
     * @return List<{@link PhysicalPort}>
     */
    List<PhysicalPort> findAll();

    /**
     * Finds {@link PhysicalPort}s in case paging is used. Delegates to
     * {@link #findAll()} which will ignore the paging mechanism for now.
     * 
     * @param firstResult
     * @param sizeNo
     * @return List of PhysicalPorts
     */
    List<PhysicalPort> findEntries(final int firstResult, final int sizeNo);

    long count();

    void delete(final PhysicalPort physicalPort);

    PhysicalPort find(final Long id);

    PhysicalPort findByPortId(final String portId);

    void save(final PhysicalPort physicalPort);

    PhysicalPort update(final PhysicalPort physicalPort);

}