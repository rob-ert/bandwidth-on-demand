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

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import nl.surfnet.bod.domain.*;
import nl.surfnet.bod.domain.validator.VirtualPortValidator;
import nl.surfnet.bod.service.AbstractFullTextSearchService;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.base.AbstractSearchableSortableListController;
import nl.surfnet.bod.web.base.MessageView;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.VirtualPortView;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Controller("managerVirtualPortController")
@RequestMapping(VirtualPortController.PAGE_URL)
public class VirtualPortController extends AbstractSearchableSortableListController<VirtualPortView, VirtualPort> {

  public static final String MODEL_KEY = "virtualPort";
  public static final String PAGE_URL = "/manager/virtualports";

  @Resource
  private VirtualPortService virtualPortService;

  @Resource
  private VirtualPortValidator virtualPortValidator;

  @Resource
  private MessageSource messageSource;

  @Resource
  private ReservationService reservationService;

  @RequestMapping(method = RequestMethod.POST)
  public String create(@Valid VirtualPortCreateCommand createCommand, BindingResult result, Model model,
      RedirectAttributes redirectAttributes) {

    if (createCommand.getAcceptOrDecline().equals("decline")) {
      if (result.hasFieldErrors("declineMessage")) {
        return addCreateFormToModel(createCommand, model);
      }

      virtualPortService.requestLinkDeclined(createCommand.getVirtualPortRequestLink(),
          createCommand.getDeclineMessage());

      return "redirect:" + PAGE_URL;
    }

    VirtualPort port = createCommand.getPort();
    virtualPortValidator.validate(port, result);
    if (result.hasErrors() && !declineMessageIsOnlyError(result)) {
      return addCreateFormToModel(createCommand, model);
    }

    model.asMap().clear();

    virtualPortService.save(port);
    virtualPortService.requestLinkApproved(createCommand.getVirtualPortRequestLink(), port);

    WebUtils.addInfoFlashMessage(redirectAttributes, messageSource, "info_virtualport_created", port.getManagerLabel());

    return "redirect:" + PAGE_URL;
  }

  private boolean declineMessageIsOnlyError(BindingResult result) {
    return result.getErrorCount() == 1 && result.hasFieldErrors("declineMessage");
  }

  private String addCreateFormToModel(VirtualPortCreateCommand command, Model model) {
    model.addAttribute("virtualPortCreateCommand", command);
    model.addAttribute("physicalPorts", command.getPhysicalResourceGroup() == null ? Collections.emptyList() : command
        .getPhysicalResourceGroup().getPhysicalPorts());
    model.addAttribute("virtualResourceGroups", ImmutableList.of(command.getVirtualResourceGroup()));
    model.addAttribute("physicalResourceGroups", ImmutableList.of(command.getPhysicalResourceGroup()));

    return PAGE_URL + CREATE;
  }

  @RequestMapping(value = "/create/{uuid}", method = RequestMethod.GET)
  public String createForm(@PathVariable("uuid") String link, Model model, RedirectAttributes redirectAttributes) {
    VirtualPortRequestLink requestLink = virtualPortService.findRequest(link);

    if (requestLink == null) {
      WebUtils.addInfoFlashMessage(redirectAttributes, messageSource, "info_virtualportrequestlink_notvalid");
      return "redirect:/";
    }

    if (!Security.isManagerMemberOf(requestLink.getPhysicalResourceGroup())) {
      WebUtils.addInfoFlashMessage(redirectAttributes, messageSource, "info_virtualportrequestlink_notmanager");
      return "redirect:/";
    }

    Security.switchToManager(requestLink.getPhysicalResourceGroup());

    if (!requestLink.isPending()) {
      MessageView message = MessageView.createInfoMessage(messageSource,
          "info_virtualportrequest_already_processed_title", "info_virtualportrequest_already_processed_message",
          requestLink.getVirtualResourceGroup().getName());

      model.addAttribute(MessageView.MODEL_KEY, message);

      return MessageView.PAGE_URL;
    }

    VirtualPortCreateCommand command = new VirtualPortCreateCommand(requestLink);
    addCreateFormToModel(command, model);

    return PAGE_URL + CREATE;
  }

