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
package nl.surfnet.bod.web.csrf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.google.common.collect.ImmutableList;

public class CsrfHandlerInterceptor extends HandlerInterceptorAdapter {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

    if (requestCanChangeData(request)) {
      String sessionToken = CsrfTokenManager.getTokenForSession(request.getSession());
      String requestToken = CsrfTokenManager.getTokenFromRequest(request);

      if (!sessionToken.equals(requestToken)) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bad or missing CSRF token");
        return false;
      }
    }

    return true;
  }

  private boolean requestCanChangeData(HttpServletRequest request) {
    ImmutableList<String> changingMethods = ImmutableList.of("put", "post", "delete");

    return changingMethods.contains(request.getMethod().toLowerCase());
  }

}
