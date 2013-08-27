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
package nl.surfnet.bod.web.noc;

import static nl.surfnet.bod.web.WebUtils.FILTER_LIST;
import static nl.surfnet.bod.web.WebUtils.FILTER_SELECT;
import static nl.surfnet.bod.web.WebUtils.PAGE_KEY;

import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.service.AbstractFullTextSearchService;
import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.util.Functions;
import nl.surfnet.bod.web.base.AbstractSearchableSortableListController;
import nl.surfnet.bod.web.noc.PhysicalPortController.PhysicalPortFilter;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.view.PhysicalPortView;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/noc/physicalports/unaligned")
public class UnalignedPortController extends AbstractSearchableSortableListController<PhysicalPortView, PhysicalPort> {

  @Resource private PhysicalPortService physicalPortService;
  @Resource private VirtualPortService virtualPortService;
  @Resource private ReservationService reservationService;

  @RequestMapping(method = RequestMethod.GET)
  @Override
  public String list(@RequestParam(value = PAGE_KEY, required = false) Integer page,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "order", required = false) String order, Model model) {

    model.addAttribute(FILTER_SELECT, PhysicalPortFilter.UN_ALIGNED);
    model.addAttribute(FILTER_LIST, PhysicalPortFilter.getAvailableFilters());

    return super.list(page, sort, order, model);
  }

  @Override
  protected List<? extends PhysicalPortView> transformToView(List<? extends PhysicalPort> entities, RichUserDetails user) {
    return Functions.transformUnalignedPhysicalPorts(entities, virtualPortService, reservationService);
  }

  @Override
  protected String listUrl() {
    return "noc/physicalports/unaligned/list";
  }

  @Override
  protected List<PhysicalPort> list(int firstPage, int maxItems, Sort sort, Model model) {
    return physicalPortService.findUnalignedPhysicalPorts(firstPage, maxItems, sort);
  }

  @Override
  protected long count(Model model) {
    return physicalPortService.countUnalignedPhysicalPorts();
  }

  @Override
  protected List<Long> getIdsOfAllAllowedEntries(Model model, Sort sort) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected AbstractFullTextSearchService<PhysicalPort> getFullTextSearchableService() {
    // TODO
    //return physicalPortService;
    return null;
  }

  @Override
  protected String getDefaultSortProperty() {
    return "nocLabel";
  }

//  @RequestMapping(value = "/unaligned/search", method = RequestMethod.GET)
//  public String searchUnaligned(@RequestParam(value = PAGE_KEY, required = false) Integer page,
//      @RequestParam String search, @RequestParam(value = "sort", required = false) String sort,
//      @RequestParam(value = "order", required = false) String order, Model model) {
//
//    List<Long> unalignedPorts = getIdsOfAllAllowedEntries(model, prepareSortOptions(sort, order, model));
//
//    try {
//      FullTextSearchResult<UniPort> searchResult = getFullTextSearchableService().searchForInFilteredList(
//          UniPort.class, search, calculateFirstPage(page), MAX_ITEMS_PER_PAGE, Security.getUserDetails(),
//          unalignedPorts);
//
//      model.addAttribute(PARAM_SEARCH, StringEscapeUtils.escapeHtml(search));
//      model.addAttribute(MAX_PAGES_KEY, calculateMaxPages(searchResult.getTotalCount()));
//      model.addAttribute(DATA_LIST, transformToView(searchResult.getResultList(), Security.getUserDetails()));
//      model.addAttribute(FILTER_SELECT, PhysicalPortFilter.UN_ALIGNED);
//      model.addAttribute(FILTER_LIST, PhysicalPortFilter.getAvailableFilters());
//
//    } catch (ParseException e) {
//      model.addAttribute(MessageManager.WARN_MESSAGES_KEY, Lists.newArrayList("Sorry, we could not process your search query."));
//    }
//
//    return listUrl();
//  }
}
