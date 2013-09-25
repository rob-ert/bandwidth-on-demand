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
package nl.surfnet.bod.web.manager;

import static nl.surfnet.bod.web.WebUtils.CREATE;
import static nl.surfnet.bod.web.WebUtils.DELETE;
import static nl.surfnet.bod.web.WebUtils.EDIT;
import static nl.surfnet.bod.web.WebUtils.FILTER_SELECT;
import static nl.surfnet.bod.web.WebUtils.ID_KEY;
import static nl.surfnet.bod.web.WebUtils.LIST;
import static nl.surfnet.bod.web.WebUtils.PAGE_KEY;
import static nl.surfnet.bod.web.WebUtils.UPDATE;

import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.UniPort;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualPortRequestLink;
import nl.surfnet.bod.domain.VirtualPortRequestLink.RequestStatus;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.domain.VlanRangesValidator;
import nl.surfnet.bod.domain.validator.VirtualPortValidator;
import nl.surfnet.bod.nsi.NsiHelper;
import nl.surfnet.bod.repo.VirtualPortRequestLinkRepo;
import nl.surfnet.bod.service.AbstractFullTextSearchService;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.base.AbstractSearchableSortableListController;
import nl.surfnet.bod.web.base.MessageManager;
import nl.surfnet.bod.web.base.MessageRetriever;
import nl.surfnet.bod.web.base.MessageView;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.VirtualPortView;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller("managerVirtualPortController")
@RequestMapping(VirtualPortController.PAGE_URL)
public class VirtualPortController extends AbstractSearchableSortableListController<VirtualPortView, VirtualPort> {

  public static final String MODEL_KEY = "virtualPort";
  public static final String PAGE_URL = "/manager/virtualports";

  @Resource private VirtualPortService virtualPortService;
  @Resource private VirtualPortValidator virtualPortValidator;
  @Resource private MessageManager messageManager;
  @Resource private MessageRetriever messageRetriever;
  @Resource private ReservationService reservationService;
  @Resource private VirtualPortRequestLinkRepo virtualPortRequestLinkRepo;
  @Resource private NsiHelper nsiHelper;

