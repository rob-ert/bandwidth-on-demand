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

import java.util.List;

import javax.annotation.Resource;

import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.service.AbstractFullTextSearchService;
import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.web.base.AbstractSearchableSortableListController;
import nl.surfnet.bod.web.manager.VirtualResourceGroupController.VirtualResourceGroupView;
import nl.surfnet.bod.web.security.Security;

@Controller("managerVirtualResourceGroupController")
@RequestMapping("/manager/" + VirtualResourceGroupController.PAGE_URL)
public class VirtualResourceGroupController extends AbstractSearchableSortableListController<VirtualResourceGroupView, VirtualResourceGroup>  {
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
    return virtualResourceGroupService.transformToView(virtualResourceGroupService.findEntries(firstPage, maxItems, sort), Security.getUserDetails());
    
  }

  @Override
  protected long count() {
    return virtualResourceGroupService.countForManager(Security.getSelectedRole());
  }

  @Override
  protected String getDefaultSortProperty() {
    return "name";
  }

  public static class VirtualResourceGroupView {
    private final String name;
    private final String description;
    private final Integer allPortCount;
    private final Integer managerPortCount;
    private final String surfconextGroupId;

    public VirtualResourceGroupView(VirtualResourceGroup group, Integer managerPortCount) {
      this.name = group.getName();
      this.description = group.getDescription();
      this.allPortCount = group.getVirtualPortCount();
      this.managerPortCount = managerPortCount;
      this.surfconextGroupId = group.getSurfconextGroupId();
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

    public String getSurfconextGroupId() {
      return surfconextGroupId;
    }

    public Integer getManagerPortCount() {
      return managerPortCount;
    }

  }

  @Override
  protected Class<VirtualResourceGroup> getEntityClass() {
    return VirtualResourceGroup.class;
  }

  @Override
  protected AbstractFullTextSearchService<VirtualResourceGroupView, VirtualResourceGroup> getFullTextSearchableService() {
    return virtualResourceGroupService;
  }

}
