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
package nl.surfnet.bod.web.noc;

import static nl.surfnet.bod.web.WebUtils.EDIT;
import static nl.surfnet.bod.web.WebUtils.FILTER_LIST;
import static nl.surfnet.bod.web.WebUtils.FILTER_SELECT;
import static nl.surfnet.bod.web.WebUtils.ID_KEY;
import static nl.surfnet.bod.web.WebUtils.PAGE_KEY;

import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import nl.surfnet.bod.domain.NbiPort;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.UniPort;
import nl.surfnet.bod.service.AbstractFullTextSearchService;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.util.Functions;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.base.AbstractSearchableSortableListController;
import nl.surfnet.bod.web.base.MessageManager;
import nl.surfnet.bod.web.noc.PhysicalPortController.PhysicalPortFilter;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.view.PhysicalPortView;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/noc/physicalports/uni")
public class UniPortController extends AbstractSearchableSortableListController<PhysicalPortView, UniPort> {

  @Resource private PhysicalPortService physicalPortService;
  @Resource private PhysicalResourceGroupService physicalResourceGroupService;
  @Resource private VirtualPortService virtualPortService;
  @Resource private ReservationService reservationService;
  @Resource private MessageManager messageManager;

  @RequestMapping(method = RequestMethod.POST)
  public String createUniPort(@Valid CreateUniPortCommand createPortCommand, BindingResult result, RedirectAttributes redirectAttributes, Model model) {
    if (physicalPortService.findByBodPortId(createPortCommand.getBodPortId()) != null) {
      result.rejectValue("bodPortId", "validation.not.unique");
    }
    if(!PhysicalPortController.containsLetters(createPortCommand.getBodPortId())) {
      result.rejectValue("bodPortId", "validation.should.contain.letter");
    }

    if (result.hasErrors()) {
      model.addAttribute("createUniPortCommand", createPortCommand);
      return "noc/physicalports/uni/create";
    }

    Optional<NbiPort> nbiPort = physicalPortService.findNbiPort(createPortCommand.getNmsPortId());

    if (!nbiPort.isPresent()) {
      return "redirect:";
    }

    UniPort uniPort = (UniPort) PhysicalPort.create(nbiPort.get());

    if (Strings.isNullOrEmpty(createPortCommand.getManagerLabel())) {
      uniPort.setManagerLabel(null);
    } else {
      uniPort.setManagerLabel(createPortCommand.getManagerLabel());
    }
    uniPort.setPhysicalResourceGroup(createPortCommand.getPhysicalResourceGroup());
    uniPort.setNocLabel(createPortCommand.getNocLabel());
    uniPort.setBodPortId(createPortCommand.getBodPortId());

    physicalPortService.save(uniPort);

    messageManager.addInfoFlashMessage(redirectAttributes, "info_physicalport_uni_created", uniPort.getNocLabel(), uniPort.getPhysicalResourceGroup().getName());

    return "redirect:/noc/physicalports";
  }

  @RequestMapping(value = EDIT, params = ID_KEY, method = RequestMethod.GET)
  public String updateForm(@RequestParam(ID_KEY) Long id, Model model) {
    UniPort uniPort = physicalPortService.findUniPort(id);

    if (uniPort == null) {
      return "redirect:";
    }

    model.addAttribute("updateUniPortCommand", new UpdateUniPortCommand(uniPort));

    return "noc/physicalports/uni/update";
  }

  @RequestMapping(method = RequestMethod.PUT)
  public String update(@Valid UpdateUniPortCommand command, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
    UniPort uniPort = (UniPort) physicalPortService.findByNmsPortId(command.getNmsPortId());

    if (uniPort == null) {
      return "redirect:";
    }

    if (!uniPort.getBodPortId().equals(command.getBodPortId()) && physicalPortService.findByBodPortId(command.getBodPortId()) != null) {
      result.rejectValue("bodPortId", "validation.not.unique");
    }
    if (!PhysicalPortController.containsLetters(command.getBodPortId())) {
      result.rejectValue("bodPortId", "validation.should.contain.letter");
    }
    if (result.hasErrors()) {
      model.addAttribute("updateUniPortCommand", command);
      return "noc/physicalports/uni/update";
    }

    uniPort.setPhysicalResourceGroup(command.getPhysicalResourceGroup());
    if (!Strings.isNullOrEmpty(command.getManagerLabel())) {
      uniPort.setManagerLabel(command.getManagerLabel());
    } else {
      uniPort.setManagerLabel(null);
    }

    uniPort.setNocLabel(command.getNocLabel());
    uniPort.setBodPortId(command.getBodPortId());

    physicalPortService.save(uniPort);

    model.asMap().clear();

    messageManager.addInfoFlashMessage(redirectAttributes, "info_physicalport_updated", uniPort.getNocLabel(), uniPort.getPhysicalResourceGroup().getName());

    return "redirect:/noc/physicalports";
  }

