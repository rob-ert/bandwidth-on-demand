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
package nl.surfnet.bod.web.security;

import java.util.Collection;
import java.util.Collections;

import nl.surfnet.bod.domain.oauth.NsiScope;

public class RichPrincipal {

  private final String nameId;
  private final String displayName;
  private final String email;
  private final Collection<NsiScope> nsiScopes;

  public RichPrincipal(String nameId, String displayName, String email) {
    this(nameId, displayName, email, Collections.<NsiScope>emptyList());
  }

  public RichPrincipal(String nameId, String displayName, String email, Collection<NsiScope> nsiScopes) {
    this.nameId = nameId;
    this.displayName = displayName;
    this.email = email;
    this.nsiScopes = nsiScopes;
  }

  public String getNameId() {
    return nameId;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getEmail() {
    return email;
  }

  public Collection<NsiScope> getNsiScopes() {
    return nsiScopes;
  }

}