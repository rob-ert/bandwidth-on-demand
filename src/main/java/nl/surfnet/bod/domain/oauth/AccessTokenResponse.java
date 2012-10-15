package nl.surfnet.bod.domain.oauth;

import org.codehaus.jackson.annotate.JsonProperty;

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

  public String getAccessToken() {
    return accessToken;
  }

  public String getTokenType() {
    return tokenType;
  }

  public long getExpiresIn() {
    return expiresIn;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public String getScope() {
    return scope;
  }

  @Override
  public String toString() {
    return "AccessTokenResponse [accessToken=" + accessToken + ", tokenType=" + tokenType + ", expiresIn=" + expiresIn
        + ", refreshToken=" + refreshToken + ", scope=" + scope + "]";
  }

}
