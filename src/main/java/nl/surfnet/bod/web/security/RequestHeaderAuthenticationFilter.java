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

import static com.google.common.base.Strings.nullToEmpty;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.util.ShibbolethConstants;
import nl.surfnet.bod.web.oauth.AuthenticatedPrincipal;
import nl.surfnet.bod.web.oauth.OAuthServerService;

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

  @Resource
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

    Optional<AuthenticatedPrincipal> principal = oAuthServerService.getAuthenticatedPrincipal(accessToken);
    logger.debug("Found principal {}", principal);

    return principal.transform(new Function<AuthenticatedPrincipal, RichPrincipal>() {
        @Override
        public RichPrincipal apply(AuthenticatedPrincipal ap) {
          return new RichPrincipal(ap.getName(), ap.getAttributes().get("displayName"), ap.getAttributes().get("email"));
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
