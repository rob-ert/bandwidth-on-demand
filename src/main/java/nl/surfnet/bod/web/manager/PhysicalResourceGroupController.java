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
package nl.surfnet.bod.web.manager;

import javax.annotation.Resource;
import javax.validation.Valid;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.web.base.MessageManager;
import nl.surfnet.bod.web.security.Security;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller("managerPhysicalResourceGroupController")
@RequestMapping("/manager/physicalresourcegroups")
public class PhysicalResourceGroupController {

  @Resource
  private PhysicalResourceGroupService physicalResourceGroupService;

  @Resource
  private MessageManager messageManager;

  @RequestMapping(value = "/edit", params = "id", method = RequestMethod.GET)
  public String updateForm(@RequestParam("id") final Long id, final Model model) {
    PhysicalResourceGroup group = physicalResourceGroupService.find(id);

    if (group == null || !Security.managerMayEdit(group)) {
      return "redirect:physicalresourcegroups";
    }

    model.addAttribute("updateEmailCommand", new UpdateEmailCommand(group));
    model.addAttribute("physicalResourceGroup", group);

    return "manager/physicalresourcegroups/update";
  }

  @RequestMapping(method = RequestMethod.PUT)
  public String update(@Valid final UpdateEmailCommand command, final BindingResult result, final Model model,
      final RedirectAttributes redirectAttributes) {

    PhysicalResourceGroup group = physicalResourceGroupService.find(command.getId());
    if (group == null || !Security.managerMayEdit(group)) {
      return "redirect:manager/index";
    }

    if (result.hasErrors()) {
      model.addAttribute("physicalResourceGroup", group);

      return "manager/physicalresourcegroups/update";
    }

    if (emailChanged(group, command)) {
      group.setManagerEmail(command.getManagerEmail());
      physicalResourceGroupService.sendActivationRequest(group);

      messageManager.addInfoFlashMessage(redirectAttributes, "info_activation_request_resend", group.getManagerEmail());
    }

    redirectAttributes.addFlashAttribute("prg", group);

    return "redirect:/manager";
  }

  private boolean emailChanged(PhysicalResourceGroup group, UpdateEmailCommand command) {
    return group.getManagerEmail() == null || !group.getManagerEmail().equals(command.getManagerEmail());
  }

  public static final class UpdateEmailCommand {
    private Long id;
    private Integer version;
    @Email
    @NotEmpty
    private String managerEmail;

    public UpdateEmailCommand() {
    }

    public UpdateEmailCommand(PhysicalResourceGroup group) {
      this.id = group.getId();
      this.version = group.getVersion();
      this.managerEmail = group.getManagerEmail();
    }

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public Integer getVersion() {
      return version;
    }

    public void setVersion(Integer version) {
      this.version = version;
    }

    public String getManagerEmail() {
      return managerEmail;
    }

    public void setManagerEmail(String email) {
      this.managerEmail = email;
    }
  }
}
