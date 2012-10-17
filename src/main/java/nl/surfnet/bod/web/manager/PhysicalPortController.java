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

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.service.AbstractFullTextSearchService;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.util.Functions;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.base.AbstractSearchableSortableListController;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.PhysicalPortView;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

@Controller("managerPhysicalPortController")
@RequestMapping(PhysicalPortController.PAGE_URL)
public class PhysicalPortController extends AbstractSearchableSortableListController<PhysicalPortView, PhysicalPort> {

  public static final String PAGE_URL = "/manager/physicalports";

  @Resource
  private PhysicalPortService physicalPortService;

  @Resource
  private VirtualPortService virtualPortService;

  @Resource
  private PhysicalResourceGroupService physicalResourceGroupService;

  @RequestMapping(value = "/edit", params = "id", method = RequestMethod.GET)
  public String updateForm(@RequestParam("id") final Long id, final Model uiModel) {
    PhysicalPort port = physicalPortService.find(id);

    if (port == null || Security.managerMayNotEdit(port)) {
      return "manager/physicalports";
    }

    uiModel.addAttribute("physicalPort", port);
    uiModel.addAttribute("updateManagerLabelCommand", new UpdateManagerLabelCommand(port));

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

  @Override
  protected String getDefaultSortProperty() {
    return "managerLabel";
  }

  @Override
  protected long count(Model model) {
    Optional<Long> groupId = WebUtils.getSelectedPhysicalResourceGroupId();

    if (!groupId.isPresent()) {
      return 0;
    }

    PhysicalResourceGroup physicalResourceGroup = physicalResourceGroupService.find(groupId.get());

    return physicalPortService.countAllocatedForPhysicalResourceGroup(physicalResourceGroup);
  }

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

  @Override
  protected String listUrl() {
    return "manager/physicalports/list";
  }

  @Override
  protected List<PhysicalPortView> list(int firstPage, int maxItems, Sort sort, Model model) {

    Optional<Long> groupId = WebUtils.getSelectedPhysicalResourceGroupId();
    if (!groupId.isPresent()) {
      return Lists.newArrayList();
    }

    PhysicalResourceGroup physicalResourceGroup = physicalResourceGroupService.find(groupId.get());

    return Functions.transformAllocatedPhysicalPorts(physicalPortService.findAllocatedEntriesForPhysicalResourceGroup(
        physicalResourceGroup, firstPage, maxItems, sort), virtualPortService);
  }

  @Override
  public List<Long> handleListFromController(Model model) {
    // TODO Auto-generated method stub
    return Collections.emptyList();
  }

  @Override
  protected AbstractFullTextSearchService<PhysicalPort> getFullTextSearchableService() {
    return physicalPortService;
  }

  @Override
  public List<PhysicalPortView> transformToView(List<PhysicalPort> entities, RichUserDetails user) {
    return Functions.transformAllocatedPhysicalPorts(entities, virtualPortService);
  }
}
