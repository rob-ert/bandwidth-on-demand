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
package nl.surfnet.bod.web.noc;

import static nl.surfnet.bod.web.WebUtils.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.service.*;
import nl.surfnet.bod.util.FullTextSearchResult;
import nl.surfnet.bod.util.Functions;
import nl.surfnet.bod.util.MessageManager;
import nl.surfnet.bod.util.ReflectiveFieldComparator;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.base.AbstractSearchableSortableListController;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.PhysicalPortView;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.lucene.queryParser.ParseException;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Controller
@RequestMapping("/noc/" + PhysicalPortController.PAGE_URL)
public class PhysicalPortController extends AbstractSearchableSortableListController<PhysicalPortView, PhysicalPort> {

  public static final String PAGE_URL = "physicalports";
  public static final String PAGE_UNALIGNED_URL = "/noc/" + PAGE_URL + "/unaligned";
  static final String MODEL_KEY = "createPhysicalPortCommand";

  @Resource
  private PhysicalPortService physicalPortService;

  @Resource
  private PhysicalResourceGroupService physicalResourceGroupService;

  @Resource
  private VirtualPortService virtualPortService;

  @Resource
  private ReservationService reservationService;

  @Resource
  private NocService nocService;

  @Resource
  private MessageManager messageManager;

  @Resource
  private ConnectionService connectionService;

  @RequestMapping(value = "add", method = RequestMethod.GET)
  public String addPhysicalPortForm(@RequestParam(value = "prg") Long prgId, Model model,
      RedirectAttributes redirectAttrs) {
    PhysicalResourceGroup prg = physicalResourceGroupService.find(prgId);
    if (prg == null) {
      return "redirect:/";
    }

    Collection<PhysicalPort> unallocatedPorts = physicalPortService.findUnallocated();

    if (unallocatedPorts.isEmpty()) {
      messageManager.addInfoFlashMessage(redirectAttrs, "info_physicalport_nounallocated");
      return "redirect:/noc/" + PhysicalResourceGroupController.PAGE_URL;
    }

    AddPhysicalPortCommand addCommand = new AddPhysicalPortCommand();
    addCommand.setPhysicalResourceGroup(prg);
    PhysicalPort port = Iterables.get(unallocatedPorts, 0);

    addCommand.setNmsPortId(port.getNmsPortId());
    addCommand.setNocLabel(port.getNocLabel());
    addCommand.setManagerLabel(port.hasManagerLabel() ? port.getManagerLabel() : "");
    addCommand.setBodPortId(port.getBodPortId());

    model.addAttribute("addPhysicalPortCommand", addCommand);
    model.addAttribute("unallocatedPhysicalPorts", unallocatedPorts);

    return "physicalports/addPhysicalPort";
  }

  @RequestMapping(value = "add", method = RequestMethod.POST)
  public String addPhysicalPort(@Valid AddPhysicalPortCommand addCommand, BindingResult result,
      RedirectAttributes redirectAttributes, Model model) {

    if (result.hasErrors()) {
      model.addAttribute("addPhysicalPortCommand", addCommand);
      model.addAttribute("unallocatedPhysicalPorts", physicalPortService.findUnallocated());
      return "physicalports/addPhysicalPort";
    }

    PhysicalPort port = physicalPortService.findByNmsPortId(addCommand.getNmsPortId());
    if (!Strings.isNullOrEmpty(addCommand.getManagerLabel())) {
      port.setManagerLabel(addCommand.getManagerLabel());
    }
    port.setNocLabel(addCommand.getNocLabel());
    port.setPhysicalResourceGroup(addCommand.getPhysicalResourceGroup());
    if (addCommand.getManagerLabel() == null || addCommand.getManagerLabel().isEmpty()) {
      port.setManagerLabel(null);
    }
    else {
      port.setManagerLabel(addCommand.getManagerLabel());
    }
    port.setBodPortId(addCommand.getBodPortId());

    physicalPortService.save(port);

    messageManager.addInfoFlashMessage(redirectAttributes, "info_physicalport_added", port.getNocLabel(), port
        .getPhysicalResourceGroup().getName());

    return "redirect:/noc/" + PhysicalResourceGroupController.PAGE_URL;
  }

