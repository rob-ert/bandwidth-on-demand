/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
package nl.surfnet.bod.web;

import static nl.surfnet.bod.web.WebUtils.*;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.web.security.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/virtualports")
public class VirtualPortController {

  @Autowired
  private VirtualPortService virtualPortService;

  @RequestMapping(method = RequestMethod.GET)
  public String list(@RequestParam(value = PAGE_KEY, required = false) final Integer page, final Model uiModel) {

    uiModel.addAttribute("virtualPorts",
        virtualPortService.findEntriesForUser(Security.getUserDetails(), calculateFirstPage(page), MAX_ITEMS_PER_PAGE));

    uiModel.addAttribute(MAX_PAGES_KEY, calculateMaxPages(virtualPortService.countForUser(Security.getUserDetails())));

    return "virtualports/list";
  }

  @RequestMapping(value = EDIT, params = ID_KEY, method = RequestMethod.GET)
  public String updateForm(@RequestParam(ID_KEY) final Long id, final Model uiModel) {
    VirtualPort virtualPort = virtualPortService.find(id);

    if (virtualPort == null || Security.userMayNotEdit(virtualPort)) {
      return "redirect:";
    }

    uiModel.addAttribute("virtualPort", virtualPort);
    uiModel.addAttribute("updateUserLabelCommand", new UpdateUserLabelCommand(virtualPort));

    return "virtualports/update";
  }

  @RequestMapping(method = RequestMethod.PUT)
  public String update(final UpdateUserLabelCommand command, final BindingResult bindingResult, final Model uiModel) {
    VirtualPort virtualPort = virtualPortService.find(command.getId());

    if (virtualPort == null || Security.userMayNotEdit(virtualPort)) {
      return "redirect:/virtualports";
    }

    validateUpdateUserLabelCommand(command, bindingResult);

    if (bindingResult.hasErrors()) {
      uiModel.addAttribute("updateUserLabelCommand", command);
      uiModel.addAttribute("virtualPort", virtualPort);

      return "virtualports/update";
    }

    uiModel.asMap().clear();

    virtualPort.setUserLabel(command.getUserLabel());
    virtualPortService.update(virtualPort);

    return "redirect:/virtualports";
  }

  private void validateUpdateUserLabelCommand(UpdateUserLabelCommand command, Errors errors) {
    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userLabel", "validation.not.empty");

    VirtualPort existingVirtualPort = virtualPortService.findByUserLabel(command.getUserLabel());

    if (existingVirtualPort != null && existingVirtualPort.getUserLabel().equalsIgnoreCase(command.getUserLabel())
        && existingVirtualPort.getId() != command.getId()) {
      errors.rejectValue("userLabel", "validation.not.unique");
    }
  }

  public static class UpdateUserLabelCommand {
    private String userLabel;
    private Long id;
    private Integer version;

    public UpdateUserLabelCommand() {
    }

    public UpdateUserLabelCommand(VirtualPort port) {
      this.userLabel = port.getUserLabel();
      this.id = port.getId();
      this.version = port.getVersion();
    }

    public String getUserLabel() {
      return userLabel;
    }

    public void setUserLabel(String userLabel) {
      this.userLabel = userLabel;
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
  }

}
