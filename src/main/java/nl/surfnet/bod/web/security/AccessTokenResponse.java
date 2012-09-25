/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package nl.surfnet.bod.web.security;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Bean representation of a Access Token response. See <a
 * href="https://tools.ietf.org/html/draft-ietf-oauth-v2-30#section-4.1.3">this
 * section</a> of the spec for more info.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class AccessTokenResponse {

  @JsonProperty("access_token")
  private String accessToken;
  @JsonProperty("token_type")
  private String tokenType;
  @JsonProperty("expires_in")
  private long expiresIn;
  @JsonProperty("refresh_token")
  private String refreshToken;
  private String scope;

  public AccessTokenResponse() {
  }

  public AccessTokenResponse(String accessToken, String tokenType, long expiresIn, String refreshToken, String scope) {
    this.accessToken = accessToken;
    this.tokenType = tokenType;
    this.expiresIn = expiresIn;
    this.refreshToken = refreshToken;
    this.scope = scope;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getTokenType() {
    return tokenType;
  }

  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }

  public long getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(long expiresIn) {
    this.expiresIn = expiresIn;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  @Override
  public String toString() {
    return "AccessTokenResponse [accessToken=" + accessToken + ", tokenType=" + tokenType + ", expiresIn=" + expiresIn
        + ", refreshToken=" + refreshToken + ", scope=" + scope + "]";
  }

}
