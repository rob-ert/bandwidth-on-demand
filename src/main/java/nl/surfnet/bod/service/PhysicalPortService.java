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

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.web.security.RichUserDetails;

public interface PhysicalPortService {

  /**
   * Finds all physical ports. Both allocated and unallocated.
   *
   * @return List of physical ports
   */
  List<PhysicalPort> findAll();

  /**
   * Finds all unallocated ports. Which means ports that are not connected to a
   * {@link PhysicalPort} in BoD.
   *
   * @return List of unallocated physical ports
   */
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

  /**
   * Finds all physical ports that are visible for a user.
   *
   * @param user
   *          the user
   * @return list of user visible physical ports
   */
  Collection<PhysicalPort> findAllocatedForUser(RichUserDetails user);

  /**
   * Finds all physical ports that are visible for a user.
   *
   * @param user
   *          the user
   * @return list of user visible physical ports
   */
  Collection<PhysicalPort> findAllocatedEntriesForUser(RichUserDetails user, final int firstResult, final int sizeNo);

  long count();

  long countUnallocated();

  long countAllocatedForUser(RichUserDetails user);

  void delete(final PhysicalPort physicalPort);

  PhysicalPort find(final Long id);

  PhysicalPort findByName(final String name);

  void save(final PhysicalPort physicalPort);

  PhysicalPort update(final PhysicalPort physicalPort);

}