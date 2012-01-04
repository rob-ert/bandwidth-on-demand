package nl.surfnet.bod.web.security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.util.ShibbolethConstants;

import org.junit.Test;

public class RequestAttributeAuthenticationFilterTest {

  private RequestAttributeAuthenticationFilter subject = new RequestAttributeAuthenticationFilter();

  @Test
  public void noShibbolethHeadersSetAndNotImitatingShouldGiveNull() {
    HttpServletRequest requestMock = mock(HttpServletRequest.class);
    subject.setEnvironment(new Environment(false, "urn:dummy", "Dummy"));

    Object principal = subject.getPreAuthenticatedPrincipal(requestMock);

    assertThat(principal, is(nullValue()));
  }

  @Test
  public void emptyShibbolethHeaderAndNotImitatingShouldGiveNull() {
    HttpServletRequest requestMock = mock(HttpServletRequest.class);
    subject.setEnvironment(new Environment(false, "urn:dummy", "Dummy"));

    when(requestMock.getAttribute(ShibbolethConstants.NAME_ID)).thenReturn("fake");
    when(requestMock.getAttribute(ShibbolethConstants.DISPLAY_NAME)).thenReturn("");

    Object principal = subject.getPreAuthenticatedPrincipal(requestMock);

    assertThat(principal, is(nullValue()));
  }

  @Test
  public void shibbolethHeadersShoulGiveAPrincipal() {
    HttpServletRequest requestMock = mock(HttpServletRequest.class);
    subject.setEnvironment(new Environment(false, "urn:dummy", "Dummy"));

    when(requestMock.getAttribute(ShibbolethConstants.NAME_ID)).thenReturn("urn:truusvisscher");
    when(requestMock.getAttribute(ShibbolethConstants.DISPLAY_NAME)).thenReturn("Truus Visscher");

    Object principal = subject.getPreAuthenticatedPrincipal(requestMock);

    assertThat(principal, is(instanceOf(RichPrincipal.class)));
    assertThat(((RichPrincipal) principal).getNameId(), is("urn:truusvisscher"));
  }

  @Test
  public void noShibbolethHeadersAndImitateShoulGiveAPrincipal() {
    HttpServletRequest requestMock = mock(HttpServletRequest.class);
    subject.setEnvironment(new Environment(true, "urn:dummy", "Dummy"));

    Object principal = subject.getPreAuthenticatedPrincipal(requestMock);

    assertThat(principal, is(instanceOf(RichPrincipal.class)));
    assertThat(((RichPrincipal) principal).getNameId(), is("urn:dummy"));
  }

  @Test
  public void credentialsShouldNotBeAvailable() {
    Object credentials = subject.getPreAuthenticatedCredentials(mock(HttpServletRequest.class));

    assertThat(credentials, is(instanceOf(String.class)));
    assertThat(((String) credentials), is("N/A"));
  }

}
