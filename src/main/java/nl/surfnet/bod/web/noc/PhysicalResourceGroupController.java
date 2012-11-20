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
package nl.surfnet.bod.web.noc;

import static nl.surfnet.bod.web.WebUtils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.context.MessageSource;
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

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.service.AbstractFullTextSearchService;
import nl.surfnet.bod.service.InstituteService;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.base.AbstractSearchableSortableListController;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.PhysicalResourceGroupView;

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
  private MessageSource messageSource;

  @Resource
  private PhysicalPortService physicalPortService;

  @Resource
  private VirtualPortService virtualPortService;

  @Resource
  private ReservationService reservationService;

  @RequestMapping(method = RequestMethod.POST)
  public String create(@Valid final PhysicalResourceGroupCommand command, final BindingResult bindingResult,
      final RedirectAttributes redirectAttributes, final Model model) {

    PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroup();
    command.copyFieldsTo(physicalResourceGroup);
    fillInstitute(command, physicalResourceGroup);

    if (bindingResult.hasErrors()) {
      model.addAttribute(MODEL_KEY, command);
      return PAGE_URL + CREATE;
    }

    model.asMap().clear();

    physicalResourceGroupService.save(physicalResourceGroup);
    physicalResourceGroupService.sendActivationRequest(physicalResourceGroup);

    WebUtils.addInfoFlashMessage(redirectAttributes, messageSource, "info_activation_request_send",
        physicalResourceGroup.getName(), physicalResourceGroup.getManagerEmail());

    // Force refresh of roles
    SecurityContextHolder.clearContext();

    return "redirect:" + PAGE_URL;
  }

  @RequestMapping(value = CREATE, method = RequestMethod.GET)
  public String createForm(final Model model) {
    model.addAttribute(MODEL_KEY, new PhysicalResourceGroupCommand());

    return "noc/" + PAGE_URL + CREATE;
  }

  @RequestMapping(method = RequestMethod.PUT)
  public String update(@Valid final PhysicalResourceGroupCommand command, final BindingResult result,
      final Model model, final RedirectAttributes redirectAttributes) {

    PhysicalResourceGroup physicalResourceGroup = physicalResourceGroupService.find(command.getId());

    if (physicalResourceGroup == null) {
      return "redirect:" + PAGE_URL;
    }

    command.copyFieldsTo(physicalResourceGroup);
    fillInstitute(command, physicalResourceGroup);

    if (result.hasErrors()) {
      model.addAttribute(MODEL_KEY, command);
      return PAGE_URL + UPDATE;
    }

    model.asMap().clear();

    physicalResourceGroupService.update(physicalResourceGroup);

    if (command.isManagerEmailChanged()) {
      physicalResourceGroupService.sendActivationRequest(physicalResourceGroup);
      addInfoFlashMessage(redirectAttributes, messageSource, "info_activation_request_send",
          physicalResourceGroup.getName(), physicalResourceGroup.getManagerEmail());
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
  public String delete(@RequestParam(ID_KEY) final Long id,
      @RequestParam(value = PAGE_KEY, required = false) final Integer page, final Model uiModel) {

    PhysicalResourceGroup physicalResourceGroup = physicalResourceGroupService.find(id);
    for (PhysicalPort physicalPort : physicalResourceGroup.getPhysicalPorts()) {
      final Collection<VirtualPort> virtualPorts = virtualPortService.findAllForPhysicalPort(physicalPort);
      final RichUserDetails userDetails = Security.getUserDetails();
      virtualPortService.deleteVirtualPorts(virtualPorts, userDetails);
      physicalPortService.deleteByNmsPortId(physicalPort.getNmsPortId());
    }
    physicalResourceGroupService.delete(physicalResourceGroup.getId());

    uiModel.asMap().clear();

    uiModel.addAttribute(PAGE_KEY, (page == null) ? "1" : page.toString());

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

    model.addAttribute("physicalPortsAmount", Integer.toString(physicalPorts.size()));
    model.addAttribute("virtualPortsAmount", Integer.toString(virtualPorts.size()));
    model.addAttribute("reservationsAmount", Integer.toString(reservations.size()));

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
  }

  @Override
  protected AbstractFullTextSearchService<PhysicalResourceGroup> getFullTextSearchableService() {
    return physicalResourceGroupService;
  }

  @Override
  protected List<Long> getIdsOfAllAllowedEntries(Model model) {
    return physicalResourceGroupService.findAllIds();
  }

  @Override
  protected List<PhysicalResourceGroupView> transformToView(List<PhysicalResourceGroup> entities, RichUserDetails user) {
    final List<PhysicalResourceGroupView> physicalResourceGroupViews = new ArrayList<>();
    for (final PhysicalResourceGroup physicalResourceGroup : entities) {
      final PhysicalResourceGroupView view = new PhysicalResourceGroupView(physicalResourceGroup);
      final List<VirtualPort> virtualPorts = new ArrayList<>();
      final List<Long> reservations = new ArrayList<>();
      final List<Long> physicalPortIds = physicalPortService.findIdsByRoleAndPhysicalResourceGroup(
          BodRole.createNocEngineer(), Optional.of(physicalResourceGroup));
      for (final long id : physicalPortIds) {
        final PhysicalPort physicalPort = physicalPortService.find(id);
        virtualPorts.addAll(virtualPortService.findAllForPhysicalPort(physicalPort));
        final long countActiveReservationsByVirtualPorts = reservationService.countActiveReservationsByVirtualPorts(virtualPorts);
        if(countActiveReservationsByVirtualPorts != 0L){
          System.out.println(countActiveReservationsByVirtualPorts);
          reservations.add(countActiveReservationsByVirtualPorts);
        }
      }
      view.setPhysicalPortsAmount(physicalPortIds.size());
      view.setReservationsAmount(reservations.size());
      view.setVirtualPortsAmount(virtualPorts.size());
      System.out.println(reservations);
      physicalResourceGroupViews.add(view);
    }
    return physicalResourceGroupViews;
  }
}
