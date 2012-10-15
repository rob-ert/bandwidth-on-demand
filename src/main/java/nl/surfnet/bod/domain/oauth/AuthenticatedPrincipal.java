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
package nl.surfnet.bod.domain.oauth;

import java.security.Principal;
import java.util.Collection;
import java.util.Map;

import com.google.common.base.Objects;

public class AuthenticatedPrincipal implements Principal {

  private String name;
  private Collection<String> roles;
  private Map<String, String> attributes;

  public Collection<String> getRoles() {
    return roles;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setRoles(Collection<String> roles) {
    this.roles = roles;
  }

  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("name", name).add("roles", roles).add("attributes", attributes).toString();
  }

}
