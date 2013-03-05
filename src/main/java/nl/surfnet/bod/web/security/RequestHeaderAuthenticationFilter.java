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

import static com.google.common.base.Strings.nullToEmpty;

import java.util.Enumeration;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import nl.surfnet.bod.domain.oauth.VerifiedToken;
import nl.surfnet.bod.service.OAuthServerService;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.util.ShibbolethConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Optional;

public class RequestHeaderAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {

  private final Logger logger = LoggerFactory.getLogger(RequestHeaderAuthenticationFilter.class);

  private final Function<HttpServletRequest, String> immitateNameId = new Function<HttpServletRequest, String>() {
      @Override
      public String apply(HttpServletRequest request) {
        String user = nullToEmpty(request.getParameter("nameId"));
        return user.isEmpty() ? env.getImitateShibbolethUserId() : user;
      }
    };

  private final Function<HttpServletRequest, String> immitateDisplayName = new Function<HttpServletRequest, String>() {
      @Override
      public String apply(HttpServletRequest request) {
        String name = nullToEmpty(request.getParameter("displayName"));
        return name.isEmpty() ? env.getImitateShibbolethDisplayName() : name;
      }
    };

  private final Function<HttpServletRequest, String> immitateEmail = new Function<HttpServletRequest, String>() {
      @Override
      public String apply(HttpServletRequest request) {
        String email = nullToEmpty(request.getParameter("email"));
        return email.isEmpty() ? env.getImitateShibbolethEmail() : email;
      }
    };

  @Resource(name = "bodEnvironment")
  private Environment env;

  @Resource
  private OAuthServerService oAuthServerService;

  @Override
  protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
    if (isNsiRequest(request)) {
      return getPrincipalFromOauth2Header(request);
    }

    return getPrincipalFromHeaders(request);
  }

  private boolean isNsiRequest(HttpServletRequest request) {
    return request.getServletPath().equals("/nsi");
  }

  private Object getPrincipalFromHeaders(HttpServletRequest request) {
    String nameId = getRequestHeaderOrImitate(request, ShibbolethConstants.NAME_ID, immitateNameId);
    String displayName = getRequestHeaderOrImitate(request, ShibbolethConstants.DISPLAY_NAME, immitateDisplayName);
    String email = getRequestHeaderOrImitate(request, ShibbolethConstants.EMAIL, immitateEmail);

    logger.debug("Found Shibboleth name-id: '{}', displayName: '{}', email: {}", new Object[] {nameId, displayName, email});

    if (nameId.isEmpty() || displayName.isEmpty()) {
      return null;
    }

    return new RichPrincipal(nameId, displayName, email);
  }

  private RichPrincipal getPrincipalFromOauth2Header(HttpServletRequest request) {
    String authorizationHeader = request.getHeader("Authorization");

    if (authorizationHeader == null || !authorizationHeader.startsWith("bearer ")) {
      logger.warn("Could not find a OAuth2 authorization header");
      return null;
    }

    String accessToken = authorizationHeader.split(" ")[1];

    Optional<VerifiedToken> principal = oAuthServerService.getVerifiedToken(accessToken);
    logger.debug("Found principal {}", principal);

    return principal.transform(new Function<VerifiedToken, RichPrincipal>() {
        @Override
        public RichPrincipal apply(VerifiedToken token) {
          return new RichPrincipal(
              token.getPrincipal().getName(),
              token.getPrincipal().getAttributes().get("displayName"),
              token.getPrincipal().getAttributes().get("email"),
              token.getScopes());
        }
    }).orNull();
  }

  private String getRequestHeaderOrImitate(
      HttpServletRequest request, String header, Function<HttpServletRequest, String> imitateValue) {
    String value = nullToEmpty(request.getHeader(header));

    String headerValue = value.isEmpty() && env.getImitateShibboleth() ? imitateValue.apply(request) : value;

    return new String(headerValue.getBytes(Charsets.ISO_8859_1), Charsets.UTF_8);
  }

  @Override
  protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
    return "N/A";
  }

  protected void setEnvironment(Environment environment) {
    this.env = environment;
  }

}
