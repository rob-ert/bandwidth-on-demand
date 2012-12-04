/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