  @RequestMapping(method = RequestMethod.GET)
  @Override
  public String list(@RequestParam(value = PAGE_KEY, required = false) Integer page,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "order", required = false) String order, Model model) {

    model.addAttribute(WebUtils.FILTER_SELECT, PhysicalPortFilter.UNI_ALLOCATED);
    model.addAttribute(WebUtils.FILTER_LIST, PhysicalPortFilter.getAvailableFilters());

    return super.list(page, sort, order, model);
  }

  @Override
  @RequestMapping(value = "search", method = RequestMethod.GET)
  public String search(Integer page, String sort, String order, String search, Model model) {
    model.addAttribute(FILTER_SELECT, PhysicalPortFilter.UNI_ALLOCATED);
    model.addAttribute(FILTER_LIST, PhysicalPortFilter.getAvailableFilters());

    return super.search(page, sort, order, search, model);
  }

  @RequestMapping(params = ID_KEY, method = RequestMethod.DELETE)
  public String delete(Long id, @RequestParam(value = PAGE_KEY, required = false) Integer page, Model uiModel) {
    physicalPortService.delete(id);

    uiModel.asMap().clear();
    uiModel.addAttribute(PAGE_KEY, page == null ? "1" : page.toString());

    return "redirect:";
  }

  @RequestMapping(value = "add", method = RequestMethod.GET)
  public String addUniPortForm(@RequestParam(value = "prg") Long prgId, Model model, RedirectAttributes redirectAttrs) {
    PhysicalResourceGroup prg = physicalResourceGroupService.find(prgId);
    if (prg == null) {
      return "redirect:/";
    }

    Collection<NbiPort> unallocatedUniPorts = physicalPortService.findUnallocatedUniPorts();

    if (unallocatedUniPorts.isEmpty()) {
      messageManager.addInfoFlashMessage(redirectAttrs, "info_physicalport_nounallocateduni");
      return "redirect:/noc/" + PhysicalResourceGroupController.PAGE_URL;
    }

    NbiPort port = Iterables.get(unallocatedUniPorts, 0);

    AddPhysicalPortCommand addCommand = new AddPhysicalPortCommand();
    addCommand.setPhysicalResourceGroup(prg);
    addCommand.setNmsPortId(port.getNmsPortId());
    addCommand.setNocLabel(port.getSuggestedNocLabel());
    addCommand.setBodPortId(port.getSuggestedBodPortId());

    model.addAttribute("addPhysicalPortCommand", addCommand);
    model.addAttribute("unallocatedPhysicalPorts", unallocatedUniPorts);

    return "noc/physicalports/addPhysicalPort";
  }

  @RequestMapping(value = "add", method = RequestMethod.POST)
  public String addUniPort(@Valid AddPhysicalPortCommand addCommand, BindingResult result, RedirectAttributes redirectAttributes, Model model) {
    if (physicalPortService.findByBodPortId(addCommand.getBodPortId()) != null) {
      result.rejectValue("bodPortId", "validation.not.unique");
    }
    if (!PhysicalPortController.containsLetters(addCommand.getBodPortId())) {
      result.rejectValue("bodPortId", "validation.should.contain.letter");
    }

    if (result.hasErrors()) {
      model.addAttribute("addPhysicalPortCommand", addCommand);
      model.addAttribute("unallocatedPhysicalPorts", physicalPortService.findUnallocated());
      return "noc/physicalports/addPhysicalPort";
    }

    Optional<NbiPort> nbiPort = physicalPortService.findNbiPort(addCommand.getNmsPortId());

    if (!nbiPort.isPresent()) {
      return "redirect:";
    }

    UniPort uniPort = new UniPort(nbiPort.get());
    if (Strings.isNullOrEmpty(addCommand.getManagerLabel())) {
      uniPort.setManagerLabel(null);
    } else {
      uniPort.setManagerLabel(addCommand.getManagerLabel());
    }
    uniPort.setNocLabel(addCommand.getNocLabel());
    uniPort.setPhysicalResourceGroup(addCommand.getPhysicalResourceGroup());
    uniPort.setBodPortId(addCommand.getBodPortId());

    physicalPortService.save(uniPort);

    messageManager.addInfoFlashMessage(redirectAttributes, "info_physicalport_uni_created", uniPort.getNocLabel(), uniPort.getPhysicalResourceGroup().getName());

    return "redirect:/noc/" + PhysicalResourceGroupController.PAGE_URL;
  }

  public static class CreateUniPortCommand extends PhysicalPortController.PhysicalPortCommand {
    @NotNull
    private PhysicalResourceGroup physicalResourceGroup;

    private String managerLabel;

    public CreateUniPortCommand() {
    }

    public CreateUniPortCommand(UniPort port) {
      setNmsPortId(port.getNmsPortId());
      setNocLabel(port.getNocLabel());
      setBodPortId(port.getBodPortId());
      setManagerLabel(((UniPort) port).hasManagerLabel() ? ((UniPort) port).getManagerLabel() : "");
      setPhysicalResourceGroup(((UniPort) port).getPhysicalResourceGroup());
    }

    public PhysicalResourceGroup getPhysicalResourceGroup() {
      return physicalResourceGroup;
    }

    public void setPhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup) {
      this.physicalResourceGroup = physicalResourceGroup;
    }

    public String getManagerLabel() {
      return managerLabel;
    }

    public void setManagerLabel(String managerLabel) {
      this.managerLabel = managerLabel;
    }

  }

  public static final class AddPhysicalPortCommand extends CreateUniPortCommand {

  }

  public static final class UpdateUniPortCommand extends CreateUniPortCommand {
    private Long id;
    private Integer version;

    public UpdateUniPortCommand() {
    }

    public UpdateUniPortCommand(UniPort uniPort) {
      super(uniPort);
      this.version = uniPort.getVersion();
      this.id = uniPort.getId();
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
  protected List<? extends PhysicalPortView> transformToView(List<? extends UniPort> entities, RichUserDetails user) {
    return Functions.transformAllocatedPhysicalPorts(entities, virtualPortService, reservationService);
  }

  @Override
  protected String listUrl() {
    return "noc/physicalports/uni/list";
  }

  @Override
  protected List<UniPort> list(int firstPage, int maxItems, Sort sort, Model model) {
    return physicalPortService.findAllocatedUniEntries(firstPage, maxItems, sort);
  }

  @Override
  protected long count(Model model) {
    return physicalPortService.countAllocated();
  }

  @Override
  protected String getDefaultSortProperty() {
    return "nocLabel";
  }

  @Override
  protected List<Long> getIdsOfAllAllowedEntries(Model model, Sort sort) {
    return physicalPortService.findIds(Optional.<Sort> fromNullable(sort));
  }

  @Override
  protected AbstractFullTextSearchService<UniPort> getFullTextSearchableService() {
    return physicalPortService;
  }

  @Override
  protected List<String> translateSortProperty(String sortProperty) {
    if (sortProperty.equals("instituteName")) {
      return ImmutableList.of("physicalResourceGroup.institute.name");
    }

    return super.translateSortProperty(sortProperty);
  }

  /**
   * Puts all {@link PhysicalResourceGroup}s on the model, needed to relate a
   * group to a {@link UniPort}.
   *
   * @return Collection<PhysicalResourceGroup>
   */
  @ModelAttribute(PhysicalResourceGroupController.MODEL_KEY_LIST)
  public Collection<PhysicalResourceGroup> populatePhysicalResourceGroups() {
    return physicalResourceGroupService.findAll();
  }

  @VisibleForTesting
  void setMessageManager(MessageManager messageManager) {
    this.messageManager = messageManager;
  }

}
