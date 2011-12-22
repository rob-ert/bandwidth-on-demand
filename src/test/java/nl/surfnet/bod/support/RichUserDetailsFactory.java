package nl.surfnet.bod.support;

import java.util.Collection;

import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.springframework.security.core.GrantedAuthority;

import com.google.common.collect.Lists;

public class RichUserDetailsFactory {

  private String username = "urn:guest:truus";
  private String displayName = "Truus Visscher";
  private Collection<GrantedAuthority> authorities = Lists.newArrayList();
  private Collection<UserGroup> userGroups = Lists.newArrayList();

  public RichUserDetails create() {
    return new RichUserDetails(username, displayName, authorities, userGroups);
  }

  public RichUserDetailsFactory addUserGroup(String groupId) {
    userGroups.add(new UserGroupFactory().setId(groupId).create());
    return this;
  }

}
