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
package nl.surfnet.bod.web.user;

import static nl.surfnet.bod.util.Orderings.prgNameOrdering;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.util.Functions;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.base.MessageView;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.UserGroupView;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;
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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.*;

@Controller
@RequestMapping("/request")
public class VirtualPortRequestController {

  @Resource
  private PhysicalResourceGroupService physicalResourceGroupService;
  @Resource
  private VirtualResourceGroupService virtualResourceGroupService;
  @Resource
  private VirtualPortService virtualPortService;
  @Resource
  private MessageSource messageSource;

  /**
   * In case no team is selected yet, present the team selection.
   *
   * @param model
   * @return SelectTeam view
   */
  @RequestMapping(method = RequestMethod.GET)
  public String selectTeam(Model model) {
    RichUserDetails user = Security.getUserDetails();

    if (!user.getEmail().isPresent()) {
      MessageView message = MessageView.createErrorMessage(
          messageSource,
          "error_label_no_email",
          "error_content_no_email");

      model.addAttribute(MessageView.MODEL_KEY, message);

      return MessageView.PAGE_URL;
    }

    // Find related virtual resource groups
    Collection<VirtualResourceGroup> vrgs = virtualResourceGroupService.findAllForUser(user);
    final Collection<String> existingIds = Lists.newArrayList(Collections2.transform(vrgs,
        new Function<VirtualResourceGroup, String>() {
          @Override
          public String apply(VirtualResourceGroup group) {
            return group.getSurfconextGroupId();
          }
        }));

    // Transform to view
    Collection<UserGroupView> existingTeams = ImmutableList.copyOf(Collections2.transform(vrgs,
        Functions.FROM_VRG_TO_USER_GROUP_VIEW));

    // Filter new teams
    ImmutableList<UserGroupView> newTeams = FluentIterable.from(user.getUserGroups())
        .filter(new Predicate<UserGroup>() {
          @Override
          public boolean apply(UserGroup group) {
            return !existingIds.contains(group.getId());
          }
        }).transform(Functions.FROM_USER_GROUP_TO_USER_GROUP_VIEW).toImmutableList();

    // Put result sorted on model
    model.addAttribute("userGroupViews", Ordering.natural().sortedCopy(Iterables.concat(existingTeams, newTeams)));

    return "virtualports/selectTeam";
  }

  @RequestMapping(method = RequestMethod.GET, params = { "teamLabel", "teamUrn" })
  public String selectInstitute(@RequestParam String teamLabel, @RequestParam String teamUrn, Model model) {
    Collection<PhysicalResourceGroup> groups = physicalResourceGroupService.findAllWithPorts();

    model.addAttribute("physicalResourceGroups", prgNameOrdering().sortedCopy(groups));
    model.addAttribute("teamLabel", teamLabel);
    model.addAttribute("teamUrn", teamUrn);

    return "virtualports/selectInstitute";
  }

  @RequestMapping(method = RequestMethod.GET, params = { "id", "teamUrn" })
  public String requestForm(@RequestParam Long id, @RequestParam String teamUrn, Model model,
      RedirectAttributes redirectAttributes) {

    PhysicalResourceGroup group = physicalResourceGroupService.find(id);
    if (group == null || !group.isActive()) {
      WebUtils.addInfoFlashMessage(redirectAttributes, messageSource, "info_virtualport_request_invalid_group");

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
    boolean shouldClearSecurityContext = false;
    if (vrg == null) {
      vrg = new VirtualResourceGroup();
      vrg.setName(userGroup.getName());
      vrg.setSurfconextGroupId(userGroup.getId());
      vrg.setDescription(userGroup.getDescription());
      virtualResourceGroupService.save(vrg);
      shouldClearSecurityContext = true;
    }

    virtualPortService.requestNewVirtualPort(Security.getUserDetails(), vrg, prg, requestCommand.getUserLabel(),
        requestCommand.getBandwidth(), requestCommand.getMessage());

    WebUtils.addInfoFlashMessage(redirectAttributes, messageSource, "info_virtualport_request_send", prg.getInstitute()
        .getName());

    // in case a new vrg was created and the user has no user role, clear the
    // security context
    // prevent switching to a different role when it is not needed
    if (shouldClearSecurityContext && !Security.hasUserRole()) {
      SecurityContextHolder.clearContext();
    }

    return "redirect:/user";
  }

  public static class RequestCommand {

    @Length(min = 0, max = 255, message = "Must be between 1 and 255 characters long")
    private String userLabel;

    @Length(min = 1, max = 255, message = "Must be between 1 and 255 characters long")
    private String message;

    @NotNull
    private Long physicalResourceGroupId;

    @NotEmpty
    private String userGroupId;

    @Range(min = 1, max = 1000000, message = "Must be between 1 and 1000000 mb/s")
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

    public String getUserLabel() {
      return userLabel;
    }

    public void setUserLabel(String portName) {
      this.userLabel = portName;
    }
  }

}
