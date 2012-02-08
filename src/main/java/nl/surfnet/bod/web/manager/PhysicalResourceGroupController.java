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
package nl.surfnet.bod.web.manager;

import static nl.surfnet.bod.web.WebUtils.*;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller("managerPhysicalResourceGroupController")
@RequestMapping("/manager/physicalresourcegroups")
public class PhysicalResourceGroupController {

  @Autowired
  private PhysicalResourceGroupService physicalResourceGroupService;

  @RequestMapping(method = RequestMethod.GET)
  public String list(@RequestParam(value = PAGE_KEY, required = false) final Integer page, final Model uiModel) {

    RichUserDetails user = Security.getUserDetails();

    uiModel.addAttribute("physicalResourceGroups", physicalResourceGroupService.findEntriesForManager(
        user, calculateFirstPage(page), MAX_ITEMS_PER_PAGE));

    uiModel.addAttribute(MAX_PAGES_KEY, calculateMaxPages(physicalResourceGroupService.countForManager(user)));

    return "manager/physicalresourcegroups/list";
  }

  @RequestMapping(value = "/edit", params = "id", method = RequestMethod.GET)
  public String updateForm(@RequestParam("id") final Long id, final Model uiModel) {
    PhysicalResourceGroup group = physicalResourceGroupService.find(id);

    if (!Security.managerMayEdit(group)) {
      return "manager/physicalresourcegroups";
    }

    uiModel.addAttribute("updateEmailCommand", new UpdateEmailCommand(group));
    uiModel.addAttribute("physicalResourceGroup", group);

    return "manager/physicalresourcegroups/update";
  }

  @RequestMapping(method = RequestMethod.PUT)
  public String update(final UpdateEmailCommand command, final BindingResult bindingResult, final Model uiModel) {
    PhysicalResourceGroup group = physicalResourceGroupService.find(command.getId());

    if (Security.managerMayEdit(group)) {
      // FIXME should save... and validate email..
      // physicalResourceGroupService.changeEmail(...);
    }

    uiModel.asMap().clear();

    return "redirect:physicalresourcegroups";
  }

  public static final class UpdateEmailCommand {
    private Long id;
    private Integer version;
    private String managerEmail;

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
