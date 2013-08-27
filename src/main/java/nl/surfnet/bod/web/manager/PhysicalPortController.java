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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.google.common.base.Optional;

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

@Controller("managerPhysicalPortController")
@RequestMapping(PhysicalPortController.PAGE_URL)
public class PhysicalPortController extends AbstractSearchableSortableListController<PhysicalPortView, UniPort> {

  public static final String PAGE_URL = "/manager/physicalports";

  @Resource private PhysicalPortService physicalPortService;
  @Resource private VirtualPortService virtualPortService;
  @Resource private PhysicalResourceGroupService physicalResourceGroupService;
  @Resource private ReservationService reservationService;

  @RequestMapping(value = "/edit", params = "id", method = RequestMethod.GET)
  public String updateForm(@RequestParam("id") Long id, Model uiModel) {
    UniPort port = physicalPortService.findUniPort(id);

    if (port == null || Security.managerMayNotEdit(port)) {
      return "manager/physicalports";
    }

    uiModel.addAttribute("physicalPort", port);
    uiModel.addAttribute("updateManagerLabelCommand", new UpdateManagerLabelCommand(port));

    return "manager/physicalports/update";
  }

  @RequestMapping(method = RequestMethod.PUT)
  public String update(UpdateManagerLabelCommand command, BindingResult result, Model model) {
    UniPort port = physicalPortService.findUniPort(command.getId());

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

    public UpdateManagerLabelCommand(UniPort port) {
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
  protected List<UniPort> list(int firstPage, int maxItems, Sort sort, Model model) {
    Optional<PhysicalResourceGroup> physicalResourceGroup = getCurrentPhysicalResourceGroup();

    if (physicalResourceGroup.isPresent()) {
      return physicalPortService.findAllocatedEntriesForPhysicalResourceGroup(physicalResourceGroup.get(), firstPage, maxItems, sort);
    } else {
      return new ArrayList<>();
    }
  }

  @Override
  protected List<Long> getIdsOfAllAllowedEntries(Model model, Sort sort) {
    Optional<PhysicalResourceGroup> physicalResourceGroup = getCurrentPhysicalResourceGroup();
    if (physicalResourceGroup.isPresent()) {
      return physicalPortService.findIdsByRoleAndPhysicalResourceGroup(
        Security.getSelectedRole(), Optional.of(physicalResourceGroup.get()), Optional.<Sort>fromNullable(sort));
    } else {
      return new ArrayList<>();
    }
  }

  private Optional<PhysicalResourceGroup> getCurrentPhysicalResourceGroup() {
    Optional<Long> groupId = WebUtils.getSelectedPhysicalResourceGroupId();
    if (!groupId.isPresent()) {
      return Optional.absent();
    }

    PhysicalResourceGroup physicalResourceGroup = physicalResourceGroupService.find(groupId.get());

    return Optional.of(physicalResourceGroup);
  }

  @Override
  protected AbstractFullTextSearchService<UniPort> getFullTextSearchableService() {
    return physicalPortService;
  }

  @Override
  protected List<? extends PhysicalPortView> transformToView(List<? extends UniPort> entities, RichUserDetails user) {
    return Functions.transformAllocatedPhysicalPorts(entities, virtualPortService, reservationService);
  }
}
