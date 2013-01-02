/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.web.security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.EnumSet;

import javax.servlet.http.HttpServletRequest;

import nl.surfnet.bod.domain.oauth.AuthenticatedPrincipal;
import nl.surfnet.bod.domain.oauth.NsiScope;
import nl.surfnet.bod.domain.oauth.VerifiedToken;
import nl.surfnet.bod.service.OAuthServerService;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.util.ShibbolethConstants;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Optional;

@RunWith(MockitoJUnitRunner.class)
public class RequestHeaderAuthenticationFilterTest {

  @InjectMocks
  private RequestHeaderAuthenticationFilter subject;

  @Mock
  private OAuthServerService oAuthServerServiceMock;

  @Test
  public void noShibbolethHeadersSetAndNotImitatingShouldGiveNull() {
    HttpServletRequest requestMock = getNonOAuth2RequestMock();

    subject.setEnvironment(new Environment(false, "urn:dummy", "Dummy", "dummy@dummy.com", "shiblogout"));

    Object principal = subject.getPreAuthenticatedPrincipal(requestMock);

    assertThat(principal, is(nullValue()));
  }

  @Test
  public void emptyShibbolethHeaderAndNotImitatingShouldGiveNull() {
    subject.setEnvironment(new Environment(false, "urn:dummy", "Dummy", "dummy@dummy.com", "shiblogout"));

    HttpServletRequest requestMock = getNonOAuth2RequestMock();
    when(requestMock.getHeader(ShibbolethConstants.NAME_ID)).thenReturn("fake");
    when(requestMock.getHeader(ShibbolethConstants.DISPLAY_NAME)).thenReturn("");

    Object principal = subject.getPreAuthenticatedPrincipal(requestMock);

    assertThat(principal, is(nullValue()));
  }

  @Test
  public void shibbolethHeadersShoulGiveAPrincipal() {
    subject.setEnvironment(new Environment(false, "urn:dummy", "Dummy", "dummy@dummy.com", "shiblogout"));

    HttpServletRequest requestMock = getNonOAuth2RequestMock();
    when(requestMock.getHeader(ShibbolethConstants.NAME_ID)).thenReturn("urn:truusvisscher");
    when(requestMock.getHeader(ShibbolethConstants.DISPLAY_NAME)).thenReturn("Truus Visscher");

    Object principal = subject.getPreAuthenticatedPrincipal(requestMock);

    assertThat(principal, is(instanceOf(RichPrincipal.class)));
    assertThat(((RichPrincipal) principal).getNameId(), is("urn:truusvisscher"));
  }

  @Test
  public void noShibbolethHeadersAndImitateShoulGiveAPrincipal() {
    HttpServletRequest requestMock = getNonOAuth2RequestMock();

    subject.setEnvironment(new Environment(true, "urn:dummy", "Dummy", "dummy@dummy.com", "shiblogout"));

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

  @Test
  public void diacriticalsShouldBeDisplayedCorrectly() {
    subject.setEnvironment(new Environment(false, "urn:dummy", "Dummy", "dummy@dummy.com", "shiblogout"));

    HttpServletRequest requestMock = getNonOAuth2RequestMock();
    when(requestMock.getHeader(ShibbolethConstants.NAME_ID)).thenReturn("urn:truusvisscher");
    when(requestMock.getHeader(ShibbolethConstants.DISPLAY_NAME)).thenReturn("Frank MÃ¶lder");

    Object principal = subject.getPreAuthenticatedPrincipal(requestMock);

    assertThat(principal, is(instanceOf(RichPrincipal.class)));
    assertThat(((RichPrincipal) principal).getDisplayName(), is("Frank Mölder"));
  }

  @Test
  public void immitatingAndHaveRequestParameters() {
    subject.setEnvironment(new Environment(true, "urn:dummy", "Dummy", "dummy@dummy.com", "shiblogout"));

    HttpServletRequest requestMock = getNonOAuth2RequestMock();
    when(requestMock.getHeader(ShibbolethConstants.NAME_ID)).thenReturn(null);
    when(requestMock.getHeader(ShibbolethConstants.DISPLAY_NAME)).thenReturn(null);
    when(requestMock.getParameter("nameId")).thenReturn("urn:Henk");
    when(requestMock.getParameter("displayName")).thenReturn("Henk");

    Object principal = subject.getPreAuthenticatedPrincipal(requestMock);

    assertThat(((RichPrincipal) principal).getNameId(), is("urn:Henk"));
    assertThat(((RichPrincipal) principal).getDisplayName(), is("Henk"));
  }

  @Test
  public void shouldFindPrincipalFromOAuthHeader() {
    String nameId = "urn:nl:surfguest:henk";
    String token = "1234-1234-abc";

    HttpServletRequest requestMock = getOAuth2RequestMock();
    when(requestMock.getHeader("Authorization")).thenReturn("bearer ".concat(token));

    AuthenticatedPrincipal oAuthPrincipal = new AuthenticatedPrincipal();
    oAuthPrincipal.setName(nameId);
    oAuthPrincipal.setAttributes(Collections.<String, String>emptyMap());
    VerifiedToken verifiedToken = new VerifiedToken(oAuthPrincipal, EnumSet.of(NsiScope.RELEASE));

    when(oAuthServerServiceMock.getVerifiedToken(token)).thenReturn(Optional.of(verifiedToken));

    Object principal = subject.getPreAuthenticatedPrincipal(requestMock);

    assertThat(((RichPrincipal) principal).getNameId(), is(nameId));
  }

  @Test
  public void shouldFailToVerifyToken() {
    String token = "1234-1234-abc";
    HttpServletRequest requestMock = getOAuth2RequestMock();
    when(requestMock.getHeader("Authorization")).thenReturn("bearer ".concat(token));

    when(oAuthServerServiceMock.getVerifiedToken(token)).thenReturn(Optional.<VerifiedToken>absent());

    Object principal = subject.getPreAuthenticatedPrincipal(requestMock);

    assertThat(principal, nullValue());
  }

  private HttpServletRequest getNonOAuth2RequestMock() {
    HttpServletRequest requestMock = mock(HttpServletRequest.class);
    when(requestMock.getServletPath()).thenReturn("/user");

    return requestMock;
  }

  private HttpServletRequest getOAuth2RequestMock() {
    HttpServletRequest requestMock = mock(HttpServletRequest.class);
    when(requestMock.getServletPath()).thenReturn("/nsi");

    return requestMock;
  }

}
