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

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessToken {

  private Long id;
  private String token;
  private long expires;
  private List<String> scopes;
  private String resourceOwnerId;
  private Client client;

  public String getToken() {
    return token;
  }

  public long getExpires() {
    return expires;
  }

  public List<String> getScopes() {
    return scopes;
  }

  public String getResourceOwnerId() {
    return resourceOwnerId;
  }

  public Long getId() {
    return id;
  }

  public Client getClient() {
    return client;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public class Client {
    private String clientId;

    public String getClientId() {
      return clientId;
    }
  }

}
