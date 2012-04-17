package nl.surfnet.bod.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.junit.Test;
import org.springframework.ui.Model;

public class DetermineRoleControllerTest {

  private DetermineRoleController subject = new DetermineRoleController();

  @Test
  public void aNocEngineerShouldBeRedirectToNocPage() {
    RichUserDetails user = new RichUserDetailsFactory().addNocRole().create();
    Security.setUserDetails(user);
    Model model = new ModelStub();

    String page = subject.index(model);

    assertThat(page, is("redirect:noc"));
  }

  @Test
  public void aIctManagerShouldBeRedirectToManagerPage() {
    RichUserDetails user = new RichUserDetailsFactory().addManagerRole().create();
    Security.setUserDetails(user);
    Model model = new ModelStub();

    String page = subject.index(model);

    assertThat(page, is("redirect:manager"));
  }

  @Test
  public void aUserShouldGoToIndex() {
    RichUserDetails user = new RichUserDetailsFactory().addUserRole().create();
    Security.setUserDetails(user);
    Model model = new ModelStub();

    String page = subject.index(model);

    assertThat(page, is("redirect:user"));
  }

}
