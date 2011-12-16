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
package nl.surfnet.bod.repo;

import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.domain.VirtualResourceGroup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface VirtualResourceGroupRepo extends JpaSpecificationExecutor<VirtualResourceGroup>,
    JpaRepository<VirtualResourceGroup, Long> {

  /**
   * Finds a {@link VirtualResourceGroup} by
   * {@link VirtualResourceGroup#getSurfConnextGroupName()}
   * 
   * @param surfConnextGroupName
   *          The name to search for
   * @return {@link VirtualResourceGroup} or null when no match was found.
   */
  VirtualResourceGroup findBySurfConnextGroupName(String surfConnextGroupName);

  /**
   * Finds a {@link VirtualResourceGroup} by
   * {@link VirtualResourceGroup#getName()}
   * 
   * @param Name
   *          The name to search for
   * @return {@link VirtualResourceGroup} or null when no match was found.
   */
  VirtualResourceGroup findByName(String name);

  /**
   * Finds {@link VirtualResourceGroup}s by a Collection of adminGroups
   * {@link VirtualResourceGroup#getSurfConnextGroupName())}
   * 
   * @param Collection
   *          <String> adminGroups to search for
   * 
   * @return List<VirtualResourceGroup> or empty list when no match was found.
   */
  List<VirtualResourceGroup> findBySurfConnextGroupNameIn(Collection<String> adminGroups);
}
