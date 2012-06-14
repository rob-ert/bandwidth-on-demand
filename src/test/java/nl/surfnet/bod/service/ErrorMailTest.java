package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import nl.surfnet.bod.service.Emails.ErrorMail;
import nl.surfnet.bod.support.RichUserDetailsFactory;

import org.junit.Test;

public class ErrorMailTest {

  @Test
  public void errorMailShouldContainUserAndError() {
    HttpServletRequest request = mock(HttpServletRequest.class);

    when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080"));

    String bodyText = ErrorMail.body(new RichUserDetailsFactory().setDisplayname("Truus").setEmail("truus@henk.nl")
        .create(), new RuntimeException("Something went wrong"), request);

    assertThat(bodyText, containsString("Something went wrong"));
    assertThat(bodyText, containsString("User: Truus (truus@henk.nl)"));
  }

  @Test
  public void errorMailWithoutALoggedInUser() {
    HttpServletRequest request = mock(HttpServletRequest.class);

    when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080"));

    String bodyText = ErrorMail.body(null, new RuntimeException("Something went wrong"), request);

    assertThat(bodyText, containsString("User: Unknown"));
    assertThat(bodyText, containsString("Username: Unknown"));
    assertThat(bodyText, containsString("Something went wrong"));
  }
}
