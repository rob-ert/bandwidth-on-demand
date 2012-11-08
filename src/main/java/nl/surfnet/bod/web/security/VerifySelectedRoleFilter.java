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

import java.io.IOException;
import java.util.Collection;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Component("verifySelectedRoleFilter")
public class VerifySelectedRoleFilter extends OncePerRequestFilter implements Filter {

  private static final Collection<String> USER_PATHS = Lists.newArrayList(
      "/reservations",
      "/user",
      "/teams",
      "/virtualports",
      "/oauth2",
      "/request",
      "/advanced",
      "/logevents");

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    if (request.getMethod() == HttpMethod.GET.name()) {
      String path = request.getServletPath();
      verifySelectedRole(path);
    }

    filterChain.doFilter(request, response);
  }

  protected void verifySelectedRole(String path) {
    if (isNocPath(path) && !Security.isSelectedNocRole()) {
      Security.switchToNocEngineer();
    } else if (isUserPath(path) && !Security.isSelectedUserRole()) {
      Security.switchToUser();
    } else if (isManagerPath(path) && !Security.isSelectedManagerRole()) {
      Security.switchToManager();
    }
  }

  private boolean isManagerPath(String path) {
    return path.startsWith("/manager");
  }

  private boolean isNocPath(final String path) {
    return path.startsWith("/noc");
  }

  private boolean isUserPath(final String path) {
    return Iterables.any(USER_PATHS, new Predicate<String>(){
      @Override
      public boolean apply(String userPath) {
        return path.startsWith(userPath);
      }
    });
  }

}
