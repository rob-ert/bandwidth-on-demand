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

import static nl.surfnet.bod.util.Orderings.prgNameOrdering;

import java.util.Collection;
import java.util.Collections;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.web.security.Security;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

@Controller
@RequestMapping("/request")
public class VirtualPortRequestController {

  @Autowired
  private PhysicalResourceGroupService physicalResourceGroupService;
  @Autowired
  private VirtualResourceGroupService virtualResourceGroupService;
  @Autowired
  private VirtualPortService virtualPortService;
  @Autowired
  private MessageSource messageSource;

  @RequestMapping(method = RequestMethod.GET, params = "team")
  public String selectInstitute(@RequestParam(value = "team") String teamUrn, Model model) {
    Collection<PhysicalResourceGroup> groups = physicalResourceGroupService.findAllWithPorts();

    model.addAttribute("physicalResourceGroups", prgNameOrdering().sortedCopy(groups));
    model.addAttribute("teamUrn", teamUrn);

    return "virtualports/selectInstitute";
  }

  @RequestMapping(method = RequestMethod.GET, params = { "id", "team" })
  public String requestForm(@RequestParam Long id, @RequestParam("team") String teamUrn, Model model,
      RedirectAttributes redirectAttributes) {

    PhysicalResourceGroup group = physicalResourceGroupService.find(id);
    if (group == null || !group.isActive()) {
      WebUtils.addInfoMessage(redirectAttributes, messageSource, "info_virtualport_request_invalid_group");

      return "redirect:/virtualports/request";
    }

    Collection<VirtualResourceGroup> vGroups = getVirtualResourceGroups(teamUrn);
    if (vGroups.isEmpty()) {
      return "redirect:/";
    }

    model.addAttribute("requestCommand", new RequestCommand(group));
    model.addAttribute("physicalResourceGroup", group);
    model.addAttribute("user", Security.getUserDetails());
    model.addAttribute("virtualResourceGroups", vGroups);

    return "virtualports/requestform";
  }

  public Collection<VirtualResourceGroup> getVirtualResourceGroups(final String teamUrn) {
    if (Strings.emptyToNull(teamUrn) == null) {
      return virtualResourceGroupService.findAllForUser(Security.getUserDetails());
    }
    else {
      UserGroup userGroup = Security.getUserGroup(teamUrn);

      if (userGroup == null) {
        return Collections.emptyList();
      }

      VirtualResourceGroup group = new VirtualResourceGroup();
      group.setSurfconextGroupId(userGroup.getId());
      group.setName(StringUtils.capitalize(userGroup.getName()));

      return ImmutableList.of(group);
    }
  }

  @RequestMapping(method = RequestMethod.POST)
  public String request(@Valid RequestCommand requestCommand, BindingResult result, Model model,
      RedirectAttributes redirectAttributes) {

    PhysicalResourceGroup prg = physicalResourceGroupService.find(requestCommand.getPhysicalResourceGroupId());
    UserGroup userGroup = Security.getUserGroup(requestCommand.getUserGroupId());

    if (prg == null || !prg.isActive() || userGroup == null) {
      return "redirect:/virtualports/request";
    }

    if (result.hasErrors()) {
      model.addAttribute("user", Security.getUserDetails());
      model.addAttribute("physicalResourceGroup", prg);
      model.addAttribute("virtualResourceGroups", getVirtualResourceGroups(requestCommand.getUserGroupId()));

      return "virtualports/requestform";
    }

    VirtualResourceGroup vrg = virtualResourceGroupService.findBySurfconextGroupId(userGroup.getId());
    if (vrg == null) {
      vrg = new VirtualResourceGroup();
      vrg.setName(userGroup.getName());
      vrg.setSurfconextGroupId(userGroup.getId());
      vrg.setDescription(userGroup.getDescription());
      virtualResourceGroupService.save(vrg);
    }

    virtualPortService.requestNewVirtualPort(Security.getUserDetails(), vrg, prg, requestCommand.getBandwidth(),
        requestCommand.getMessage());

    WebUtils.addInfoMessage(redirectAttributes, messageSource, "info_virtualport_request_send", prg.getInstitute()
        .getName());

    // in case a new vrg was created make sure the user sees it, but prevent unnecessary switching of roles
    if (Security.getUserDetails().getSelectedRole() == null) {
      SecurityContextHolder.clearContext();
    }

    return "redirect:/";
  }

  public static class RequestCommand {
    @NotEmpty
    private String message;
    @NotNull
    private Long physicalResourceGroupId;
    @NotEmpty
    private String userGroupId;
    @NotNull
    private Integer bandwidth;

    public RequestCommand() {
    }

    public RequestCommand(PhysicalResourceGroup group) {
      physicalResourceGroupId = group.getId();
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }

    public Long getPhysicalResourceGroupId() {
      return physicalResourceGroupId;
    }

    public void setPhysicalResourceGroupId(Long groupId) {
      this.physicalResourceGroupId = groupId;
    }

    public String getUserGroupId() {
      return userGroupId;
    }

    public void setUserGroupId(String userGroupId) {
      this.userGroupId = userGroupId;
    }

    public Integer getBandwidth() {
      return bandwidth;
    }

    public void setBandwidth(Integer bandwidth) {
      this.bandwidth = bandwidth;
    }
  }

}
