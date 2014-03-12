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
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;

/**
 * Does not 'commence', only handles requests that did not carry a valid oAuth token
 * Decides whether to return a plain 403, or to send humans to the splash page
 *
 * @see Http403ForbiddenEntryPoint
 */
public class BodAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private static final Logger LOG = LoggerFactory.getLogger(BodAuthenticationEntryPoint.class);

  private String splashPath;

  private Set<String> machinePaths;

  public BodAuthenticationEntryPoint(String splashPath, Set<String> machinePaths) {
    this.splashPath = splashPath;
    this.machinePaths = machinePaths;
  }

  @Override
  public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException authException) throws IOException, ServletException {

    final String requestedPath = request.getServletPath() + request.getPathInfo();

    boolean isMachinePath = false;
    for (String machinePath: machinePaths){
      if (requestedPath.startsWith(machinePath)) {
        isMachinePath = true;
        break;
      }
    }
    LOG.debug("Request for {} matches machine path: {}", requestedPath, isMachinePath);

    if (isMachinePath) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN, "You did not include a valid oAuth token in your request");
    } else { // redirect to splash page
      response.sendRedirect( request.getContextPath() + splashPath);
    }
  }
}