  @RequestMapping(method = RequestMethod.POST)
  public String create(@Valid VirtualPortCreateCommand createCommand, BindingResult result, Model model, RedirectAttributes redirectAttributes) {

    if (createCommand.getAcceptOrDecline().equals("decline")) {
      if (result.hasFieldErrors("declineMessage")) {
        return addCreateFormToModel(createCommand, model);
      }

      virtualPortService.requestLinkDeclined(createCommand.getVirtualPortRequestLink(), createCommand
          .getDeclineMessage());

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

    messageManager.addInfoFlashMessage(redirectAttributes, "info_virtualport_created", port.getManagerLabel());

    return "redirect:" + PAGE_URL;
  }

  private boolean declineMessageIsOnlyError(BindingResult result) {
    return result.getErrorCount() == 1 && result.hasFieldErrors("declineMessage");
  }

  private String addCreateFormToModel(VirtualPortCreateCommand command, Model model) {
    model.addAttribute("virtualPortCreateCommand", command);
    model.addAttribute("physicalPorts", command.getPhysicalResourceGroup().getPhysicalPorts());
    model.addAttribute("virtualResourceGroups", ImmutableList.of(command.getVirtualResourceGroup()));
    model.addAttribute("physicalResourceGroups", ImmutableList.of(command.getPhysicalResourceGroup()));

    return PAGE_URL + CREATE;
  }

  @RequestMapping(value = "/create/{uuid}", method = RequestMethod.GET)
  public String createForm(@PathVariable("uuid") String link, Model model, RedirectAttributes redirectAttributes) {
    VirtualPortRequestLink requestLink = virtualPortService.findRequest(link);

    if (requestLink == null) {
      messageManager.addInfoFlashMessage(redirectAttributes, "info_virtualportrequestlink_notvalid");
      return "redirect:/";
    }

    if (!Security.isManagerMemberOf(requestLink.getPhysicalResourceGroup())) {
      messageManager.addInfoFlashMessage(redirectAttributes, "info_virtualportrequestlink_notmanager");
      return "redirect:/";
    }

    Security.switchToManager(requestLink.getPhysicalResourceGroup());

    if (!requestLink.isPending()) {
      MessageView message = MessageView.createInfoMessage(messageRetriever, "info_virtualportrequest_already_processed_title",
          "info_virtualportrequest_already_processed_message", requestLink.getVirtualResourceGroup().getName());

      model.addAttribute(MessageView.MODEL_KEY, message);

      return MessageView.PAGE_URL;
    }

    VirtualPortCreateCommand command = new VirtualPortCreateCommand(requestLink);
    addCreateFormToModel(command, model);

    return PAGE_URL + CREATE;
  }

  @RequestMapping(method = RequestMethod.PUT)
  public String update(@Valid VirtualPortUpdateCommand command, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
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
    messageManager.addInfoFlashMessage(redirectAttributes, "info_virtualport_updated", port.getManagerLabel());

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

    messageManager.addInfoFlashMessage(redirectAttributes, "info_virtualport_deleted", virtualPort.getManagerLabel());

    return "redirect:" + PAGE_URL;
  }

  @RequestMapping(value = "/delete/{uuid}")
  public String deleteVirtualPort(@PathVariable("uuid") String link, Model model, RedirectAttributes redirectAttributes) {
    VirtualPortRequestLink requestLink = virtualPortService.findRequest(link);

    if (requestLink.getStatus() == RequestStatus.DELETE_REQUEST_PENDING) {
      Collection<VirtualPort> virtualPorts = requestLink.getVirtualResourceGroup().getVirtualPorts();

      for (VirtualPort virtualPort : virtualPorts) {
        virtualPortService.delete(virtualPort, Security.getUserDetails());
        requestLink.setStatus(RequestStatus.DELETE_REQUEST_APPROVED);
        virtualPortRequestLinkRepo.saveAndFlush(requestLink);
        messageManager.addInfoFlashMessage(redirectAttributes, "info_virtualport_deleted",
            virtualPort.getManagerLabel());
      }
    }

    return "redirect:" + PAGE_URL;
  }

  @Override
  protected String listUrl() {
    return PAGE_URL + LIST;
  }

  @Override
  protected List<VirtualPort> list(int firstPage, int maxItems, Sort sort, Model model) {
    return virtualPortService.findEntriesForManager(Security.getSelectedRole(), firstPage, maxItems, sort);
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
    private Long maxBandwidth;
    @Range(min = VlanRangesValidator.MINIMUM_VLAN_ID, max = VlanRangesValidator.MAXIMUM_VLAN_ID)
    private Integer vlanId;
    @NotNull
    private UniPort physicalPort;
    @NotNull
    private VirtualResourceGroup virtualResourceGroup;
    @NotNull
    private PhysicalResourceGroup physicalResourceGroup;

    public VirtualPortCommand() {
    }

    public VirtualPortCommand(String managerLabel, Long maxBandwidth, Integer vlanId,
        PhysicalResourceGroup physicalResourceGroup, UniPort physicalPort,
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

    public Long getMaxBandwidth() {
      return maxBandwidth;
    }

    public void setMaxBandwidth(Long maxBandwidth) {
      this.maxBandwidth = maxBandwidth;
    }

    public Integer getVlanId() {
      return vlanId;
    }

    public void setVlanId(Integer vlanId) {
      this.vlanId = vlanId;
    }

    public UniPort getPhysicalPort() {
      return physicalPort;
    }

    public void setPhysicalPort(UniPort physicalPort) {
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
  protected List<Long> getIdsOfAllAllowedEntries(Model model, Sort sort) {
    final VirtualPortView filter = WebUtils.getAttributeFromModel(FILTER_SELECT, model);
    return virtualPortService.findIdsForUserUsingFilter(Security.getUserDetails(), filter, sort);
  }

  @Override
  protected List<? extends VirtualPortView> transformToView(List<? extends VirtualPort> entities, RichUserDetails user) {
    return Lists.transform(entities, new Function<VirtualPort, VirtualPortView>() {
      @Override
      public VirtualPortView apply(VirtualPort port) {
        long counter = reservationService.findActiveByVirtualPort(port).size();
        return new VirtualPortView(port, nsiHelper, Optional.<Long> of(counter));
      }
    });
  }

  protected void setMessageManager(MessageManager messageManager) {
    this.messageManager = messageManager;
  }
}
