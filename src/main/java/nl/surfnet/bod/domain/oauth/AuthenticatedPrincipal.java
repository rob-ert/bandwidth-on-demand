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
