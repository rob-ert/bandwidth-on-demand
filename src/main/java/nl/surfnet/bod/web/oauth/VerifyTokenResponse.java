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

  public void setAudience(String audience) {
    this.audience = audience;
  }

  public List<String> getScopes() {
    return scopes;
  }

  public void setScopes(List<String> scopes) {
    this.scopes = scopes;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public Long getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(Long expiresIn) {
    this.expiresIn = expiresIn;
  }

  public AuthenticatedPrincipal getPrincipal() {
    return principal;
  }

  public void setPrincipal(AuthenticatedPrincipal principal) {
    this.principal = principal;
  }

}
