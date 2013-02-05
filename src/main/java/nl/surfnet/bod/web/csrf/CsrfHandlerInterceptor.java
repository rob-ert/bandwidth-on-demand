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
package nl.surfnet.bod.web.csrf;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.surfnet.bod.web.base.MessageManager;

import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.google.common.collect.ImmutableList;

public class CsrfHandlerInterceptor extends HandlerInterceptorAdapter {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {

    if (requestCanChangeData(request)) {

      if (request.getSession().isNew()) {

        addInfoMessage(request, response);
        response.sendRedirect(request.getContextPath());
        return false;
      }

      String sessionToken = CsrfTokenManager.getTokenForSession(request.getSession());
      String requestToken = CsrfTokenManager.getTokenFromRequest(request);

      if (!sessionToken.equals(requestToken)) {

        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bad or missing CSRF token");
        return false;
      }
    }

    return true;
  }

  private void addInfoMessage(HttpServletRequest request, HttpServletResponse response) {
    FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
    flashMap.put(MessageManager.INFO_MESSAGES_KEY, "Your POST request has been ignored because your session timed out.");
    FlashMapManager flashMapManager = RequestContextUtils.getFlashMapManager(request);
    flashMapManager.saveOutputFlashMap(flashMap, request, response);
  }

  private boolean requestCanChangeData(HttpServletRequest request) {
    ImmutableList<String> changingMethods = ImmutableList.of("put", "post", "delete");

    return changingMethods.contains(request.getMethod().toLowerCase());
  }

}
