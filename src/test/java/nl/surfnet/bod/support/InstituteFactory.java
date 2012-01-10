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
package nl.surfnet.bod.support;

import nl.surfnet.bod.domain.Institute;

/**
 * Factory for the {@link Institute} class. Enables custom values by using the
 * supplied setters, otherwise a anonymous instance will be created.
 * 
 * @author Franky
 * 
 */
public class InstituteFactory {

  private int id = 1;
  private String name = "Customer One";
  private String shortName = "One";

  public Institute create() {
    Institute institute = new Institute(id, name, shortName);

    return institute;
  }

  public InstituteFactory setId(int id) {
    this.id = id;
    return this;
  }

  public InstituteFactory setName(String name) {
    this.name = name;
    return this;
  }

  public InstituteFactory setShortName(String shortName) {
    this.shortName = shortName;
    return this;
  }

}
