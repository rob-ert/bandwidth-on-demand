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

import javax.servlet.http.HttpServletRequest;

import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.util.ShibbolethConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

public class RequestHeaderAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {

  private final Logger logger = LoggerFactory.getLogger(RequestHeaderAuthenticationFilter.class);

  @Autowired
  private Environment env;

  @Override
  protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
    String nameId = getRequestHeaderOrImitate(request, ShibbolethConstants.NAME_ID, env.getImitateShibbolethUserId());
    String displayName = getRequestHeaderOrImitate(request, ShibbolethConstants.DISPLAY_NAME,
        env.getImitateShibbolethDisplayName());
    String email = getRequestHeaderOrImitate(request, ShibbolethConstants.EMAIL, env.getImitateShibbolethEmail());

    logger.debug("Found Shibboleth name-id: '{}', displayName: '{}', email: {}", new String [] {nameId, displayName, email});

    if (nameId.isEmpty() || displayName.isEmpty()) {
      return null;
    }

    return new RichPrincipal(nameId, displayName, email);
  }

  private String getRequestHeaderOrImitate(HttpServletRequest request, String header, String imitateValue) {
    String value = nullToEmpty(request.getHeader(header));

    return value.isEmpty() && env.getImitateShibboleth() ? imitateValue : value;
  }

  @Override
  protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
    return "N/A";
  }

  protected void setEnvironment(Environment environment) {
    this.env = environment;
  }

}
