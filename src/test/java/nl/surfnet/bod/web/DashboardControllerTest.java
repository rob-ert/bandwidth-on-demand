package nl.surfnet.bod.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.junit.Test;

public class DashboardControllerTest {

  private DashboardController subject = new DashboardController();

  @Test
  public void aNocEngineerShouldBeRedirectToNocPage() {
    RichUserDetails user = new RichUserDetailsFactory().addNocAuthority().create();
    Security.setUserDetails(user);

    String page = subject.index();

    assertThat(page, is("redirect:noc"));
  }

  @Test
  public void aIctManagerShouldBeRedirectToManagerPage() {
    RichUserDetails user = new RichUserDetailsFactory().addManagerAuthority().create();
    Security.setUserDetails(user);

    String page = subject.index();

    assertThat(page, is("redirect:manager"));
  }

  @Test
  public void aUserShouldGoToIndex() {
    RichUserDetails user = new RichUserDetailsFactory().addUserAuthority().create();
    Security.setUserDetails(user);

    String page = subject.index();

    assertThat(page, is("index"));
  }

  @Test
  public void aNoBodyShouldGoTo() {
    RichUserDetails user = new RichUserDetailsFactory().create();
    Security.setUserDetails(user);

    String page = subject.index();

    assertThat(page, is("noUserRole"));
  }

}
