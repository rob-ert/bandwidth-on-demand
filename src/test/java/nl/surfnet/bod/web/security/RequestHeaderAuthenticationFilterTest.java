/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
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
