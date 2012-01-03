package nl.surfnet.bod.web.security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import nl.surfnet.bod.support.RichUserDetailsFactory;

import org.junit.Test;

public class SecurityTest {

  @Test
  public void userIsNotMemberOf() {
    RichUserDetails user = new RichUserDetailsFactory().create();
    Security.setUserDetails(user);

    assertThat(Security.isUserMemberOf("urn:group"), is(false));
    assertThat(Security.isUserNotMemberOf("urn:group"), is(true));
  }

  @Test
  public void userIsMemberOf() {
    RichUserDetails user = new RichUserDetailsFactory().addUserGroup("urn:group").create();
    Security.setUserDetails(user);

    assertThat(Security.isUserMemberOf("urn:group"), is(true));
    assertThat(Security.isUserNotMemberOf("urn:group"), is(false));
  }

}
