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

import nl.surfnet.bod.domain.Institute;

public interface InstituteService {

  /**
   * Finds the {@link Institute} related to the given Id.
   * 
   * @param id
   *          Id to search for.
   * @return {@link Institute} related to the id
   */
  Institute find(Long id);

  /**
   * Finds all {@link Institute}s which are aligned with IDD.
   * 
   * @return
   */
  Collection<Institute> findAlignedWithIDD();

  /**
   * Retrieves all {@link Institute}s from the external IDD system, updates and
   * persist them. All {@link Institute}s that are present in BoD but not in the
   * IDD system anymore are marked as <strong>not aligned</strong> with IDD
   * {@link Institute#isAlignedWithIDD()}. All other institutes are marked
   * alignedWithIDD.
   */
  void refreshInstitutes();
}
