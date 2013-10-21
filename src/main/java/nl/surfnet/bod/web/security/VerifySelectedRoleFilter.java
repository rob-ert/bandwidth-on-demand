/**
 * Copyright (c) 2012, 2013 SURFnet BV
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

import java.io.IOException;
import java.util.Collection;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.common.collect.Lists;

import nl.surfnet.bod.domain.BodRole;

import org.apache.log4j.NDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component("verifySelectedRoleFilter")
public class VerifySelectedRoleFilter extends OncePerRequestFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(VerifySelectedRoleFilter.class);

  private static final Collection<String> USER_PATHS = Lists.newArrayList(
      "/reservations",
      "/user",
      "/teams",
      "/virtualports",
      "/oauth2",
      "/request",
      "/advanced",
      "/report",
      "/logevents");

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (HttpMethod.GET.name().equalsIgnoreCase(request.getMethod())) {
      verifySelectedRole(request.getServletPath());
    }

    RichUserDetails userDetails = Security.getUserDetails();
    if (userDetails != null) {
      BodRole selectedRole = userDetails.getSelectedRole();
      NDC.push("user=" + userDetails.getNameId() + ";role=" + (selectedRole == null ? "<none>" : selectedRole.getRoleName()));
    } else {
      NDC.push("user=<none>");
    }
    try {
      if (LOGGER.isDebugEnabled()) {
        HttpSession session = request.getSession(false);
        if (session != null) {
          StringBuilder cookies = new StringBuilder();
          if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
              cookies.append(cookie.getName()).append('=').append(cookie.getValue()).append(';');
            }
          }
          LOGGER.debug("{}session {}, cookies {}", session.isNew() ? "NEW " : "", session.getId(), cookies);
        }
      }

      filterChain.doFilter(request, response);
    } finally {
      NDC.pop();
    }
  }

  protected void verifySelectedRole(String path) {
    if (isNocPath(path) && !Security.isSelectedNocRole()) {
      Security.switchToNocEngineer();
    }
    else if (isUserPath(path) && !Security.isSelectedUserRole()) {
      Security.switchToUser();
    }
    else if (isManagerPath(path) && !Security.isSelectedManagerRole()) {
      Security.switchToManager();
    }
    else if (isAppManagerPath(path) && !Security.isSelectedAppManagerRole()) {
      Security.switchToAppManager();
    }
  }

  private boolean isAppManagerPath(String path) {
    return path.startsWith("/appmanager");
  }

  private boolean isManagerPath(String path) {
    return path.startsWith("/manager");
  }

  private boolean isNocPath(final String path) {
    return path.startsWith("/noc");
  }

  private boolean isUserPath(final String path) {
    for (String userPath: USER_PATHS) {
      if (path.startsWith(userPath)) {
        return true;
      }
    }
    return false;
  }
}
