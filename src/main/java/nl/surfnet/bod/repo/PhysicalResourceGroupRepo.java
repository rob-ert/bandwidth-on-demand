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

import nl.surfnet.bod.domain.PhysicalResourceGroup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PhysicalResourceGroupRepo extends JpaSpecificationExecutor<PhysicalResourceGroup>,
    JpaRepository<PhysicalResourceGroup, Long> {

  /**
   * Finds {@link PhysicalResourceGroup}s by a Collection of adminGroups
   * {@link PhysicalResourceGroup#getAdminGroup()}
   * 
   * @param Collection
   *          <String> adminGroups to search for
   * 
   * @return List<PhysicalResourceGroup> or empty collection when no match was
   *         found.
   */
  List<PhysicalResourceGroup> findByAdminGroupIn(Collection<String> adminGroups);

  /**
   * Finds a {@link PhysicalResourceGroup} by
   * {@link PhysicalResourceGroup#getName()}
   * 
   * @param name
   *          The name to search for
   * @return {@link PhysicalResourceGroup} or null when no match was found.
   */
  PhysicalResourceGroup findByName(String name);
}
