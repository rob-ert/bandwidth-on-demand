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

import java.util.Map;

import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.security.Security.RoleEnum;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequestMapping("/")
@Controller
public class DetermineRoleController {

  @RequestMapping(method = RequestMethod.GET)
  public String index(Model model, RedirectAttributes redirectAttributes) {

    preserveFlashMessages(model, redirectAttributes);

    if (Security.isSelectedNocRole()) {
      return RoleEnum.NOC_ENGINEER.getViewName();
    }

    if (Security.isSelectedManagerRole()) {
      return RoleEnum.ICT_MANAGER.getViewName();
    }

    return RoleEnum.USER.getViewName();
  }

  private void preserveFlashMessages(Model model, RedirectAttributes redirectAttributes) {
    preserveFlashAttributes(model, redirectAttributes, WebUtils.INFO_MESSAGES_KEY);
    preserveFlashAttributes(model, redirectAttributes, WebUtils.WARN_MESSAGES_KEY);
    preserveFlashAttributes(model, redirectAttributes, WebUtils.ERROR_MESSAGES_KEY);
  }

  private void preserveFlashAttributes(Model model, RedirectAttributes redirectAttributes, String key) {
    Map<String, Object> modelMap = model.asMap();

    if (modelMap.containsKey(key)) {
      Object messages = modelMap.remove(key);
      redirectAttributes.addFlashAttribute(key, messages);
    }
  }


}
