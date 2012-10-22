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
package nl.surfnet.bod.web.base;

import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.event.LogEvent;
import nl.surfnet.bod.service.AbstractFullTextSearchService;
import nl.surfnet.bod.service.LogEventService;
import nl.surfnet.bod.service.VirtualResourceGroupService;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.ui.Model;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public abstract class AbstractLogEventController extends AbstractSearchableSortableListController<LogEvent, LogEvent> {
  public static final String PAGE_URL = "logevents";

  static final String MODEL_KEY = "list";

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Resource
  private LogEventService logEventService;

  @Resource
  private VirtualResourceGroupService virtualResourceGroupService;

  @Override
  protected String getDefaultSortProperty() {
    return "created";
  }

  @Override
  protected Direction getDefaultSortOrder() {
    return Direction.DESC;
  }

  @Override
  protected List<String> translateSortProperty(String sortProperty) {
    if ("eventType".equals(sortProperty)) {
      return ImmutableList.of("eventType", "correlationId");
    }

    return super.translateSortProperty(sortProperty);
  }

  @Override
  protected List<LogEvent> list(int firstPage, int maxItems, Sort sort, Model model) {
    return logEventService.findByAdminGroups(determinGroupsToSearchFor(Security.getUserDetails()), firstPage, maxItems, sort);
  }

  @Override
  protected long count(Model model) {
    return logEventService.countByAdminGroups(determinGroupsToSearchFor(Security.getUserDetails()));
  }

  @Override
  protected AbstractFullTextSearchService<LogEvent> getFullTextSearchableService() {
    return logEventService;
  }

  protected List<String> determinGroupsToSearchFor(RichUserDetails user) {
    List<String> groups = Lists.newArrayList();

    if (user.isSelectedUserRole()) {
      groups.addAll(
          Collections2.filter(user.getUserGroupIds(),
          new Predicate<String>() {
            @Override
            public boolean apply(String groupId) {
              return virtualResourceGroupService.findBySurfconextGroupId(groupId) != null;
            }
          })
      );
    }
    else if (user.isSelectedManagerRole()) {
      groups.add(user.getSelectedRole().getAdminGroup().get());
    }

    logger.debug("Groups to search for: {}", groups);

    return groups;
  }

  @Override
  protected List<LogEvent> transformToView(List<LogEvent> entities, RichUserDetails user) {
    return entities;
  }

  protected final LogEventService getLogEventService() {
    return logEventService;
  }


}
