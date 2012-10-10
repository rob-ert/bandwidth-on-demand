package nl.surfnet.bod.web.oauth;

import java.security.Principal;
import java.util.Collection;
import java.util.Map;

public class AuthenticatedPrincipal implements Principal {

  private String name;
  private Collection<String> roles;
  private Map<String, Object> attributes;

  public Collection<String> getRoles() {
    return roles;
  }

  public Map<String, Object> getAttributes() {
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

  public void setAttributes(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  @Override
  public String toString() {
    return "AuthenticatedPrincipalImpl [name=" + name + ", roles=" + roles + ", attributes=" + attributes + "]";
  }

}
