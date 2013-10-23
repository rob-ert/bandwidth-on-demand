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
package nl.surfnet.bod.web.user;

import static nl.surfnet.bod.util.Orderings.prgNameOrdering;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.util.Functions;
import nl.surfnet.bod.web.base.MessageManager;
import nl.surfnet.bod.web.base.MessageRetriever;
import nl.surfnet.bod.web.base.MessageView;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.UserGroupView;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;
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

  @Resource private PhysicalResourceGroupService physicalResourceGroupService;
  @Resource private VirtualResourceGroupService virtualResourceGroupService;
  @Resource private VirtualPortService virtualPortService;
  @Resource private MessageManager messageManager;
  @Resource private MessageRetriever messageRetriever;

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
      return missingEmailAddress(model);
    }

    // Find related virtual resource groups
    Collection<VirtualResourceGroup> vrgs = virtualResourceGroupService.findAllForUser(user);
    final Collection<String> existingIds = Lists.newArrayList(Collections2.transform(vrgs,
        new Function<VirtualResourceGroup, String>() {
          @Override
          public String apply(VirtualResourceGroup group) {
            return group.getAdminGroup();
          }
        }));

    // Transform to view
    Collection<UserGroupView> existingTeams = ImmutableList.copyOf(Collections2.transform(vrgs,
        Functions.FROM_VRG_TO_USER_GROUP_VIEW));

    // Filter new teams
    ImmutableList<UserGroupView> newTeams = FluentIterable.from(user.getUserGroups()).filter(
        new Predicate<UserGroup>() {
          @Override
          public boolean apply(UserGroup group) {
            return !existingIds.contains(group.getId());
          }
        }).transform(Functions.FROM_USER_GROUP_TO_USER_GROUP_VIEW).toList();

    // Put result sorted on model
    model.addAttribute("userGroupViews", Ordering.natural().sortedCopy(Iterables.concat(existingTeams, newTeams)));

    return "virtualports/selectTeam";
  }

  private String missingEmailAddress(Model model) {
    MessageView message = MessageView.createErrorMessage(messageRetriever, "error_label_no_email",
        "error_content_no_email");

    model.addAttribute(MessageView.MODEL_KEY, message);

    return MessageView.PAGE_URL;
  }

  @RequestMapping(method = RequestMethod.GET, params = { "teamLabel", "teamUrn" })
  public String selectInstitute(@RequestParam String teamLabel, @RequestParam String teamUrn, Model model) {
    RichUserDetails user = Security.getUserDetails();

    if (!user.getEmail().isPresent()) {
      return missingEmailAddress(model);
    }

    Collection<PhysicalResourceGroup> groups = physicalResourceGroupService.findAllWithPorts();

    model.addAttribute("physicalResourceGroups", prgNameOrdering().sortedCopy(groups));
    model.addAttribute("teamLabel", teamLabel);
    model.addAttribute("teamUrn", teamUrn);

    return "virtualports/selectInstitute";
  }

  @RequestMapping(value = "/delete", method = RequestMethod.GET)
  public String deleteRequestForm(@RequestParam Long id, Model model) {
    VirtualPort virtualPort = virtualPortService.find(id);

    if (virtualPort == null || Security.userMayNotEdit(virtualPort)) {
      return "redirect:/virtualports";
    }

    model.addAttribute("user", Security.getUserDetails());
    model.addAttribute("deleteRequestCommand", new DeleteRequestCommand(virtualPort));

    return "virtualports/deleterequestform";
  }

  @RequestMapping(value = "/delete", method = RequestMethod.POST)
  public String deleteRequest(@Valid DeleteRequestCommand requestCommand, BindingResult result, Model model,
      RedirectAttributes redirectAttributes) {

    if (result.hasErrors()) {
      model.addAttribute("user", Security.getUserDetails());
      model.addAttribute("deleteRequestCommand", requestCommand);

      return "virtualports/deleterequestform";
    }

    if (Security.userMayNotEdit(requestCommand.getVirtualPort())) {
      return "redirect:/virtualports";
    }

    virtualPortService.requestDeleteVirtualPort(Security.getUserDetails(), requestCommand.getMessage(), requestCommand.getVirtualPort());

    messageManager.addInfoFlashMessage(redirectAttributes,
      "info_virtualport_delete_request_send",
      requestCommand.getVirtualPort().getUserLabel(),
      requestCommand.getVirtualPort().getVirtualResourceGroup().getName());

    return "redirect:/user";
  }

  @RequestMapping(method = RequestMethod.GET, params = { "id", "teamUrn" })
  public String requestForm(@RequestParam Long id, @RequestParam String teamUrn, Model model,
      RedirectAttributes redirectAttributes) {

    PhysicalResourceGroup group = physicalResourceGroupService.find(id);
    if (group == null || !group.isActive()) {
      messageManager.addInfoFlashMessage(redirectAttributes, "info_virtualport_request_invalid_group");

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
    } else {
      UserGroup userGroup = Security.getUserGroup(teamUrn);

      if (userGroup == null) {
        return Collections.emptyList();
      }

      VirtualResourceGroup group = new VirtualResourceGroup();
      group.setAdminGroup(userGroup.getId());
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

    VirtualResourceGroup vrg = virtualResourceGroupService.findByAdminGroup(userGroup.getId());
    boolean shouldClearSecurityContext = false;
    if (vrg == null) {
      vrg = new VirtualResourceGroup();
      vrg.setName(userGroup.getName());
      vrg.setAdminGroup(userGroup.getId());
      vrg.setDescription(userGroup.getDescription());
      virtualResourceGroupService.save(vrg);
      shouldClearSecurityContext = true;
    }

    virtualPortService.requestNewVirtualPort(Security.getUserDetails(), vrg, prg, requestCommand.getUserLabel(),
        requestCommand.getBandwidth(), requestCommand.getMessage());

    messageManager.addInfoFlashMessage(redirectAttributes, "info_virtualport_request_send", prg.getInstitute()
        .getName());

    // in case a new vrg was created and the user has no user role, clear the
    // security context
    // prevent switching to a different role when it is not needed
    if (shouldClearSecurityContext && !Security.hasUserRole()) {
      SecurityContextHolder.clearContext();
    }

    return "redirect:/user";
  }

  public static class DeleteRequestCommand {
    @Length(min = 1, max = 255, message = "Must be between 1 and 255 characters long")
    private String message;

    @NotNull
    private VirtualPort virtualPort;

    public DeleteRequestCommand() {
    }

    public DeleteRequestCommand(VirtualPort virtualPort) {
      this.virtualPort = virtualPort;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }

    public VirtualPort getVirtualPort() {
      return virtualPort;
    }

    public void setVirtualPort(VirtualPort virtualPort) {
      this.virtualPort = virtualPort;
    }
  }

  public static class RequestCommand {
    @Length(min = 1, max = 255, message = "Must be between 1 and 255 characters long")
    private String userLabel;

    @Length(min = 1, max = 255, message = "Must be between 1 and 255 characters long")
    private String message;

    @NotNull
    private Long physicalResourceGroupId;

    @NotEmpty
    private String userGroupId;

    @Range(min = 1, max = 1000000, message = "Must be between 1 and 1000000 mb/s")
    private Long bandwidth;

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

    public Long getBandwidth() {
      return bandwidth;
    }

    public void setBandwidth(Long bandwidth) {
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