  @RequestMapping(method = RequestMethod.PUT)
  public String update(@Valid CreatePhysicalPortCommand command, BindingResult result, Model model,
      final RedirectAttributes redirectAttributes) {

    if (result.hasErrors()) {
      model.addAttribute(MODEL_KEY, command);
      return PAGE_URL + UPDATE;
    }

    PhysicalPort portToSave = physicalPortService.findByNmsPortId(command.getNmsPortId());
    portToSave.setPhysicalResourceGroup(command.getPhysicalResourceGroup());
    portToSave.setNocLabel(command.getNocLabel());
    portToSave.setBodPortId(command.getBodPortId());
    if (Strings.emptyToNull(command.getManagerLabel()) != null) {
      portToSave.setManagerLabel(command.getManagerLabel());
    }

    physicalPortService.save(portToSave);

    model.asMap().clear();

    messageManager.addInfoFlashMessage(redirectAttributes, "info_physicalport_updated", portToSave.getNocLabel(),
        portToSave.getPhysicalResourceGroup().getName());

    return "redirect:physicalports";
  }

  @RequestMapping(method = RequestMethod.GET)
  @Override
  public String list(@RequestParam(value = PAGE_KEY, required = false) Integer page,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "order", required = false) String order, Model model) {

    model.addAttribute(WebUtils.FILTER_SELECT, PhysicalPortFilter.ALLOCATED);
    model.addAttribute(WebUtils.FILTER_LIST, getAvailableFilters());

    return super.list(page, sort, order, model);
  }

  @Override
  @RequestMapping(value = "search", method = RequestMethod.GET)
  public String search(Integer page, String sort, String order, String search, Model model) {

    model.addAttribute(FILTER_SELECT, PhysicalPortFilter.ALLOCATED);
    model.addAttribute(FILTER_LIST, getAvailableFilters());

    return super.search(page, sort, order, search, model);
  }

  @RequestMapping(value = "/free", method = RequestMethod.GET)
  public String listUnallocated(@RequestParam(value = PAGE_KEY, required = false) Integer page,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "order", required = false) String order, Model model) {

    List<PhysicalPortView> transformedUnallocatedPhysicalPorts = Functions
        .transformUnallocatedPhysicalPorts(physicalPortService.findUnallocatedEntries(calculateFirstPage(page),
            MAX_ITEMS_PER_PAGE));

    if (!StringUtils.hasText(sort)) {
      sort = "nocLabel";
    }

    sortExternalResources(sort, order, model, transformedUnallocatedPhysicalPorts);

    model.addAttribute(DATA_LIST, transformedUnallocatedPhysicalPorts);
    model.addAttribute(MAX_PAGES_KEY, calculateMaxPages(physicalPortService.countUnallocated()));
    model.addAttribute(FILTER_SELECT, PhysicalPortFilter.UN_ALLOCATED);
    model.addAttribute(FILTER_LIST, getAvailableFilters());

    return PAGE_URL + "/listunallocated";
  }
  
  @VisibleForTesting
  void setMessageManager(MessageManager messageManager) {
    this.messageManager = messageManager;
  }

  private void sortExternalResources(String sort, String order, Model model,
      final List<PhysicalPortView> transformedUnallocatedPhysicalPorts) {

    prepareSortOptions(sort, order, model);
    Collections.sort(transformedUnallocatedPhysicalPorts, new ReflectiveFieldComparator(sort));

    if (StringUtils.hasText(order)) {
      if ("DESC".equals(order)) {
        Collections.reverse(transformedUnallocatedPhysicalPorts);
      }
    }
  }

  @RequestMapping(value = "/unaligned", method = RequestMethod.GET)
  public String listUnaligned(@RequestParam(value = PAGE_KEY, required = false) Integer page,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "order", required = false) String order, //
      Model model) {

    Sort sortOptions = prepareSortOptions(sort, order, model);

    List<PhysicalPortView> allocatedPhysicalPorts = Functions.transformAllocatedPhysicalPorts(physicalPortService
        .findUnalignedPhysicalPorts(calculateFirstPage(page), MAX_ITEMS_PER_PAGE, sortOptions), virtualPortService,
        reservationService);

    model.addAttribute(MAX_PAGES_KEY, calculateMaxPages(physicalPortService.countUnalignedPhysicalPorts()));
    model.addAttribute(WebUtils.DATA_LIST, allocatedPhysicalPorts);
    model.addAttribute(WebUtils.FILTER_SELECT, PhysicalPortFilter.UN_ALIGNED);
    model.addAttribute(WebUtils.FILTER_LIST, getAvailableFilters());

    return listUrl();
  }

  @RequestMapping(value = "/unaligned/search", method = RequestMethod.GET)
  public String searchUnaligned(@RequestParam(value = PAGE_KEY, required = false) final Integer page,
      @RequestParam final String search, @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "order", required = false) String order, Model model) {

    List<Long> unalignedPorts = getIdsOfAllAllowedEntries(model, prepareSortOptions(sort, order, model));

    try {
      FullTextSearchResult<PhysicalPort> searchResult = getFullTextSearchableService().searchForInFilteredList(
          PhysicalPort.class, search, calculateFirstPage(page), MAX_ITEMS_PER_PAGE, Security.getUserDetails(),
          unalignedPorts);

      model.addAttribute(PARAM_SEARCH, StringEscapeUtils.escapeHtml(search));
      model.addAttribute(MAX_PAGES_KEY, calculateMaxPages(searchResult.getTotalCount()));
      model.addAttribute(DATA_LIST, transformToView(searchResult.getResultList(), Security.getUserDetails()));
      model.addAttribute(FILTER_SELECT, PhysicalPortFilter.UN_ALIGNED);
      model.addAttribute(FILTER_LIST, getAvailableFilters());
    }
    catch (ParseException e) {
      model.addAttribute(MessageManager.WARN_MESSAGES_KEY, Lists
          .newArrayList("Sorry, we could not process your search query."));

    }

    return listUrl();
  }

  @RequestMapping(value = "/mtosi", method = RequestMethod.GET)
  public String listMtosi(@RequestParam(value = PAGE_KEY, required = false) final Integer page,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "order", required = false) String order, final Model model) {

    final List<PhysicalPortView> transformUnallocatedPhysicalPorts = Functions
        .transformUnallocatedPhysicalPorts(physicalPortService.findUnallocatedMTOSIEntries(calculateFirstPage(page),
            MAX_ITEMS_PER_PAGE));

    if (!StringUtils.hasText(sort)) {
      sort = "nocLabel";
    }
    sortExternalResources(sort, order, model, transformUnallocatedPhysicalPorts);

    model.addAttribute(DATA_LIST, transformUnallocatedPhysicalPorts);
    model.addAttribute(MAX_PAGES_KEY, calculateMaxPages(physicalPortService.countUnallocatedMTOSI()));
    model.addAttribute(FILTER_SELECT, PhysicalPortFilter.MTOSI);
    model.addAttribute(FILTER_LIST, getAvailableFilters());

    return PAGE_URL + "/listunallocated";
  }

  @RequestMapping(value = "edit", params = ID_KEY, method = RequestMethod.GET)
  public String updateForm(@RequestParam(ID_KEY) final String nmsPortId, final Model model) {
    PhysicalPort port;
    try {
      port = physicalPortService.findByNmsPortId(nmsPortId);
    }
    catch (IllegalStateException e) {
      return "redirect:";
    }

    model.addAttribute(MODEL_KEY, new CreatePhysicalPortCommand(port));

    return PAGE_URL + UPDATE;
  }

  @RequestMapping(value = DELETE, params = ID_KEY, method = RequestMethod.DELETE)
  public String delete(@RequestParam(ID_KEY) final String nmsPortId,
      @RequestParam(value = PAGE_KEY, required = false) final Integer page, final Model uiModel) {
    final PhysicalPort physicalPort = physicalPortService.findByNmsPortId(nmsPortId);
    final Collection<VirtualPort> virtualPorts = virtualPortService.findAllForPhysicalPort(physicalPort);
    final RichUserDetails userDetails = Security.getUserDetails();
    virtualPortService.deleteVirtualPorts(virtualPorts, userDetails);
    physicalPortService.deleteByNmsPortId(nmsPortId);

    uiModel.asMap().clear();
    uiModel.addAttribute(PAGE_KEY, (page == null) ? "1" : page.toString());

    return "redirect:";
  }

  @RequestMapping(value = "move", method = RequestMethod.GET)
  public String moveForm(@RequestParam Long id, Model model, RedirectAttributes redirectAttrs) {
    PhysicalPort port = physicalPortService.find(id);

    if (port == null) {
      redirectAttrs.addFlashAttribute(MessageManager.INFO_MESSAGES_KEY, ImmutableList.of("Could not find port.."));
      return "redirect:/noc/physicalports";
    }

    Collection<PhysicalPort> unallocatedPorts = physicalPortService.findUnallocated();
    if (unallocatedPorts.isEmpty()) {
      messageManager.addInfoFlashMessage(redirectAttrs, "info_physicalport_nounallocated");
      return "redirect:/noc/" + PhysicalResourceGroupController.PAGE_URL;
    }

    long numberOfVirtualPorts = virtualPortService.countForPhysicalPort(port);
    long numberOfReservations = reservationService.countForPhysicalPort(port);
    long numberOfActiveReservations = reservationService.countActiveReservationsForPhysicalPort(port);

    model.addAttribute("relatedObjects", new RelatedObjects(numberOfVirtualPorts, numberOfReservations,
        numberOfActiveReservations));
    model.addAttribute("physicalPort", port);
    model.addAttribute("unallocatedPhysicalPorts", unallocatedPorts);
    model.addAttribute("movePhysicalPortCommand", new MovePhysicalPortCommand(port));

    return PAGE_URL + "/move";
  }

  @Override
  protected List<String> translateSortProperty(String sortProperty) {
    if (sortProperty.equals("instituteName")) {
      return ImmutableList.of("physicalResourceGroup.institute.name");
    }

    return super.translateSortProperty(sortProperty);
  }

  public static final class RelatedObjects {
    private final Long numberOfVirtualPorts;
    private final Long numberOfReservations;
    private final Long numberOfActiveReservations;

    public RelatedObjects(Long numberOfVirtualPorts, Long numberOfReservations, Long numberOfActiveReservations) {
      this.numberOfActiveReservations = numberOfActiveReservations;
      this.numberOfVirtualPorts = numberOfVirtualPorts;
      this.numberOfReservations = numberOfReservations;
    }

    public Long getNumberOfVirtualPorts() {
      return numberOfVirtualPorts;
    }

    public Long getNumberOfReservations() {
      return numberOfReservations;
    }

    public Long getNumberOfActiveReservations() {
      return numberOfActiveReservations;
    }
  }

  @RequestMapping(value = "move", method = RequestMethod.PUT)
  public String move(MovePhysicalPortCommand command, BindingResult result, Model model) {

    PhysicalPort newPort = physicalPortService.findByNmsPortId(command.getNewPhysicalPort());
    PhysicalPort oldPort = physicalPortService.find(command.getId());

    Collection<Reservation> reservations = nocService.movePort(oldPort, newPort);

    model.addAttribute("reservations", reservations);

    return "physicalports/moveResult";
  }

  /**
   * Puts all {@link PhysicalResourceGroup}s on the model, needed to relate a
   * group to a {@link PhysicalPort}.
   * 
   * @return Collection<PhysicalResourceGroup>
   */
  @ModelAttribute(PhysicalResourceGroupController.MODEL_KEY_LIST)
  public Collection<PhysicalResourceGroup> populatePhysicalResourceGroups() {
    return physicalResourceGroupService.findAll();
  }

  @Override
  protected String listUrl() {
    return PAGE_URL + LIST;
  }

  @Override
  protected List<PhysicalPortView> list(int firstPage, int maxItems, Sort sort, Model model) {

    return Functions.transformAllocatedPhysicalPorts(physicalPortService
        .findAllocatedEntries(firstPage, maxItems, sort), virtualPortService, reservationService);
  }

  @Override
  protected long count(Model model) {
    return physicalPortService.countAllocated();
  }

  @Override
  protected String getDefaultSortProperty() {
    return "nocLabel";
  }

  public static class MovePhysicalPortCommand {
    private Long id;
    private String newPhysicalPort;

    public MovePhysicalPortCommand() {
    }

    public MovePhysicalPortCommand(PhysicalPort port) {
      this.id = port.getId();
    }

    public String getNewPhysicalPort() {
      return newPhysicalPort;
    }

    public void setNewPhysicalPort(String newPhysicalPort) {
      this.newPhysicalPort = newPhysicalPort;
    }

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }
  }

  public static class PhysicalPortCommand {
    @NotNull
    private PhysicalResourceGroup physicalResourceGroup;
    @NotEmpty
    private String nmsPortId;
    @NotEmpty
    private String nocLabel;
    @NotEmpty
    private String bodPortId;

    private String managerLabel;

    public PhysicalPortCommand() {
    }

    public PhysicalResourceGroup getPhysicalResourceGroup() {
      return physicalResourceGroup;
    }

    public void setPhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup) {
      this.physicalResourceGroup = physicalResourceGroup;
    }

    public String getNmsPortId() {
      return nmsPortId;
    }

    public void setNmsPortId(String nmsPortId) {
      this.nmsPortId = nmsPortId;
    }

    public String getNocLabel() {
      return nocLabel;
    }

    public void setNocLabel(String nocLabel) {
      this.nocLabel = nocLabel;
    }

    public String getBodPortId() {
      return bodPortId;
    }

    public void setBodPortId(String portId) {
      this.bodPortId = portId;
    }

    public String getManagerLabel() {
      return managerLabel;
    }

    public void setManagerLabel(String managerLabel) {
      this.managerLabel = managerLabel;
    }

    @Override
    public String toString() {
      return "PhysicalPortCommand [physicalResourceGroup=" + physicalResourceGroup + ", nmsPortId=" + nmsPortId
          + ", nocLabel=" + nocLabel + ", bodPortId=" + bodPortId + ", managerLabel=" + managerLabel + "]";
    }

  }

  public static final class AddPhysicalPortCommand extends PhysicalPortCommand {

  }

  public static final class CreatePhysicalPortCommand extends PhysicalPortCommand {
    private Integer version;

    public CreatePhysicalPortCommand() {
    }

    public CreatePhysicalPortCommand(PhysicalPort port) {
      setNmsPortId(port.getNmsPortId());
      setPhysicalResourceGroup(port.getPhysicalResourceGroup());
      setNocLabel(port.getNocLabel());
      setManagerLabel(port.hasManagerLabel() ? port.getManagerLabel() : "");
      setBodPortId(port.getBodPortId());
      this.version = port.getVersion();
    }

    public Integer getVersion() {
      return version;
    }

    public void setVersion(Integer version) {
      this.version = version;
    }

  }

  @Override
  protected AbstractFullTextSearchService<PhysicalPort> getFullTextSearchableService() {
    return physicalPortService;
  }

  private List<PhysicalPortFilter> getAvailableFilters() {
    return Lists.newArrayList(PhysicalPortFilter.ALLOCATED, PhysicalPortFilter.UN_ALLOCATED,
        PhysicalPortFilter.UN_ALIGNED, PhysicalPortFilter.MTOSI);
  }

  @Override
  protected List<Long> getIdsOfAllAllowedEntries(Model model, Sort sort) {
    return physicalPortService.findIds(Optional.<Sort> fromNullable(sort));
  }

  public static final class PhysicalPortFilter {
    public static final PhysicalPortFilter ALLOCATED = new PhysicalPortFilter("Allocated", "/");
    public static final PhysicalPortFilter UN_ALLOCATED = new PhysicalPortFilter("Unllocated", "/free");
    public static final PhysicalPortFilter UN_ALIGNED = new PhysicalPortFilter("Unaligned", "/unaligned");
    public static final PhysicalPortFilter MTOSI = new PhysicalPortFilter("OneControl (beta)", "/mtosi");

    private final String path;
    private final String name;

    private PhysicalPortFilter(String name, String path) {
      this.name = name;
      this.path = path;
    }

    public String getPath() {
      return path;
    }

    public String getName() {
      return name;
    }

  }

  @Override
  protected List<PhysicalPortView> transformToView(List<PhysicalPort> entities, RichUserDetails user) {
    return Functions.transformAllocatedPhysicalPorts(entities, virtualPortService, reservationService);
  }

  

}
