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
package nl.surfnet.bod.web.manager;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.ActivationEmailLink;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.security.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.ImmutableList;

@RequestMapping(ActivationEmailController.ACTIVATION_MANAGER_PATH)
@Controller
public class ActivationEmailController {

  public static final String ACTIVATION_MANAGER_PATH = "/manager/activate";

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Resource
  private PhysicalResourceGroupService physicalResourceGroupService;

  @Resource
  private MessageSource messageSource;

  @RequestMapping(value = "/{uuid}", method = RequestMethod.GET)
  public String activateEmail(@PathVariable String uuid, Model model, RedirectAttributes redirectAttrs) {
    ActivationEmailLink link = physicalResourceGroupService.findActivationLink(uuid);

    if (link == null || link.getSourceObject() == null) {
      redirectAttrs.addFlashAttribute(WebUtils.INFO_MESSAGES_KEY, ImmutableList.of("Activation link is not valid"));
      return "redirect:/";
    }

    PhysicalResourceGroup physicalResourceGroup = link.getSourceObject();

    if (!Security.isManagerMemberOf(physicalResourceGroup)) {
      WebUtils.addInfoFlashMessage(redirectAttrs, messageSource, "info_activation_request_notallowed", Security
          .getUserDetails().getDisplayName(), physicalResourceGroup.getName());

      log.debug("Manager [{}] has no right to access physical resourcegroup: {}", Security.getUserDetails()
          .getUsername(), physicalResourceGroup.getName());

      return "redirect:/";
    }

    model.addAttribute("link", link);
    model.addAttribute("physicalResourceGroup", physicalResourceGroup);

    Security.switchToManager(physicalResourceGroup);

    if (link.isActivated()) {
      return "manager/linkActive";
    }
    else if (emailHasChanged(link)) {
      return "manager/linkChanged";
    }
    else if (link.isValid()) {
      physicalResourceGroupService.activate(link);

      return "manager/emailConfirmed";
    }

    return "manager/linkNotValid";
  }

  private boolean emailHasChanged(ActivationEmailLink link) {
    return !link.getSourceObject().getManagerEmail().equals(link.getToEmail());
  }

  @RequestMapping(method = RequestMethod.POST)
  public String create(PhysicalResourceGroup physicalResourceGroup) {
    PhysicalResourceGroup foundPhysicalResourceGroup = physicalResourceGroupService.find(physicalResourceGroup.getId());

    physicalResourceGroupService.sendActivationRequest(foundPhysicalResourceGroup);

    return "manager/index";
  }
}
