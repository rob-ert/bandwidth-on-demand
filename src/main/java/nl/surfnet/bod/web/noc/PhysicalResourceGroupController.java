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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import nl.surfnet.bod.domain.*;
import nl.surfnet.bod.service.*;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.base.AbstractSearchableSortableListController;
import nl.surfnet.bod.web.base.MessageManager;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.PhysicalResourceGroupView;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

@Controller("nocPhysicalResourceGroupController")
@RequestMapping("/noc/" + PhysicalResourceGroupController.PAGE_URL)
public class PhysicalResourceGroupController extends
    AbstractSearchableSortableListController<PhysicalResourceGroupView, PhysicalResourceGroup> {

  public static final String PAGE_URL = "institutes";
  static final String MODEL_KEY = "physicalResourceGroupCommand";
  static final String MODEL_KEY_LIST = "physicalResourceGroup" + WebUtils.LIST_POSTFIX;

  @Resource
  private PhysicalResourceGroupService physicalResourceGroupService;

  @Resource
  private InstituteService instituteService;

  @Resource
  private MessageManager messageManager;

  @Resource
  private PhysicalPortService physicalPortService;

  @Resource
  private VirtualPortService virtualPortService;

  @Resource
  private ReservationService reservationService;

  @RequestMapping(method = RequestMethod.POST)
  public String create(
    @Valid PhysicalResourceGroupCommand command,
    BindingResult result,
    RedirectAttributes redirectAttributes,
    Model model) {

    PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroup();
    command.copyFieldsTo(physicalResourceGroup);
    fillInstitute(command, physicalResourceGroup);

    validateAdminGroupUnique(command, result);

    if (result.hasErrors()) {
      model.addAttribute("sabGroupPrefix", SabGroupService.GROUP_PREFIX);
      model.addAttribute(MODEL_KEY, command);
      return "noc/" + PAGE_URL + CREATE;
    }

    model.asMap().clear();

    physicalResourceGroupService.save(physicalResourceGroup);
    physicalResourceGroupService.sendActivationRequest(physicalResourceGroup);

    messageManager.addInfoFlashMessage(redirectAttributes, "info_activation_request_send", physicalResourceGroup
        .getName(), physicalResourceGroup.getManagerEmail());

    // Force refresh of roles
    SecurityContextHolder.clearContext();

    return "redirect:" + PAGE_URL;
  }

  private void validateAdminGroupUnique(PhysicalResourceGroupCommand command, BindingResult result) {
    List<PhysicalResourceGroup> groups = physicalResourceGroupService.findByAdminGroup(command.getAdminGroup());

    if (command.getId() == null && !groups.isEmpty()) {
      result.rejectValue("adminGroup", "validation.not.unique");
    }

    if (command.getId() != null && (groups.size() > 1 || groups.size() == 1 && !Iterables.getOnlyElement(groups).getId().equals(command.getId()))) {
      result.rejectValue("adminGroup", "validation.not.unique");
    }
  }

  @RequestMapping(value = CREATE, method = RequestMethod.GET)
  public String createForm(final Model model) {
    model.addAttribute("sabGroupPrefix", SabGroupService.GROUP_PREFIX);
    model.addAttribute(MODEL_KEY, new PhysicalResourceGroupCommand());

    return "noc/" + PAGE_URL + CREATE;
  }

  @RequestMapping(method = RequestMethod.PUT)
  public String update(@Valid PhysicalResourceGroupCommand command, BindingResult result,
      Model model, RedirectAttributes redirectAttributes) {

    PhysicalResourceGroup physicalResourceGroup = physicalResourceGroupService.find(command.getId());

    if (physicalResourceGroup == null) {
      return "redirect:" + PAGE_URL;
    }

    validateAdminGroupUnique(command, result);

    fillInstitute(command, physicalResourceGroup);
    command.copyFieldsTo(physicalResourceGroup);

    if (result.hasErrors()) {
      model.addAttribute(MODEL_KEY, command);
      return "noc/" + PAGE_URL + UPDATE;
    }

    model.asMap().clear();

    physicalResourceGroupService.update(physicalResourceGroup);

    if (command.isManagerEmailChanged()) {
      physicalResourceGroupService.sendActivationRequest(physicalResourceGroup);
      messageManager.addInfoFlashMessage(redirectAttributes, "info_activation_request_send", physicalResourceGroup
          .getName(), physicalResourceGroup.getManagerEmail());
    }

    // Force refresh of roles
    SecurityContextHolder.clearContext();

    return "redirect:" + PAGE_URL;
  }

  @RequestMapping(value = EDIT, params = ID_KEY, method = RequestMethod.GET)
  public String updateForm(@RequestParam(ID_KEY) final Long id, final Model uiModel) {
    PhysicalResourceGroup group = physicalResourceGroupService.find(id);

    uiModel.addAttribute(MODEL_KEY, new PhysicalResourceGroupCommand(group));
    return "noc/" + PAGE_URL + UPDATE;
  }

  @RequestMapping(value = DELETE, params = ID_KEY, method = RequestMethod.DELETE)
  public String delete(
    @RequestParam(ID_KEY) Long id,
    @RequestParam(value = PAGE_KEY, required = false) Integer page,
    Model model) {

    PhysicalResourceGroup physicalResourceGroup = physicalResourceGroupService.find(id);

    for (PhysicalPort physicalPort : physicalResourceGroup.getPhysicalPorts()) {
      Collection<VirtualPort> virtualPorts = virtualPortService.findAllForPhysicalPort(physicalPort);

      virtualPortService.deleteVirtualPorts(virtualPorts, Security.getUserDetails());
      // physicalPort's will be cascade deleted when deleting the physicalResourceGroup?
    }

    physicalResourceGroupService.delete(physicalResourceGroup.getId());

    model.asMap().clear();

    model.addAttribute(PAGE_KEY, (page == null) ? "1" : page.toString());

    // Force refresh of roles
    SecurityContextHolder.clearContext();

    return "redirect:";
  }

  @Override
  protected List<String> translateSortProperty(String sortProperty) {
    if (sortProperty.equals("institute")) {
      return ImmutableList.of("institute.name");
    }

    return super.translateSortProperty(sortProperty);
  }

  private void fillInstitute(final PhysicalResourceGroupCommand command, PhysicalResourceGroup group) {
    if (command.getInstituteId() == null) {
      return;
    }

    Institute institute = instituteService.find(command.getInstituteId());
    command.setInstitute(institute);
    group.setInstitute(institute);
  }

  @Override
  protected String listUrl() {
    return "noc/" + PAGE_URL + LIST;
  }

  @Override
  protected List<PhysicalResourceGroupView> list(int firstPage, int maxItems, Sort sort, Model model) {
    final List<PhysicalResourceGroup> physycalResourceGroups = physicalResourceGroupService.findEntries(firstPage,
        maxItems, sort);

    final List<VirtualPort> virtualPorts = new ArrayList<>();
    final List<Reservation> reservations = new ArrayList<>();
    List<PhysicalPort> physicalPorts = new ArrayList<>();

    for (final PhysicalResourceGroup physycalResourceGroup : physycalResourceGroups) {
      physicalPorts = physicalPortService.findAllocatedEntriesForPhysicalResourceGroup(physycalResourceGroup,
          MAX_ITEMS_PER_PAGE, MAX_ITEMS_PER_PAGE, null);
      for (final PhysicalPort physicalPort : physicalPorts) {
        virtualPorts.addAll(virtualPortService.findAllForPhysicalPort(physicalPort));
        reservations.addAll(reservationService.findActiveByPhysicalPort(physicalPort));
      }
    }

    return transformToView(physycalResourceGroups, Security.getUserDetails());
  }

  @Override
  protected long count(Model model) {
    return physicalResourceGroupService.count();
  }

  @Override
  protected String getDefaultSortProperty() {
    return "managerEmail";
  }

  public static final class PhysicalResourceGroupCommand {
    private Long id;
    private Integer version;
    @NotNull
    private Long instituteId;
    private Institute institute;
    @NotEmpty
    private String adminGroup;
    @NotEmpty
    @Email(message = "Not a valid email address")
    private String managerEmail;
    private boolean active = false;
    private boolean managerEmailChanged;
    private String authMethod = "sab";

    public PhysicalResourceGroupCommand() {
    }

    public PhysicalResourceGroupCommand(PhysicalResourceGroup physicalResourceGroup) {
      this.id = physicalResourceGroup.getId();
      this.version = physicalResourceGroup.getVersion();

      this.instituteId = physicalResourceGroup.getInstitute() != null ? physicalResourceGroup.getInstitute().getId()
          : null;
      this.institute = physicalResourceGroup.getInstitute();
      this.adminGroup = physicalResourceGroup.getAdminGroup();
      this.active = physicalResourceGroup.isActive();
      this.managerEmail = physicalResourceGroup.getManagerEmail();
    }

    /**
     * Copies fields this command object to the given domainOjbect. Only the
     * fields that can be changed in the UI will be copied. Determines if the
     * {@link #managerEmail} has changed.
     *
     * @param physicalResourceGroup
     *          The {@link PhysicalResourceGroup} the copy the field to.
     */
    public void copyFieldsTo(PhysicalResourceGroup physicalResourceGroup) {
      managerEmailChanged = hasManagerEmailChanged(physicalResourceGroup.getManagerEmail());

      // Never copy id, should be generated by jpa
      physicalResourceGroup.setAdminGroup(adminGroup.trim());
      physicalResourceGroup.setManagerEmail(managerEmail);
    }

    private boolean hasManagerEmailChanged(String newEmail) {
      return managerEmail == null || !managerEmail.equals(newEmail);
    }

    public void setId(Long id) {
      this.id = id;
    }

    public Long getId() {
      return id;
    }

    public Integer getVersion() {
      return version;
    }

    public Long getInstituteId() {
      return instituteId;
    }

    public void setInstituteId(Long instituteId) {
      this.instituteId = instituteId;
    }

    public String getAdminGroup() {
      return adminGroup;
    }

    public Institute getInstitute() {
      return institute;
    }

    public void setInstitute(Institute institute) {
      this.institute = institute;
    }

    public void setAdminGroup(String adminGroup) {
      this.adminGroup = adminGroup;
    }

    public String getManagerEmail() {
      return managerEmail;
    }

    public void setManagerEmail(String managerEmail) {
      this.managerEmail = managerEmail;
    }

    public boolean isActive() {
      return active;
    }

    public boolean isManagerEmailChanged() {
      return managerEmailChanged;
    }

    public String getName() {
      return institute != null ? institute.getName() : String.valueOf(instituteId);
    }

    public String getAuthMethod() {
      return authMethod;
    }

    public void setAuthMethod(String authMethod) {
      this.authMethod = authMethod;
    }
  }

  @Override
  protected AbstractFullTextSearchService<PhysicalResourceGroup> getFullTextSearchableService() {
    return physicalResourceGroupService;
  }

  @Override
  protected List<Long> getIdsOfAllAllowedEntries(Model model, Sort sort) {
    return physicalResourceGroupService.findAllIds(sort);
  }

  @Override
  protected List<PhysicalResourceGroupView> transformToView(List<PhysicalResourceGroup> entities, RichUserDetails user) {
    final List<PhysicalResourceGroupView> physicalResourceGroupViews = new ArrayList<>();
    for (final PhysicalResourceGroup physicalResourceGroup : entities) {
      final PhysicalResourceGroupView view = new PhysicalResourceGroupView(physicalResourceGroup);
      final List<VirtualPort> virtualPorts = new ArrayList<>();
      final List<Long> reservations = new ArrayList<>();
      final List<Long> physicalPortIds = physicalPortService.findIdsByRoleAndPhysicalResourceGroup(BodRole
          .createNocEngineer(), Optional.of(physicalResourceGroup), Optional.<Sort> absent());
      for (final long id : physicalPortIds) {
        final PhysicalPort physicalPort = physicalPortService.find(id);
        virtualPorts.addAll(virtualPortService.findAllForPhysicalPort(physicalPort));
        final long countActiveReservationsByVirtualPorts = reservationService
            .countActiveReservationsByVirtualPorts(virtualPorts);
        if (countActiveReservationsByVirtualPorts != 0L) {
          reservations.add(countActiveReservationsByVirtualPorts);
        }
      }
      view.setPhysicalPortsAmount(physicalPortIds.size());
      view.setReservationsAmount(reservations.size());
      view.setVirtualPortsAmount(virtualPorts.size());
      physicalResourceGroupViews.add(view);
    }
    return physicalResourceGroupViews;
  }

}
