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

import java.io.IOException;
import java.net.URISyntaxException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.util.ShibbolethConstants;
import nl.surfnet.bod.web.oauth.OAuth2Helper;
import nl.surfnet.bod.web.oauth.VerifyTokenResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import com.google.common.base.Charsets;
import com.google.common.base.Function;

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

    try {
      String accessToken = authorizationHeader.split(" ")[1];
      String uri = new URIBuilder(env.getOauthServerUrl().concat("/v1/tokeninfo"))
        .addParameter("access_token", accessToken).build().toASCIIString();

      DefaultHttpClient client = new DefaultHttpClient();
      HttpGet httpGet = new HttpGet(uri);
      httpGet.addHeader(OAuth2Helper.getBasicAuthorizationHeader(env.getResourceKey(), env.getResourceSecret()));

      HttpResponse response = client.execute(httpGet);

      if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
        logger.warn("Could not verify access_token for NSI request");
        httpGet.releaseConnection();
        return null;
      }

      String jsonResponse = EntityUtils.toString(response.getEntity(), Charsets.UTF_8);
      VerifyTokenResponse token = new ObjectMapper().readValue(jsonResponse, VerifyTokenResponse.class);

      logger.debug("Found principal with name-id {}", token.getPrincipal().getName());

      return new RichPrincipal(token.getPrincipal().getName(), "dummy", "dummy@dummy.nl");
    }
    catch (URISyntaxException | IOException e) {
      logger.error("Could not verify the accessToken for nsi request", e);
      return null;
    }
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
