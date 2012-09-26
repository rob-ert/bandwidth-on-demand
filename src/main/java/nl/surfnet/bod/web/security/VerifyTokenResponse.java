package nl.surfnet.bod.web.security;

import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

public class VerifyTokenResponse {

  /*
   * The application that is the intended target of the token.
   */
  private String audience;

  private List<String> scopes;

  private AccessToken.Principal principal;

  @JsonProperty("expires_in")
  private Long expiresIn;

  private String error;

  public VerifyTokenResponse() {
  }

  public VerifyTokenResponse(String error) {
    this.error = error;
  }

  public VerifyTokenResponse(String audience, List<String> scopes, AccessToken.Principal principal, Long expiresIn) {
    this.audience = audience;
    this.scopes = scopes;
    this.principal = principal;
    this.expiresIn = expiresIn;
  }

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

  public AccessToken.Principal getPrincipal() {
    return principal;
  }

  public void setPrincipal(AccessToken.Principal principal) {
    this.principal = principal;
  }
}

