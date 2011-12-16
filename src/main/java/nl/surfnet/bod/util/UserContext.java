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
package nl.surfnet.bod.util;

import static com.google.common.base.Preconditions.checkNotNull;

public class UserContext {

  private final String userName;
  private final String nameId;

  public UserContext(String nameId, String userName) {
    this.userName = checkNotNull(userName);
    this.nameId = checkNotNull(nameId);
  }

  public String getUserName() {
    return userName;
  }

  public String getNameId() {
    return nameId;
  }
}
