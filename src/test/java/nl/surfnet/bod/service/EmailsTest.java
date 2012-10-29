package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import nl.surfnet.bod.domain.VirtualPortRequestLink;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.VirtualPortRequestLinkFactory;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.junit.Test;

public class EmailsTest {

  @Test
  public void requestVirtualPortMailWithEmail() {
    RichUserDetails from = new RichUserDetailsFactory().setDisplayname("Henk").setEmail("henk@henk.nl").create();
    VirtualPortRequestLink requestLink = new VirtualPortRequestLinkFactory().create();
    String link = "http://localhost";

    String body = Emails.VirtualPortRequestMail.body(from, requestLink, link);

    assertThat(body, containsString("From: Henk (henk@henk.nl)"));
  }

  @Test
  public void requestVirtualPortMailWithoutEmail() {
    RichUserDetails from = new RichUserDetailsFactory().setDisplayname("Henk").setEmail(null).create();
    VirtualPortRequestLink requestLink = new VirtualPortRequestLinkFactory().create();
    String link = "http://localhost";

    String body = Emails.VirtualPortRequestMail.body(from, requestLink, link);

    assertThat(body, containsString("From: Henk (Unknown Email)"));
  }
}
