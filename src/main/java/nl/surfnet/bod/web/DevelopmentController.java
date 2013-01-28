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
package nl.surfnet.bod.web;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import nl.surfnet.bod.web.security.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("dev")
public class DevelopmentController {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Resource
  private ReloadableResourceBundleMessageSource messageSource;

  @RequestMapping(value = "refresh/messages", method = RequestMethod.GET)
  public String refreshMessageSource(HttpServletRequest request, RedirectAttributes model) {
    messageSource.clearCache();
    logger.info("Refresing messages");
    WebUtils.addInfoFlashMessage(model, messageSource, "info_dev_refresh", "Messages");

    return "redirect:" + request.getHeader("Referer");
  }

  @RequestMapping("refresh/roles")
  public String refreshGroups(HttpServletRequest request, RedirectAttributes model) {
    SecurityContextHolder.clearContext();
    logger.info("Refreshing roles");
    WebUtils.addInfoFlashMessage(model, messageSource, "info_dev_refresh", "Roles");

    return "redirect:" + request.getHeader("Referer");
  }

  @RequestMapping("/show/teams")
  public String list(final Model uiModel) {
    uiModel.addAttribute("teams", Security.getUserDetails().getUserGroups());

    return "shibboleth/teams";
  }

  @RequestMapping("/show/shibinfo")
  public String info() {
    return "shibboleth/info";
  }

  @RequestMapping(value = "error", method = RequestMethod.GET)
  public String error() {
    throw new RuntimeException("Something went wrong on purpose");
  }
}
