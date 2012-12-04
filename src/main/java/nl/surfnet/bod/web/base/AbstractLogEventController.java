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
              return virtualResourceGroupService.findByAdminGroup(groupId) != null;
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
