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

import nl.surfnet.bod.domain.UserGroup;

public class UserGroupFactory {

  private String id = "urn:emtpy";
  private String title = "";
  private String description = "";
  
  public UserGroup create() {
    UserGroup group = new UserGroup(id, title, description);
    
    return group;
  }
  
  public UserGroupFactory setId(String id) {
    this.id = id;
    return this;
  }
  
  public UserGroupFactory setDescription(String description) {
    this.description = description;
    return this;
  }
  
  public UserGroupFactory setTitle(String title) {
    this.title = title;
    return this;
  }

}
