package nl.surfnet.bod.web.security;

import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.service.GroupService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class RichUserDetailsService implements AuthenticationUserDetailsService {

  private static final String NOC_ENGINEER_GROUP = "urn:collab:group:surfteams.nl:nl:surfnet:diensten:noc-engineer";

  private final Logger logger = LoggerFactory.getLogger(RichUserDetailsService.class);

  @Autowired
  private GroupService groupService;

  @Override
  public UserDetails loadUserDetails(Authentication token) throws UsernameNotFoundException {
    RichPrincipal principal = (RichPrincipal) token.getPrincipal();

    Collection<UserGroup> groups = groupService.getGroups(principal.getNameId());
    logger.debug("Found groups: '{}' for name-id: '{}'", groups, principal.getNameId());

    List<GrantedAuthority> authorities = Lists.newArrayList();
    if (containsNocEngineerGroup(groups)) {
      authorities.add(new GrantedAuthority() {
        @Override
        public String getAuthority() {
          return "NOC_ENGINEER";
        }
      });
    }

    return new RichUserDetails(principal.getNameId(), principal.getDisplayName(), authorities);
  }

  private boolean containsNocEngineerGroup(Collection<UserGroup> groups) {
    return Iterables.any(groups, new Predicate<UserGroup>() {
      @Override
      public boolean apply(UserGroup group) {
        return NOC_ENGINEER_GROUP.equals(group.getId());
      }
    });
  }
}
