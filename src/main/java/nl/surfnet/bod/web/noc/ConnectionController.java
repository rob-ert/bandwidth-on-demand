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

import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.service.AbstractFullTextSearchService;
import nl.surfnet.bod.service.ConnectionService;
import nl.surfnet.bod.web.base.AbstractSearchableSortableListController;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/noc/connections")
public class ConnectionController extends AbstractSearchableSortableListController<Connection, Connection> {

  @Resource
  private ConnectionService connectionService;

  @RequestMapping("/illegal")
  public String listIllegal(Model model) {
    model.addAttribute("list", connectionService.findWithIllegalState());
    return listUrl();
  }

  @Override
  protected List<Connection> transformToView(List<Connection> entities, RichUserDetails user) {
    return entities;
  }

  @Override
  protected String listUrl() {
    return "noc/connections/list";
  }

  @Override
  protected List<Connection> list(int firstPage, int maxItems, Sort sort, Model model) {
    return connectionService.findEntries(firstPage, maxItems, sort);
  }

  @Override
  protected long count(Model model) {
    return connectionService.count();
  }

  @Override
  protected List<Long> getIdsOfAllAllowedEntries(Model model) {
    throw new UnsupportedOperationException("Search not implemented for connections");
  }

  @Override
  protected AbstractFullTextSearchService<Connection> getFullTextSearchableService() {
    throw new UnsupportedOperationException("Search not implemented for connections");
  }

  @Override
  protected String getDefaultSortProperty() {
    return "reservation.startDateTime";
  }
}
