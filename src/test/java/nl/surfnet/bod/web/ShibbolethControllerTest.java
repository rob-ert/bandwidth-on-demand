package nl.surfnet.bod.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;

import java.util.Collection;

import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.web.security.Security;

import org.junit.Test;

public class ShibbolethControllerTest {

  private ShibbolethController subject = new ShibbolethController();

  @SuppressWarnings("unchecked")
  @Test
  public void test() {
    Security.setUserDetails(new RichUserDetailsFactory().addUserGroup("urn:group").create());

    ModelStub model = new ModelStub();

    subject.list(model);

    assertThat(model.asMap(), hasKey("groups"));
    assertThat((Collection<UserGroup>) model.asMap().get("groups"), hasSize(1));
  }

}
