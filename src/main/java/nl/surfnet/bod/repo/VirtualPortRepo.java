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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import nl.surfnet.bod.domain.VirtualPort;

@Repository
public interface VirtualPortRepo extends JpaSpecificationExecutor<VirtualPort>, JpaRepository<VirtualPort, Long> {

  /**
   * Finds a {@link VirtualPort} by its ict manager label.
   * 
   * @param label
   *          The label to search for
   * @return {@link VirtualPort} or null when no match was found.
   */
  VirtualPort findByManagerLabel(String label);

  /**
   * Finds a {@link VirtualPort} by it user label
   * 
   * @param label
   *          Label to search for
   * @return {@link VirtualPort} of null when no match was found
   */
  VirtualPort findByUserLabel(String label);
}
