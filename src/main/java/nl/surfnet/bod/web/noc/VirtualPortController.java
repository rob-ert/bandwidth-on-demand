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

import java.util.List;

import javax.annotation.Resource;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.service.AbstractFullTextSearchService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.base.AbstractSearchableSortableListController;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.VirtualPortView;

@Controller("nocVirtualPortController")
@RequestMapping("/noc/virtualports")
public class VirtualPortController extends AbstractSearchableSortableListController<VirtualPortView, VirtualPort> {

  @Resource
  private VirtualPortService virtualPortService;

  @Override
  protected AbstractFullTextSearchService<VirtualPort> getFullTextSearchableService() {
    return virtualPortService;
  }

  @Override
  protected String listUrl() {
    return "/noc/virtualports/list";
  }

  @Override
  protected List<VirtualPortView> list(int firstPage, int maxItems, Sort sort, Model model) {
    List<VirtualPort> entriesForManager = virtualPortService.findEntries(firstPage, maxItems);

    return transformToView(entriesForManager, Security.getUserDetails());
  }

  @Override
  protected long count(Model model) {
    return virtualPortService.count();
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

  @Override
  protected List<Long> getIdsOfAllAllowedEntries(Model model) {
    final VirtualPortView filter = WebUtils.getAttributeFromModel(FILTER_SELECT, model);
    return virtualPortService.findIdsForUserUsingFilter(Security.getUserDetails(), filter);
  }

  @Override
  protected List<VirtualPortView> transformToView(List<VirtualPort> entities, RichUserDetails user) {
    return Lists.transform(entities, nl.surfnet.bod.util.Functions.FROM_VIRTUALPORT_TO_VIRTUALPORT_VIEW);
  }
}
