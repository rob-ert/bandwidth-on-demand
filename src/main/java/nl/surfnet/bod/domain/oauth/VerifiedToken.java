package nl.surfnet.bod.domain.oauth;

import java.util.Collection;

public class VerifiedToken {

  private final AuthenticatedPrincipal principal;
  private final Collection<NsiScope> scopes;

  public VerifiedToken(AuthenticatedPrincipal principal, Collection<NsiScope> scopes) {
    super();
    this.principal = principal;
    this.scopes = scopes;
  }

  public AuthenticatedPrincipal getPrincipal() {
    return principal;
  }

  public Collection<NsiScope> getScopes() {
    return scopes;
  }

}
