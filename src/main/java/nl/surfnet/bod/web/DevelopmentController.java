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
package nl.surfnet.bod.web;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.web.base.MessageManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

@Controller
@RequestMapping("/" + DevelopmentController.PAGE_URL)
public class DevelopmentController {

  static final String PAGE_URL = "dev";

  static final String REFRESH_PART = "/refresh/";

  static final String MESSAGES_PART = REFRESH_PART + "messages";
  static final String ROLES_PART = REFRESH_PART + "roles";

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Resource(name = "bodEnvironment")
  private Environment environment;

  @Resource
  private ReloadableResourceBundleMessageSource messageSource;

  @Resource
  private MessageManager messageManager;

  @RequestMapping(value = MESSAGES_PART)
  public String refreshMessageSource(HttpServletRequest request, RedirectAttributes model) {
    if (environment.isDevelopment()) {
      refreshMessageSource();
      messageManager.addInfoFlashMessage(model, "info_dev_refresh", "Messages");
    }

    return getRedirect(request);
  }

  private void refreshMessageSource() {
    messageSource.clearCache();
    logger.info("Refresing messages");
  }

  @RequestMapping(value = ROLES_PART)
  public String refreshGroups(HttpServletRequest request, RedirectAttributes model) {
    if (environment.isDevelopment()) {
      refreshGroups();
      messageManager.addInfoFlashMessage(model, "info_dev_refresh", "Roles/User");
    }

    RequestParameter[] parameters = Iterables.toArray(Optional.presentInstances(
      ImmutableList.of(
        RequestParameter.unwrap("nameId", request),
        RequestParameter.unwrap("displayName", request),
        RequestParameter.unwrap("email", request))),
      RequestParameter.class);

    return getRedirect(request, parameters);
  }

  private void refreshGroups() {
    SecurityContextHolder.clearContext();
    logger.info("Refreshing roles");
  }

  private String getRedirect(HttpServletRequest request, RequestParameter... parameters) {
    StringBuilder redirectBuilder = new StringBuilder("redirect:");
    redirectBuilder.append(request.getHeader("Referer").split("\\?")[0]);

    if (parameters.length > 0) {
      redirectBuilder.append("?");
      Joiner.on("&").appendTo(redirectBuilder, parameters);
    }

    return redirectBuilder.toString();
  }

  protected void setMessageManager(MessageManager messageManager) {
    this.messageManager = messageManager;
  }

  private static class RequestParameter {
    private final String name;
    private final String value;

    private static Optional<RequestParameter> unwrap(String name, HttpServletRequest request) {
      String value = Strings.nullToEmpty(request.getParameter(name));

      if (value.isEmpty()) {
        return Optional.absent();
      } else {
        return Optional.of(new RequestParameter(name, value));
      }
    }

    private RequestParameter(String name, String value) {
      this.name = name;
      this.value = value;
    }

    @Override
    public String toString() {
      return name+"="+value;
    }

  }

}