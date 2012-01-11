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
package nl.surfnet.bod.idd;

import java.util.Collection;

import nl.surfnet.bod.idd.generated.Klanten;

public interface IddClient {

  /**
   * Finds all klanten.
   * 
   * @return Collection<Klanten>
   */
  Collection<Klanten> getKlanten();

  /**
   * Finds one instance of a {@link Klanten} by the specified klantId. Assumes
   * that the klantId is unique, so matches only the first occurence of that id
   * in the list.
   * 
   * @see #getKlanten()
   * 
   * @param klantId
   *          Id to search for
   *          
   * @return {@link Klanten} or null when not machted.
   */
  Klanten getKlantById(final Long klantId);
}
