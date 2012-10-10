package nl.surfnet.bod.web.oauth;

import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

public class VerifyTokenResponse {

  private String audience;
  private List<String> scopes;
  private AuthenticatedPrincipal principal;
  @JsonProperty("expires_in")
  private Long expiresIn;
  private String error;

  public String getAudience() {
    return audience;
  }

  public List<String> getScopes() {
    return scopes;
  }

  public String getError() {
    return error;
  }

  public Long getExpiresIn() {
    return expiresIn;
  }

  public AuthenticatedPrincipal getPrincipal() {
    return principal;
  }

}
