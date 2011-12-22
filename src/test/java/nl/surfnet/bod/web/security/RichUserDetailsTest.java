package nl.surfnet.bod.web.security;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import nl.surfnet.bod.support.RichUserDetailsFactory;

import org.hamcrest.Matchers;
import org.junit.Test;

public class RichUserDetailsTest {

  @Test
  public void noGroupsShouldGiveAnEmptyListOfUserGroupIds() {
    RichUserDetails userDetails = new RichUserDetailsFactory().create();

    assertThat(userDetails.getUserGroupIds(), Matchers.<String>empty());
  }

  @Test
  public void shouldGiveBackTheUserGroupIds() {
    RichUserDetails userDetails = new RichUserDetailsFactory().addUserGroup("urn:first").addUserGroup("urn:second").create();

    assertThat(userDetails.getUserGroupIds(), contains("urn:first", "urn:second"));
  }

}
