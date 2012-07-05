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
package nl.surfnet.bod.web.view;

import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.domain.VirtualResourceGroup;

public class UserGroupView implements Comparable<UserGroupView> {
  private final String name;
  private final String description;
  private final String surfconextGroupId;
  private final boolean existing;

  public UserGroupView(UserGroup userGroup) {
    this.name = userGroup.getName();
    this.description = userGroup.getDescription();
    this.surfconextGroupId = userGroup.getId();
    this.existing = false;
  }

  public UserGroupView(VirtualResourceGroup vrg) {
    this.name = vrg.getName();
    this.description = vrg.getDescription();
    this.surfconextGroupId = vrg.getSurfconextGroupId();
    this.existing = true;
  }

  @Override
  public int compareTo(UserGroupView other) {
    if (this.equals(other)) {
      return 0;
    }
    else {
      return this.getName().compareTo(other.getName());
    }
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getSurfconextGroupId() {
    return surfconextGroupId;
  }

  public boolean isExisting() {
    return existing;
  }

}
