package nl.surfnet.bod.web.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.google.common.base.Objects;

@SuppressWarnings("serial")
public class RichUserDetails implements UserDetails {

  private final String username;
  private final String displayName;
  private final Collection<GrantedAuthority> authorities;

  public RichUserDetails(String username, String displayName, Collection<GrantedAuthority> authorities) {
    this.username = username;
    this.displayName = displayName;
    this.authorities = authorities;
  }

  @Override
  public Collection<GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return "N/A";
  }

  @Override
  public String getUsername() {
    return username;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getNameId() {
    return getUsername();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(RichUserDetails.class).add("nameId", getNameId())
        .add("displayName", getDisplayName()).toString();
  }

}
