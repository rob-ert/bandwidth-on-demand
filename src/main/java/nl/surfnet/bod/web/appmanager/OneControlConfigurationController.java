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
package nl.surfnet.bod.web.appmanager;

import javax.annotation.Resource;

import nl.surfnet.bod.nbi.onecontrol.OneControlInstance;
import nl.surfnet.bod.nbi.onecontrol.OneControlService;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@Profile({ "onecontrol", "onecontrol-offline" })
@RequestMapping("/appmanager/onecontrol")
public class OneControlConfigurationController {

  @Resource private OneControlInstance oneControlInstance;
  @Resource private OneControlService oneControlService;

  @RequestMapping("/configuration")
  public String configuration(Model model) {
    model.addAttribute("instance", oneControlInstance.isPrimaryEnabled() ? "Primary": "Secondary");
    model.addAttribute("other", oneControlInstance.isPrimaryEnabled() ? "Secondary" : "Primary");
    model.addAttribute("configuration", oneControlInstance.getCurrentConfiguration());

    return "appmanager/onecontrol/configuration";
  }

  @RequestMapping(value = "/configuration/switch", method = RequestMethod.POST)
  public String switchConfiguration(Model model) {

    oneControlService.switchOneControlInstance();

    return "appmanager/onecontrol/configuration";
  }
}
