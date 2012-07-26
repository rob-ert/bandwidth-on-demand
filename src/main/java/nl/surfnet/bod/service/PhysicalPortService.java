/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
package nl.surfnet.bod.service;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Sort;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;

public interface PhysicalPortService {

  /**
   * Finds all unallocated ports. Which means ports that are not connected to a
   * {@link PhysicalPort} in BoD.
   * 
   * @return List of unallocated physical ports
   */
  Collection<PhysicalPort> findUnallocated();

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

  /**
   * Finds all allocated physical ports.
   * 
   * @param firstResult
   *          index of first result
   * @param sizeNo
   *          max result size
   * 
   * @return Collection of allocated ports
   */
  List<PhysicalPort> findAllocatedEntries(int firstResult, int sizeNo, Sort sort);

  /**
   * Finds all physical ports related to the given {@link PhysicalResourceGroup}
   * *
   * 
   * 
   * @param physicalResourceGroup
   *          {@link PhysicalResourceGroup} to filter on
   * 
   * @return list of physical ports
   */
  List<PhysicalPort> findAllocatedEntriesForPhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup,
      int firstResult, int sizeNo, Sort sort);

  /**
   * Finds all physical ports which are marked unaligned, since the are no
   * longer present in the NMS.
   * 
   * @return list of physical ports
   */
  List<PhysicalPort> findUnalignedPhysicalPorts();

  long countUnallocated();

  long countAllocated();

  long countUnallocatedMTOSI();

  long countAllocatedForPhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup);

  /**
   * 
   * @return Count of all physical ports marked unaligned with the NMS
   */
  long countUnalignedPhysicalPorts();

  void delete(final PhysicalPort physicalPort);

  PhysicalPort find(final Long id);

  PhysicalPort findByNmsPortId(final String nmsPortId);

  void save(final PhysicalPort physicalPort);

  PhysicalPort update(final PhysicalPort physicalPort);

  /**
   * Finds unallocated {@link PhysicalPort}s with a start index and a max number
   * of results retreived using MTOSI.
   * 
   * @param firstResult
   * @param sizeNo
   *          max result size
   * @return Collection of unallocated ports
   */
  Collection<PhysicalPort> findUnallocatedMTOSIEntries(final int firstResult, final int sizeNo);

  /**
   * Forces to check for port unconstitencies, e.g. checking if there are ports
   * unaligned or re-aligned with the NM.
   */
  void forceCheckForPortInconsitencies();

}