/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.web.manager;

import static nl.surfnet.bod.web.WebUtils.DELETE;
import static nl.surfnet.bod.web.WebUtils.ID_KEY;
import static nl.surfnet.bod.web.WebUtils.LIST;
import static nl.surfnet.bod.web.WebUtils.PAGE_KEY;

import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.service.AbstractFullTextSearchService;
import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.web.base.AbstractSearchableSortableListController;
import nl.surfnet.bod.web.manager.VirtualResourceGroupController.VirtualResourceGroupView;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.collect.Lists;

@Controller("managerVirtualResourceGroupController")
@RequestMapping("/manager/" + VirtualResourceGroupController.PAGE_URL)
public class VirtualResourceGroupController extends
    AbstractSearchableSortableListController<VirtualResourceGroupView, VirtualResourceGroup>  {
  public static final String PAGE_URL = "teams";
  public static final String MODEL_KEY = "virtualResourceGroup";

  @Resource
  private VirtualResourceGroupService virtualResourceGroupService;

  @RequestMapping(value = DELETE, params = ID_KEY, method = RequestMethod.DELETE)
  public String delete(@RequestParam(ID_KEY) Long id, @RequestParam(value = PAGE_KEY, required = false) Integer page,
      Model model) {

    VirtualResourceGroup virtualResourceGroup = virtualResourceGroupService.find(id);
    virtualResourceGroupService.delete(virtualResourceGroup);

    model.asMap().clear();
    model.addAttribute(PAGE_KEY, (page == null) ? "1" : page.toString());

    // Force refresh of roles, a role should possibly be removed
    SecurityContextHolder.clearContext();

    return "redirect:";
  }

  @Override
  protected String listUrl() {
    return "manager/" + PAGE_URL + LIST;
  }

  @Override
  protected List<VirtualResourceGroupView> list(int firstPage, int maxItems, Sort sort, Model model) {
    List<VirtualResourceGroup> entriesForManager =
        virtualResourceGroupService.findEntriesForManager(Security.getSelectedRole(), firstPage, maxItems, sort);

    return transformToView(entriesForManager, Security.getUserDetails());
  }

  @Override
  protected long count(Model model) {
    return virtualResourceGroupService.countForManager(Security.getSelectedRole());
  }

  @Override
  protected String getDefaultSortProperty() {
    return "name";
  }

  @Override
  protected AbstractFullTextSearchService<VirtualResourceGroup> getFullTextSearchableService() {
    return virtualResourceGroupService;
  }

  @Override
  protected List<Long> getIdsOfAllAllowedEntries(Model model) {
    return virtualResourceGroupService.findTeamIdsForRole(Security.getSelectedRole());
  }

  @Override
  protected List<VirtualResourceGroupView> transformToView(List<VirtualResourceGroup> entities, RichUserDetails user) {
      return Lists.transform(entities, VirtualResourceGroupService.TO_MANAGER_VIEW);
  }

  public static class VirtualResourceGroupView {
    private final Long id;
    private final String name;
    private final String description;
    private final Integer allPortCount;
    private final Integer managerPortCount;
    private final String adminGroup;

    public VirtualResourceGroupView(VirtualResourceGroup group, Integer managerPortCount) {
      this.name = group.getName();
      this.description = group.getDescription();
      this.allPortCount = group.getVirtualPortCount();
      this.managerPortCount = managerPortCount;
      this.id = group.getId();
      this.adminGroup = group.getAdminGroup();
    }

    public String getName() {
      return name;
    }

    public String getDescription() {
      return description;
    }

    public Integer getAllPortCount() {
      return allPortCount;
    }

    public String getAdminGroup() {
      return adminGroup;
    }

    public Integer getManagerPortCount() {
      return managerPortCount;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      VirtualResourceGroupView other = (VirtualResourceGroupView) obj;
      if (id == null) {
        if (other.id != null) {
          return false;
        }
      }
      else if (!id.equals(other.id)) {
        return false;
      }
      return true;
    }

  }

}