  @RequestMapping(method = RequestMethod.PUT)
  public String update(@Valid VirtualPortUpdateCommand command, BindingResult result, Model model,
      RedirectAttributes redirectAttributes) {

    VirtualPort port = virtualPortService.find(command.getId());

    if (port == null || Security.managerMayNotEdit(port)) {
      return "redirect:" + PAGE_URL;
    }

    port.setManagerLabel(command.getManagerLabel());
    port.setVlanId(command.getVlanId());
    port.setMaxBandwidth(command.getMaxBandwidth());
    port.setPhysicalPort(command.getPhysicalPort());
    port.setVirtualResourceGroup(command.getVirtualResourceGroup());

    virtualPortValidator.validate(port, result);

    if (result.hasErrors()) {
      model.addAttribute("virtualPortUpdateCommand", command);
      model.addAttribute("virtualResourceGroups", ImmutableList.of(port.getVirtualResourceGroup()));
      model.addAttribute("physicalResourceGroups", ImmutableList.of(port.getPhysicalResourceGroup()));
      model.addAttribute("physicalPorts", port.getPhysicalResourceGroup().getPhysicalPorts());
      return PAGE_URL + UPDATE;
    }

    model.asMap().clear();
    WebUtils.addInfoFlashMessage(redirectAttributes, messageSource, "info_virtualport_updated", port.getManagerLabel());

    virtualPortService.update(port);

    return "redirect:" + PAGE_URL;
  }

  @RequestMapping(value = EDIT, params = ID_KEY, method = RequestMethod.GET)
  public String updateForm(@RequestParam(ID_KEY) final Long id, final Model model) {
    VirtualPort virtualPort = virtualPortService.find(id);

    if (virtualPort == null || Security.managerMayNotEdit(virtualPort)) {
      return "redirect:" + PAGE_URL;
    }

    model.addAttribute("virtualPortUpdateCommand", new VirtualPortUpdateCommand(virtualPort));
    model.addAttribute("physicalPorts", virtualPort.getPhysicalResourceGroup().getPhysicalPorts());
    model.addAttribute("virtualResourceGroups", ImmutableList.of(virtualPort.getVirtualResourceGroup()));
    model.addAttribute("physicalResourceGroups", ImmutableList.of(virtualPort.getPhysicalResourceGroup()));

    return PAGE_URL + UPDATE;
  }

  @RequestMapping(value = DELETE, params = ID_KEY, method = RequestMethod.DELETE)
  public String delete(@RequestParam(ID_KEY) Long id, @RequestParam(value = PAGE_KEY, required = false) Integer page,
      RedirectAttributes redirectAttributes) {

    VirtualPort virtualPort = virtualPortService.find(id);

    if (virtualPort == null || Security.managerMayNotEdit(virtualPort)) {
      return "redirect:" + PAGE_URL;
    }

    virtualPortService.delete(virtualPort, Security.getUserDetails());

    WebUtils.addInfoFlashMessage(redirectAttributes, messageSource, "info_virtualport_deleted",
        virtualPort.getManagerLabel());

    return "redirect:" + PAGE_URL;
  }

  @Override
  protected String listUrl() {
    return PAGE_URL + LIST;
  }

  @Override
  protected List<VirtualPortView> list(int firstPage, int maxItems, Sort sort, Model model) {
    final List<VirtualPort> entriesForManager = virtualPortService.findEntriesForManager(Security.getSelectedRole(),
        firstPage, maxItems, sort);

    return transformToView(entriesForManager, Security.getUserDetails());
  }

  @Override
  protected long count(Model model) {
    return virtualPortService.countForManager(Security.getSelectedRole());
  }

  @Override
  protected String getDefaultSortProperty() {
    return "managerLabel";
  }

  @Override
  protected List<String> translateSortProperty(String sortProperty) {
    if (sortProperty.equals("physicalResourceGroup")) {
      return ImmutableList.of("physicalPort.physicalResourceGroup");
    }

    return super.translateSortProperty(sortProperty);
  }

  public static class VirtualPortCommand {
    @NotEmpty
    private String managerLabel;
    @NotNull
    @Min(value = 1)
    private Integer maxBandwidth;
    @Range(min = 1, max = 4095)
    private Integer vlanId;
    @NotNull
    private PhysicalPort physicalPort;
    @NotNull
    private VirtualResourceGroup virtualResourceGroup;
    @NotNull
    private PhysicalResourceGroup physicalResourceGroup;

    public VirtualPortCommand() {
    }

    public VirtualPortCommand(String managerLabel, Integer maxBandwidth, Integer vlanId,
        PhysicalResourceGroup physicalResourceGroup, PhysicalPort physicalPort,
        VirtualResourceGroup virtualResourceGroup) {
      this.managerLabel = managerLabel;
      this.maxBandwidth = maxBandwidth;
      this.vlanId = vlanId;
      this.physicalPort = physicalPort;
      this.physicalResourceGroup = physicalResourceGroup;
      this.virtualResourceGroup = virtualResourceGroup;
    }

