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
package nl.surfnet.bod.domain;

import javax.persistence.*;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

@Entity
public class BodAccount {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Version
  private long version;

  @Column(unique = true, nullable = false)
  private String nameId;

  private String authorizationServerAccessToken;

  public String getNameId() {
    return nameId;
  }

  public void setNameId(String nameId) {
    this.nameId = nameId;
  }

  public Optional<String> getAuthorizationServerAccessToken() {
    return Optional.fromNullable(Strings.emptyToNull(authorizationServerAccessToken));
  }

  public void setAuthorizationServerAccessToken(String authorizationServerAccessToken) {
    this.authorizationServerAccessToken = authorizationServerAccessToken;
  }

  public void removeAuthorizationServerAccessToken() {
    this.authorizationServerAccessToken = null;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

}
