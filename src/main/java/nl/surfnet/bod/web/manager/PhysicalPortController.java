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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.service.InstituteService;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.web.AbstractFilteredSortableListController;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.VirtualPortJsonView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

@Controller("managerPhysicalPortController")
@RequestMapping("/manager/physicalports")
public class PhysicalPortController extends
    AbstractFilteredSortableListController<PhysicalPortController.PhysicalPortView> {

  @Autowired
  private PhysicalPortService physicalPortService;

  @Autowired
  private VirtualPortService virtualPortService;

  @Autowired
  private InstituteService instituteService;

  @Autowired
  private PhysicalResourceGroupService physicalResourceGroupService;

  private final Function<PhysicalPort, PhysicalPortView> toView = new Function<PhysicalPort, PhysicalPortView>() {
    @Override
    public PhysicalPortView apply(PhysicalPort port) {
      instituteService.fillInstituteForPhysicalResourceGroup(port.getPhysicalResourceGroup());
      Collection<VirtualPort> virtualPorts = virtualPortService.findAllForPhysicalPort(port);
      return new PhysicalPortView(port, virtualPorts);
    }
  };

  @RequestMapping(value = "/edit", params = "id", method = RequestMethod.GET)
  public String updateForm(@RequestParam("id") final Long id, final Model uiModel) {
    PhysicalPort port = physicalPortService.find(id);

    if (port == null || Security.managerMayNotEdit(port)) {
      return "manager/physicalports";
    }

    uiModel.addAttribute("updateManagerLabelCommand", new UpdateManagerLabelCommand(port));
    uiModel.addAttribute("physicalPort", toView.apply(port));

    return "manager/physicalports/update";
  }

  @RequestMapping(method = RequestMethod.PUT)
  public String update(final UpdateManagerLabelCommand command, final BindingResult result, final Model model) {
    PhysicalPort port = physicalPortService.find(command.getId());

    if (port == null || Security.managerMayNotEdit(port)) {
      return "redirect:physicalports";
    }

    port.setManagerLabel(command.getManagerLabel());
    physicalPortService.update(port);

    return "redirect:physicalports";
  }

  @RequestMapping(value = "/{id}/virtualports", method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  public Collection<VirtualPortJsonView> listVirtualPortsJson(@PathVariable Long id) {
    PhysicalPort physicalPort = physicalPortService.find(id);

    if (physicalPort == null) {
      return Collections.emptyList();
    }

    return Collections2.transform(virtualPortService.findAllForPhysicalPort(physicalPort),
        new Function<VirtualPort, VirtualPortJsonView>() {
          @Override
          public VirtualPortJsonView apply(VirtualPort port) {
            return new VirtualPortJsonView(port);
          }
        });
  }

  @Override
  protected String defaultSortProperty() {
    return "managerLabel";
  }

  @Override
  protected long count() {
    return physicalPortService.countAllocatedForUser(Security.getUserDetails());
  }

  @Override
  protected long count(String filterId, Model model) {

    if (!org.springframework.util.StringUtils.hasText(filterId)) {
      return 0;
    }

    PhysicalResourceGroup physicalResourceGroup = physicalResourceGroupService.find(Long.valueOf(filterId));

    return physicalPortService.countAllocatedForPhysicalResourceGroupAndUser(physicalResourceGroup,
        Security.getUserDetails());
  }

  @ModelAttribute
  protected void populateFilter(Model model) {
    RichUserDetails userDetails = Security.getUserDetails();
    Collection<PhysicalResourceGroup> groups = physicalResourceGroupService.findAllForManager(userDetails);

    // Select prg related to choosen role and related institute
    model.addAttribute("selPrg",
        physicalResourceGroupService.filterByInstituteId(groups, userDetails.getSelectedRole().getInstituteId()));

    // Put list on model
    model.addAttribute("selPrgList", groups);
  }

  // **** **** //
  // ** View/Command objects ** //
  // **** **** //
  public static final class UpdateManagerLabelCommand {
    private Long id;
    private Integer version;
    private String managerLabel;

    public UpdateManagerLabelCommand() {
    }

    public UpdateManagerLabelCommand(PhysicalPort port) {
      version = port.getVersion();
      id = port.getId();
      managerLabel = port.getManagerLabel();
    }

    public Long getId() {
      return id;
    }

    public Integer getVersion() {
      return version;
    }

    public String getManagerLabel() {
      return managerLabel;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public void setVersion(Integer version) {
      this.version = version;
    }

    public void setManagerLabel(String managerLabel) {
      this.managerLabel = managerLabel;
    }
  }

  public static final class PhysicalPortView {
    private final Long id;
    private final String managerLabel;
    private final String nocLabel;
    private final PhysicalResourceGroup physicalResourceGroup;
    private final String networkElementPk;
    private final Collection<VirtualPort> virtualPorts;

    public PhysicalPortView(PhysicalPort port, Collection<VirtualPort> virtualPorts) {
      this.id = port.getId();
      this.managerLabel = port.getManagerLabel();
      this.nocLabel = port.getNocLabel();
      this.networkElementPk = port.getNetworkElementPk();
      this.physicalResourceGroup = port.getPhysicalResourceGroup();
      this.virtualPorts = virtualPorts;
    }

    public String getNetworkElementPk() {
      return networkElementPk;
    }

    public Integer getNumberOfVirtualPorts() {
      return virtualPorts.size();
    }

    public String getManagerLabel() {
      return managerLabel;
    }

    public PhysicalResourceGroup getPhysicalResourceGroup() {
      return physicalResourceGroup;
    }

    public Long getId() {
      return id;
    }

    public String getNocLabel() {
      return nocLabel;
    }

  }

  @Override
  protected String listUrl() {
    return "manager/physicalports/list";
  }

  @Override
  protected List<PhysicalPortView> list(int firstPage, int maxItems, Sort sort, String filterId, Model model) {
    PhysicalResourceGroup physicalResourceGroup = null;

    if (filterId != null) {
      physicalResourceGroup = physicalResourceGroupService.find(Long.valueOf(filterId));
    }
    else {
      // Just pick the first one
      physicalResourceGroup = physicalResourceGroupService.findAllForManager(Security.getUserDetails()).iterator()
          .next();
    }

    List<PhysicalPort> list = physicalPortService.findAllocatedEntriesForPhysicalResourceGroupAndUser(
        physicalResourceGroup, Security.getUserDetails(), firstPage, maxItems, sort);

    return Lists.transform(list, toView);
  }

}