    public String getManagerLabel() {
      return managerLabel;
    }

    public void setManagerLabel(String managerLabel) {
      this.managerLabel = managerLabel;
    }

    public Integer getMaxBandwidth() {
      return maxBandwidth;
    }

    public void setMaxBandwidth(Integer maxBandwidth) {
      this.maxBandwidth = maxBandwidth;
    }

    public Integer getVlanId() {
      return vlanId;
    }

    public void setVlanId(Integer vlanId) {
      this.vlanId = vlanId;
    }

    public PhysicalPort getPhysicalPort() {
      return physicalPort;
    }

    public void setPhysicalPort(PhysicalPort physicalPort) {
      this.physicalPort = physicalPort;
    }

    public VirtualResourceGroup getVirtualResourceGroup() {
      return virtualResourceGroup;
    }

    public void setVirtualResourceGroup(VirtualResourceGroup virtualResourceGroup) {
      this.virtualResourceGroup = virtualResourceGroup;
    }

    public PhysicalResourceGroup getPhysicalResourceGroup() {
      return physicalResourceGroup;
    }

    public void setPhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup) {
      this.physicalResourceGroup = physicalResourceGroup;
    }
  }

  public static final class VirtualPortCreateCommand extends VirtualPortCommand {
    @NotNull
    private VirtualPortRequestLink virtualPortRequestLink;
    @NotEmpty
    private String declineMessage;
    @NotEmpty
    private String acceptOrDecline = "accept";

    private String userLabel;

    public VirtualPortCreateCommand() {
    }

    public VirtualPortCreateCommand(VirtualPortRequestLink link) {
      super("", link.getMinBandwidth(), null, link.getPhysicalResourceGroup(), Iterables.get(link
          .getPhysicalResourceGroup().getPhysicalPorts(), 0), link.getVirtualResourceGroup());

      this.userLabel = link.getUserLabel();
      this.virtualPortRequestLink = link;
    }

    public VirtualPort getPort() {
      VirtualPort port = new VirtualPort();
      port.setManagerLabel(getManagerLabel());
      port.setUserLabel(getUserLabel());
      port.setMaxBandwidth(getMaxBandwidth());
      port.setVlanId(getVlanId());
      port.setPhysicalPort(getPhysicalPort());
      port.setVirtualResourceGroup(getVirtualResourceGroup());

      return port;
    }

    public VirtualPortRequestLink getVirtualPortRequestLink() {
      return virtualPortRequestLink;
    }

    public void setVirtualPortRequestLink(VirtualPortRequestLink virtualPortRequestLink) {
      this.virtualPortRequestLink = virtualPortRequestLink;
    }

    public String getDeclineMessage() {
      return declineMessage;
    }

    public void setDeclineMessage(String declineExplanation) {
      this.declineMessage = declineExplanation;
    }

    public String getAcceptOrDecline() {
      return acceptOrDecline;
    }

    public void setAcceptOrDecline(String acceptOrDecline) {
      this.acceptOrDecline = acceptOrDecline;
    }

    public String getUserLabel() {
      return userLabel;
    }

    public void setUserLabel(String userLabel) {
      this.userLabel = userLabel;
    }
  }

  public static final class VirtualPortUpdateCommand extends VirtualPortCommand {
    private Long id;
    private Integer version;

    public VirtualPortUpdateCommand() {
    }

    public VirtualPortUpdateCommand(VirtualPort port) {
      super(port.getManagerLabel(), port.getMaxBandwidth(), port.getVlanId(), port.getPhysicalResourceGroup(), port
          .getPhysicalPort(), port.getVirtualResourceGroup());
      this.id = port.getId();
      this.version = port.getVersion();
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

  @Override
  protected AbstractFullTextSearchService<VirtualPort> getFullTextSearchableService() {
    return virtualPortService;
  }

  @Override
  protected List<Long> getIdsOfAllAllowedEntries(Model model) {
    final VirtualPortView filter = WebUtils.getAttributeFromModel(FILTER_SELECT, model);
    return virtualPortService.findIdsForUserUsingFilter(Security.getUserDetails(), filter);
  }

  @Override
  protected List<VirtualPortView> transformToView(List<VirtualPort> entities, RichUserDetails user) {
    return Lists.transform(entities, new Function<VirtualPort, VirtualPortView>() {
      @Override
      public VirtualPortView apply(VirtualPort port) {
        final long counter = reservationService.findAllActiveByVirtualPort(port).size();
        return new VirtualPortView(port, Optional.<Long> of(counter));
      }
    });
  }
}
